declare const google: any

import { Injectable } from '@angular/core';
import { Observable, of, Observer } from 'rxjs';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { ResponseContentType, RequestOptions } from '@angular/http';


const httpOptions = {
  headers: new HttpHeaders()
    .set("Content-Type", "application/json")
    .set("firebus-timezone", Intl.DateTimeFormat().resolvedOptions().timeZone),
  withCredentials: true
};

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  public baseUrl: string;
  public uiService: string;
  public objectService: string;
  public fileService: string;
  public domainService: string;
  public reportService: string;
  public processService: string;
  public userprefService: string;
  public signalService: string;
  public signalWebsocket: WebSocketSubject<any>;
  public signalObservers: Observer<String>[] = [];
  public chatService: string;
  public chatWebsocket: WebSocketSubject<any>;
  public chatObservers: Observer<String>[] = [];
  public placesAutocompleteService: any;

  constructor(
    private http: HttpClient
  ) { 
    this.placesAutocompleteService = new google.maps.places.AutocompleteService();
  }

  getObject(name: string, uid: string): Observable<any> {
    const req = {
      action: 'get',
      object: name,
      uid: uid,
      options: {
        addrelated: true,
        addvalidation: true
      }
    };
    return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }

  listObjects(name: string, filter: any, search: string, sort: any, page: number, pageSize: number): Observable<any> {
    const req = {
      action: 'list',
      object: name,
      filter: filter,
      sort: sort,
      page: page,
      pagesize: pageSize,
      options: {
        addrelated: true,
        addvalidation: true
      }
    };
    if(search != null)
      req['search'] = search;
    return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }

  listRelatedObjects(name: string, uid: string, attribute: string, filter: any, search: string, sort: any): Observable<any> {
    const req = {
      action: 'list',
      object: name,
      uid: uid,
      attribute: attribute,
      filter: filter,
      sort: sort,
      options: {
        addrelated: true,
        addvalidation: true
      }
    };
    if(search != null)
      req['search'] = search;
    return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }

  updateObject(name: string, uid: string, data: any) {
    const req = {
      action: 'update',
      object: name,
      uid: uid,
      data: data,
      options: {
        addrelated: true,
        addvalidation: true
      }
    };
    return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }

  createObject(name: string, uid: string, data: any) {
    const req = {
      action: 'create',
      object: name,
      data: data,
      options: {
        addrelated: true,
        addvalidation: true
      }
    };
    if(uid != null) {
      req['uid'] = uid;
    }
    return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }

  deleteObject(name: string, uid: string) {
    const req = {
      action: 'delete',
      object: name,
      uid: uid
    };
    return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }

  executeObject(name: string, uid: string, func: string) {
    const req = {
      action: 'execute',
      object: name,
      uid: uid,
      function: func,
      options: {
        addrelated: true,
        addvalidation: true
      }
    };
    return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }
  
  executeGlobal(func: string, param: any) {
    const req = {
      action: 'execute',
      function: func,
      param: param
    };
    return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }

  aggregateObjects(name: string, filter: any, search: string, tuple: any, metrics: any): Observable<any> {
    const req = {
      action: 'aggregate',
      object: name,
      filter: filter,
      search: search,
      tuple: tuple,
      metrics: metrics,
      options: {
        addrelated: true
      }
    };
    return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }

  exportObjects(name: string, filter: any, search: string): Observable<any> {
    const req = {
      action: 'list',
      object: name,
      filter: filter,
      page: 0,
      pagesize: 5000,
      options: {
        addrelated: true,
        addvalidation: false,
        format: "csv"
      }
    };
    if(search != null)
      req['search'] = search;
    const headers = new HttpHeaders().set('Content-Type', 'text/plain; charset=utf-8');
    
    return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, { headers, withCredentials: true, responseType: 'text' as 'json'});
  }

  /******* Files *********/

  listFiles(object: string, uid: string): Observable<any> {
    return this.http.get<any>(this.baseUrl + '/' + this.fileService + '?action=list&object=' + object + '&uid=' + uid, httpOptions);
  }

  /******* Domain *********/

  listDomainFunctions(category: string): Observable<any> {
    const req = {
      action: 'listfunctions',
      category: category
    };
    return this.http.post<any>(this.baseUrl + '/' + this.domainService, req, httpOptions);
  }

  executeDomain(func: string, domain: string, param: any) {
    const req = {
      action: 'execute',
      name: func,
      domain: domain,
      param: param
    };
    return this.http.post<any>(this.baseUrl + '/' + this.domainService, req, httpOptions);
  }

  /******* Reporting Service *********/
  
  listReports(category: string): Observable<any> {
    return this.http.get<any>(this.baseUrl + '/' + this.reportService + '?action=list&category=' + category, httpOptions);
  }


  /******* User Preference Service *********/

  getUserPreference(type: string, name: string): Observable<any> {
    const req = {
      action: 'get',
      type: type,
      name: name
    };
    return this.http.post<any>(this.baseUrl + '/' + this.userprefService, req, httpOptions);
  }

  putUserPreference(type: string, name: string, value: any): Observable<any> {
    const req = {
      action: 'put',
      type: type,
      name: name,
      value: value
    };
    return this.http.post<any>(this.baseUrl + '/' + this.userprefService, req, httpOptions);
  }

  /******* Signals *********/

  initSignalWebsocket() {
    if(this.signalService != null && this.signalService != "" && this.signalWebsocket == null) {
      this.signalWebsocket = webSocket(this.baseUrl.replace('http:', 'ws:').replace('https:', 'wss:') + '/' + this.signalService);
      this.initSignalWebsocketSubscribe();
      this.signalHeartbeat();
    }
  }

  initSignalWebsocketSubscribe() {
    this.signalWebsocket.asObservable().subscribe(
      (msg) => this.receiveSignal(msg),
      (err) => this.signalError(err)
    );
}

  receiveSignal(signal: String) {
    this.signalObservers.forEach((observer) => {
      observer.next(signal);
    });
  }

  signalError(error: String) {
    setTimeout(() => {this.initSignalWebsocketSubscribe()}, 1000);
  }

  
  signalHeartbeat() {
    this.signalWebsocket.next({action:"heartbeat"});
    setTimeout(() => {this.signalHeartbeat()}, 60000);
  }

  getSignalObservable() : Observable<any>  {
    return new Observable<any>((observer) => {
      this.signalObservers.push(observer);
    });
  }

  SignalWebsocketConnected() : Boolean {
    return this.signalWebsocket != null;
  }

  subscribeToSignal(req: any) {
    if(this.signalService != null) {
      if(this.signalWebsocket == null) {
        this.initSignalWebsocket();
      }
      if(this.signalWebsocket != null) {
        req["action"] = "subscribe";
        this.signalWebsocket.next(req);
      } 
    } 
  }

  clearSubscriptions() {
    if(this.signalService != null) {
      if(this.signalWebsocket == null) {
        this.initSignalWebsocket();
      }
      if(this.signalWebsocket != null) {
        this.signalWebsocket.next({"action":"unsubscribe"});
      } 
    } 
  }

  /********** chat ********/

  initChatWebsocket() {
    if(this.chatService != null && this.chatService != "" && this.chatWebsocket == null) {
      this.chatWebsocket = webSocket(this.baseUrl.replace('http:', 'ws:').replace('https:', 'wss:') + '/' + this.chatService);
      this.initChatWebsocketSubscribe();
      this.chatHeartbeat();
    }
  }

  initChatWebsocketSubscribe() {
    this.chatWebsocket.asObservable().subscribe(
      (msg) => this.receiveChat(msg),
      (err) => this.chatError(err)
    );
  }

  receiveChat(msg: any) {
    this.chatObservers.forEach((observer) => {
      observer.next(msg);
    });
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
    this.chatWebsocket.next(json);
  }

  getChatUsers() {
    let json: any = {
      action: "listusers"
    };
    this.chatWebsocket.next(json);
  }

  chatError(error: String) {
    setTimeout(() => {this.initChatWebsocketSubscribe()}, 1000);
  }

  chatHeartbeat() {
    this.chatWebsocket.next({action:"heartbeat"});
    setTimeout(() => {this.chatHeartbeat()}, 60000);
  }

  getChatObservable() : Observable<any>  {
    return new Observable<any>((observer) => {
      this.chatObservers.push(observer);
    });
  }

  chatWebsocketConnected() : Boolean {
    return this.chatWebsocket != null;
  }

  /********* Process **********/

  listAssignments(filter: any): Observable<any> {
    const req = {
      action: 'getassignments',
      filter: filter,
      viewdata:['objectname', 'uid']
    };
    return this.http.post<any>(this.baseUrl + '/' + this.processService, req, httpOptions);
  }

  getAssignmentCount(filter: any): Observable<any> {
    const req = {
      action: 'getassignmentcount',
      filter: filter
    };    
    return this.http.post<any>(this.baseUrl + '/' + this.processService, req, httpOptions);
  }

  actionAssignment(pid: string, action: string): Observable<any> {
    const req = {
      action: 'processaction',
      pid: pid,
      processaction: action
    };
    return this.http.post<any>(this.baseUrl + '/' + this.processService, req, httpOptions);
  }

  /********* Google Location **********/  

  
  predictAddresses(search: String, center: any, radius: number): Observable<any> {
    return new Observable<any>((observer) => {
      setTimeout(() => {
        var req = {
          input: search.toString(),
          location: center != null ? new google.maps.LatLng(center) : null,
          radius: radius
        }
        this.placesAutocompleteService.getQueryPredictions(req, (predictions, status) => {
          observer.next(predictions);
          observer.complete();
        });
        }, 1);
    })    
  }

}

 