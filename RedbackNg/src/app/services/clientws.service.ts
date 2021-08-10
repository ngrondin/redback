import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Observer } from 'rxjs';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { UUID } from 'angular2-uuid';

export class Upload {
  uploaduid = UUID.UUID();
  chunkSeq = 0;
  chunkSize = 262144;
  file: File;
  observer: Observer<any>;
  websocket: WebSocketSubject<any>;

  constructor(f: File, o: Observer<any>, w: WebSocketSubject<any>) {
    this.file = f;
    this.observer = o;
    this.websocket = w;
  }

  receiveCtl(msg: any) : boolean {
    let ctl = msg.ctl;
    if(ctl == 'next') {
      var start = this.chunkSeq * this.chunkSize;
      if(start < this.file.size) {
        var chunk = this.file.slice(start, start + this.chunkSize);
        const reader = new FileReader();
        reader.onload = () => {
          this.websocket.next({
            type: "upload",
            uploaduid: this.uploaduid,
            sequence: this.chunkSeq,
            data: reader.result
          });
          this.chunkSeq++;  
          const prog = Math.round(100 * start / this.file.size);
          console.log("File upload progress " + prog);
          this.observer.next({type:"progress", value: prog});
        }
        reader.readAsDataURL(chunk);
      } else {
        this.websocket.next({
          type: "upload",
          uploaduid: this.uploaduid,
          complete: true
        });
      }
      return false;
    } else if(ctl == 'error') {
      this.observer.error(msg.error);
      return true;
    } else if(ctl == 'result') {
      this.observer.next({type:"result", result: msg.result});
      this.observer.complete();
      return true;
    }
  }
}

@Injectable({
  providedIn: 'root'
})
export class ClientWSService {
  public baseUrl: string;
  public path: string;
  public websocket: WebSocketSubject<any>;
  public stateObservers: Observer<boolean>[] = [];
  public objectUpdateObservers: Observer<any>[] = [];
  public notificationObservers: Observer<any>[] = [];
  public chatObservers: Observer<any>[] = [];
  public clientPingObservers: Observer<String>[] = [];
  public requestObservers: any = {};
  public uploads: any = {};
  public uniqueObjectSubscriptions: any[] = [];
  public filterObjectSubscriptions: any = {};
  public subscriptionRequestPending: boolean = false;
  public connected: boolean = false;
  public heartbeatFreq: number = 0;

  constructor(
    private http: HttpClient
  ) { }

  initWebsocket() : Observable<boolean> {
    if(this.path != null && this.path != "" && this.websocket == null) {
      this.websocket = webSocket({
        url: this.baseUrl.replace('http:', 'ws:').replace('https:', 'wss:') + '/' + this.path + '?firebus-timezone=' + Intl.DateTimeFormat().resolvedOptions().timeZone,
        openObserver: {next: () => this.opened()},
        closeObserver: {next: (closeEvent) => this.closed(closeEvent)}
      });
      this.initWebsocketSubscribe();
    }
    return this.getStateObservable();
  }

  initWebsocketSubscribe() {
    this.websocket.asObservable().subscribe(
      (msg) => this.receive(msg),
      (err) => this.error(err)
    );
  }

  opened() {
    this.heartbeatFreq = 500;
    this.heartbeat();
  }

  receive(msg: any) {
    try {
      if(this.connected == false) {
        this.connected = true;
        console.log("WS Connection Open");
        this.sendSubscriptionRequests();
        this.heartbeatFreq = 10000;
        this.stateObservers.forEach((observer) => observer.next(true));
      }

      if(msg.type == 'objectupdate') {
        this.objectUpdateObservers.forEach((observer) => observer.next(msg.object));  
      } else if(msg.type == 'notification') {
        this.notificationObservers.forEach((observer) => observer.next(msg.notification))
      } else if(msg.type == 'serviceresponse' || msg.type == 'serviceerror') {
        let observer: Observer<any> = this.requestObservers[msg.requid];
        if(observer != null) {
          if(msg.type == 'serviceresponse') {
            observer.next(msg.response);
            observer.complete();
          } else if(msg.type == 'serviceerror') {
            observer.error(msg.error);
          }
          delete this.requestObservers[msg.requid];
        }
      } else if(msg.type == 'uploadctl') {
        let upload: Upload = this.uploads[msg.uploaduid];
        if(upload != null) {
          let done = upload.receiveCtl(msg);
          if(done) delete this.uploads[msg.uploaduid];
        }
      } else if(msg.type == 'serverkeepalive') {
        this.websocket.next({type:"clientisalive"});
      } else if(msg.type == 'chatmessage') {
        this.chatObservers.forEach((observer) => observer.next(msg.message))
      } else if(msg.type == 'clientpong') {
        this.clientPingObservers.forEach((observer) => observer.next(msg.username))
      }
    } catch(err) {
      console.error('WS receive error for message ' + ': ' + err);
    }
  }

