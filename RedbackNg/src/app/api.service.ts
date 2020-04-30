import { Injectable } from '@angular/core';
import { Observable, of, Observer } from 'rxjs';
import { map } from 'rxjs/operators';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ObjectResp, RbObject } from './datamodel';
import { RequestOptionsArgs, RequestOptions } from '@angular/http';
import { CookieService } from 'ngx-cookie-service';
import { ToastrService } from 'ngx-toastr';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';

const httpOptions = {
  headers: new HttpHeaders().set("Content-Type", "application/json"),
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
  public processService: string;
  public signalService: string;
  public signalWebsocket: WebSocketSubject<any>;
  public signalObservers: Observer<String>[] = [];

  constructor(
    private http: HttpClient
  ) { 
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

  listObjects(name: string, filter: any, search: string): Observable<any> {
    const req = {
      action: 'list',
      object: name,
      filter: filter,
      options: {
        addrelated: true,
        addvalidation: true
      }
    };
    if(search != null)
      req['search'] = search;
    return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }

  listRelatedObjects(name: string, uid: string, attribute: string, filter: any, search: string): Observable<any> {
    const req = {
      action: 'list',
      object: name,
      uid: uid,
      attribute: attribute,
      filter: filter,
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
  
  aggregateObjects(name: string, filter: any, tuple: any, metrics: any): Observable<any> {
    const req = {
      action: 'aggregate',
      object: name,
      filter: filter,
      tuple: tuple,
      metrics: metrics,
      options: {
        addrelated: true
      }
    };
    return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }

  initSignalWebsocket() {
    if(this.signalService != null && this.signalService != "" && this.signalWebsocket == null) {
      this.signalWebsocket = webSocket(this.baseUrl.replace('http:', 'ws:').replace('https:', 'wss:') + '/' + this.signalService);
      this.initSignalWebsocketSubscribe();
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

  getSignalObservable() : Observable<any>  {
    return new Observable<any>((observer) => {
      this.signalObservers.push(observer);
    });
  }

  SignalWebsocketConnected() : Boolean {
    return this.signalWebsocket != null;
  }

  subscribeToSignal(signal: string) {
    if(this.signalService != null) {
      if(this.signalWebsocket == null) {
        this.initSignalWebsocket();
      }
      if(this.signalWebsocket != null) {
        this.signalWebsocket.next({subscribe:signal});
      } 
    } 
  }

  listAssignments(filter: any): Observable<any> {
    const req = {
      action: 'getassignments',
      filter: filter,
      viewdata:['objectname', 'uid']
    };
    return this.http.post<any>(this.baseUrl + '/' + this.processService, req, httpOptions);
  }

  getAssignmentCount(): Observable<any> {
    const req = {
      action: 'getassignmentcount'
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



  
  listFiles(object: string, uid: string): Observable<any> {
    return this.http.get<any>(this.baseUrl + '/' + this.fileService + '?object=' + object + '&uid=' + uid, httpOptions);
  }

}
