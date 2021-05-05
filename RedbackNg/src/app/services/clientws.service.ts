import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Observer } from 'rxjs';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { UUID } from 'angular2-uuid';
import { id } from '@swimlane/ngx-charts';

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
  public uploadObservers: any = {};
  public uniqueObjectSubscriptions: any[] = [];
  public filterObjectSubscriptions: any = {};
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
        closeObserver: {next: () => this.closed()}
      });
      this.initWebsocketSubscribe();
    }
    return new Observable<boolean>((observer) => {
      this.stateObservers.push(observer);
    })
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
        let resubscribe = {type: "subscribe", list: []};
        this.uniqueObjectSubscriptions.forEach(item => resubscribe.list.push({"objectname": item.objectname, "uid": item.uid}));
        Object.keys(this.filterObjectSubscriptions).forEach(key => resubscribe.list.push({"objectname": this.filterObjectSubscriptions[key].objectname, "filter": this.filterObjectSubscriptions[key].filter, "id": key}));
        this.websocket.next(resubscribe);
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
        let observer: Observer<null> = this.uploadObservers[msg.uploaduid];
        if(observer != null) {
          if(msg.ctl == 'next') {
            observer.next(null);
          } else if(msg.ctl == 'error') {
            observer.error(msg.error);
          } else if(msg.ctl == 'result') {
            observer.complete();
          }
        }
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

  closed() {
    if(this.connected) {
      this.heartbeatFreq = 0;
      this.connected = false;
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
    this.uniqueObjectSubscriptions.push({objectname: objectname, uid: uid});
    if(this.connected) {
      this.websocket.next({
        type: "subscribe",
        objectname: objectname,
        uid: uid
      });
    }
  }

  subscribeToFilterObjectUpdate(objectname: String, filter: any, id: string) {
    this.filterObjectSubscriptions[id] = {objectname: objectname, filter: filter};
    if(this.connected) {
      this.websocket.next({
        type: "subscribe",
        objectname: objectname,
        filter: filter,
        id: id
      });
    }
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

  request(service: string, request: any) : Observable<any> {
    if(this.connected) {
      return new Observable((observer) => {
        let ruid = UUID.UUID();
        this.requestObservers[ruid] = observer;
        this.websocket.next({
          type:"servicerequest",
          servicename: service,
          requid: ruid,
          request: request
        });
      });
    } else {
      return null;
    }
  }

  upload(file: File, object: string, uid: string) : Observable<number> {
    if(this.connected) {
      return new Observable((observer) => {
        let uploaduid = UUID.UUID();
        let chunkSeq = 0;
        let chunkSize = 262144;
        this.uploadObservers[uploaduid] = { 
          next: () => {
            var start = chunkSeq * chunkSize;
            if(start < file.size) {
              var chunk = file.slice(start, start + chunkSize);
              const reader = new FileReader();
              reader.onload = () => {
                this.websocket.next({
                  type: "upload",
                  uploaduid: uploaduid,
                  sequence: chunkSeq,
                  data: reader.result
                });
                chunkSeq++;  
                observer.next(100 * start / file.size);
              }
              reader.readAsDataURL(chunk);
            } else {
              this.websocket.next({
                type: "upload",
                uploaduid: uploaduid,
                complete: true
              });
              observer.next(100);
            }
          },
          error: (error) => {
            observer.error("Error uploading file: " + error);
          },
          complete: () => {
            observer.complete();
          }
        };
        this.websocket.next({
          type:"upload",
          uploaduid: uploaduid,
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