  error(error: String) {
    
  }

  closed(event: any) {
    if(this.connected) {
      this.heartbeatFreq = 0;
      this.connected = false;
      this.uniqueObjectSubscriptions.forEach(item => item.sent = false);
      Object.keys(this.filterObjectSubscriptions).forEach(key => this.filterObjectSubscriptions[key].sent = false);
      console.log("WSS Connection closed");
      this.stateObservers.forEach((observer) => observer.next(false));
    }
    setTimeout(() => {this.initWebsocketSubscribe()}, 1000);
  }
  
  heartbeat() {
    if(this.heartbeatFreq > 0) {
      this.websocket.next({type:"heartbeat"});
      setTimeout(() => {this.heartbeat()}, this.heartbeatFreq);
    }
  }

  sendSubscriptionRequests() {
    if(this.subscriptionRequestPending == false) {
      this.subscriptionRequestPending = true;
      setTimeout(() => {
        let subreq = {type: "subscribe", list: []};
        this.uniqueObjectSubscriptions.filter(item => item.sent == false).forEach(item => subreq.list.push({"objectname": item.objectname, "uid": item.uid}));
        Object.keys(this.filterObjectSubscriptions).filter(key => this.filterObjectSubscriptions[key].sent == false).forEach(key => subreq.list.push({"objectname": this.filterObjectSubscriptions[key].objectname, "filter": this.filterObjectSubscriptions[key].filter, "id": key}));
        this.websocket.next(subreq);    
        this.subscriptionRequestPending = false;
      }, 500);
    }
  }

  getStateObservable() : Observable<any>  {
    return new Observable<any>((observer) => {
      this.stateObservers.push(observer);
    });
  }

  getObjectUpdateObservable() : Observable<any>  {
    return new Observable<any>((observer) => {
      this.objectUpdateObservers.push(observer);
    });
  }

  getNotificationObservable() : Observable<any>  {
    return new Observable<any>((observer) => {
      this.notificationObservers.push(observer);
    });
  }

  getChatObservable() : Observable<any> {
    return new Observable<any>((observer) => {
      this.chatObservers.push(observer);
    })
  }

  isConnected() : Boolean {
    return this.connected;
  }

  subscribeToUniqueObjectUpdate(objectname: string, uid: string) {
    this.uniqueObjectSubscriptions.push({objectname: objectname, uid: uid, sent:false});
    this.sendSubscriptionRequests();
  }

  subscribeToFilterObjectUpdate(objectname: String, filter: any, id: string) {
    this.filterObjectSubscriptions[id] = {objectname: objectname, filter: filter, sent:false};
    this.sendSubscriptionRequests();
  }

  clearSubscriptions() {
    this.uniqueObjectSubscriptions = [];
    this.filterObjectSubscriptions = {};
    if(this.connected) {
      this.websocket.next({
        type: "unsubscribe"
      });
    }
  }

  request(service: string, request: any, timeout: number) : Observable<any> {
    if(this.connected) {
      return new Observable((observer) => {
        let ruid = UUID.UUID();
        this.requestObservers[ruid] = observer;
        this.websocket.next({
          type:"servicerequest",
          servicename: service,
          requid: ruid,
          request: request,
          timeout: timeout
        });
      });
    } else {
      return null;
    }
  }

  upload(file: File, object: string, uid: string) : Observable<number> {
    if(this.connected) {
      return new Observable((observer) => {
        let upload = new Upload(file, observer, this.websocket);
        this.uploads[upload.uploaduid] = upload;
        this.websocket.next({
          type:"upload",
          uploaduid: upload.uploaduid,
          request: {
            filename: file.name,
            filesize: file.size,
            mime: file.type,
            object: object,
            uid: uid
          }
        });       
      })
    } else {
      return null;
    }
  }

  pingOtherClients() : Observable<any> {
    if(this.connected) {
      return new Observable((observer) => {
        this.clientPingObservers.push(observer);
        this.websocket.next({
          type:"pingclients"
        });
        setTimeout(() => {
          observer.complete();
          this.clientPingObservers.splice(this.clientPingObservers.indexOf(observer), 1);
        }, 3000);
      });
    } else {
      return null;
    }    
  }

  sendChat(to: String[], id: String, object: String, uid: String, body: String) {
    let json: any = {
      action: "sendtext",
      to: to,
      chatid: id,
      object: object,
      uid: uid,
      body: body
    }
    this.websocket.next(json);
  }
}


