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
  public objectUpdateObservers: Observer<any>[] = [];
  public notificationObservers: Observer<any>[] = [];
  public chatObservers: Observer<any>[] = [];
  public clientPingObservers: Observer<String>[] = [];
  public requestObservers: any = {};
  public uniqueObjectSubscriptions: any[] = [];
  public filterObjectSubscriptions: any = {};
  public connected: boolean = false;
  public heartbeatFreq: number = 0;

  constructor(
    private http: HttpClient
  ) { }

  initWebsocket() {
    if(this.path != null && this.path != "" && this.websocket == null) {
      this.websocket = webSocket({
        url: this.baseUrl.replace('http:', 'ws:').replace('https:', 'wss:') + '/' + this.path,
        openObserver: {next: () => this.opened()},
        closeObserver: {next: () => this.closed()}
      });
      this.initWebsocketSubscribe();
    }
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
        console.log("WS Connection Open");
        this.uniqueObjectSubscriptions.forEach(item => this.subscribeToUniqueObjectUpdate(item.objectname, item.uid));
        Object.keys(this.filterObjectSubscriptions).forEach(key => this.subscribeToFilterObjectUpdate(this.filterObjectSubscriptions[key].objectname, this.filterObjectSubscriptions[key].filter, key));
        this.connected = true;
        this.heartbeatFreq = 10000;
      }

      if(msg.type == 'objectupdate') {
        this.objectUpdateObservers.forEach((observer) => {
          observer.next(msg.object);
        });  
      } else if(msg.type == 'notification') {
        this.notificationObservers.forEach((observer) => {
          observer.next(msg.notification);
        })
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
      } else if(msg.type == 'chatmessage') {
        this.chatObservers.forEach((observer) => {
          observer.next(msg.message);
        })
      } else if(msg.type == 'clientpong') {
        this.clientPingObservers.forEach((observer) => {
          observer.next(msg.username);
        })
      }
    } catch(err) {
      console.error('WS receive error for message ' + ': ' + err);
    }
  }

  error(error: String) {
    
  }

  closed() {
    this.heartbeatFreq = 0;
    this.connected = false;
    setTimeout(() => {this.initWebsocketSubscribe()}, 1000);
    console.log("WSS Connection closed");
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


