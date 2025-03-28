declare const google: any

import { Injectable } from '@angular/core';
import { Observable, Observer, Subscriber } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ClientWSService } from './clientws.service';
import { SecurityService } from './security.service';


const httpJSONOptions = {
  headers: new HttpHeaders()
    .set("Content-Type", "application/json")
    .set("firebus-timezone", Intl.DateTimeFormat().resolvedOptions().timeZone),
  withCredentials: true
};

const httpOptions = {
  headers: new HttpHeaders()
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
  //public domainService: string;
  public reportService: string;
  public processService: string;
  public userprefService: string;
  public useCSForAPI: boolean = false;
  public chatService: string; 
  public aiService: string;
  public placesAutocompleteService: any;

  constructor(
    private http: HttpClient,
    private clientWSService: ClientWSService,
    private securityService: SecurityService
  ) { 
    try {
      this.placesAutocompleteService = new google.maps.places.AutocompleteService();

    } catch(error) {
      console.error(error);
    }
  }


  private requestService(service: string, request: any, timeout?: number) {
    return new Observable<any>((observer) => {
      if(service != null && service != "") {
        this.securityService.checkToken().subscribe(() => {
          if(service == null || service == '') {
            observer.error("Undefined service");
          } else if(this.canStream()) {
            this.clientWSService.requestService(service, request, timeout).subscribe({
              next: (value) => observer.next(value),
              error: (err) => observer.error(err),
              complete: () => observer.complete()
            })
          } else {
            let headers = new HttpHeaders()
              .set("Content-Type", "application/json")
              .set("firebus-timezone", Intl.DateTimeFormat().resolvedOptions().timeZone);
            if(timeout != null) headers.set("firebus-timeout", timeout.toString());
            this.http.post<any>(this.baseUrl + '/' + service, request, {headers: headers, withCredentials: true}).subscribe({
              next: (value) => observer.next(value),
              error: (err) => observer.error(err),
              complete: () => observer.complete()
            });
          }
        });
      } else {
        observer.error("Service is undefined");
      }
    });
  }

  private requestStream(service: string, request: any, autoNext: boolean) {
    return new Observable<any>((observer) => {
      if(this.canStream() && service != null && service != "") {
        this.securityService.checkToken().subscribe({
          error: (err) => observer.error(err),
          complete: () => {
            this.clientWSService.requestStream(service, request, autoNext).subscribe({
              next: (value) => observer.next(value),
              error: (err) => observer.error(err),
              complete: () => observer.complete()   
            });
          }
        });
      } else {
        observer.error("Streams can only be requested when connected to the client servce");
      }
    });
  }

  private get(url: string) {
    return new Observable<any>((observer) => {
      this.securityService.checkToken().subscribe(() => {
        this.http.get<any>(url, httpJSONOptions).subscribe({
          next: (value) => observer.next(value),
          error: (err) => observer.error(err),
          complete: () => observer.complete()
        });
      });
    });
  }

  canStream() {
    return this.clientWSService.isConnected() && this.useCSForAPI;
  }


  /********* UI **********/  

  getPreviewUrl(url: string): Observable<any> {
    let reqUrl = this.baseUrl + '/' + this.uiService + "/tools/urlpreview?url=" + encodeURIComponent(url);
    return this.get(reqUrl);
  }

  getAppConfig(name: string) {
    let reqUrl = this.baseUrl + "/" + this.uiService + "/config/" + name;
    return this.get(reqUrl);
  }

  getView(name: string, domain: string) {
    let reqUrl = this.baseUrl + "/" + this.uiService + "/view/" + (domain != null ? domain + '/' : '')  + name;
    return this.get(reqUrl);
  }

 /********* Objects Service **********/  

  getObject(name: string, uid: string): Observable<any> {
    const req = {
      action: 'get',
      object: name,
      uid: uid,
      options: {
        addrelated: false,
        addvalidation: true
      }
    };
    return this.requestService(this.objectService, req);
  }

  listObjects(name: string, filter: any, search: string, sort: any, page: number, pageSize: number, addRelated: boolean): Observable<any> {
    const req = {
      action: 'list',
      object: name,
      filter: filter,
      sort: sort,
      page: page,
      pagesize: pageSize,
      options: {
        addrelated: addRelated,
        addvalidation: true
      }
    };
    if(search != null) req['search'] = search;
    return this.requestService(this.objectService, req);
  }

  streamObjects(name: string, filter: any, search: string, sort: any/*, addRelated: boolean*/): Observable<any> {
    const req = {
      action: 'list',
      object: name,
      filter: filter,
      sort: sort,
      chunksize: 250,
      advance: 1,
      options: {
        //addrelated: addRelated,
        addvalidation: true
      }
    };
    if(search != null) req['search'] = search;
    return this.requestStream(this.objectService, req, false);
  }

  listRelatedObjects(name: string, uid: string, attribute: string, filter: any, search: string, sort: any, addRelated: boolean): Observable<any> {
    const req = {
      action: 'list',
      object: name,
      uid: uid,
      attribute: attribute,
      filter: filter,
      sort: sort,
      options: {
        addrelated: addRelated,
        addvalidation: true
      }
    };
    if(search != null) req['search'] = search;
    return this.requestService(this.objectService, req);
  }

  listScripts(category: string): Observable<any> {
    const req = {
      action: 'listscripts',
      category: category
    };
    return this.requestService(this.objectService, req);
  }

  updateObject(name: string, uid: string, data: any) {
    const req = {
      action: 'update',
      object: name,
      uid: uid,
      data: data,
      options: {
        addrelated: false,
        addvalidation: true
      }
    };
    return this.requestService(this.objectService, req);
  }

  createObject(name: string, uid: string, data: any) {
    const req = {
      action: 'create',
      object: name,
      data: data,
      options: {
        addrelated: false,
        addvalidation: true
      }
    };
    if(uid != null) {
      req['uid'] = uid;
    }
    return this.requestService(this.objectService, req);
  }

  deleteObject(name: string, uid: string) {
    const req = {
      action: 'delete',
      object: name,
      uid: uid
    };
    return this.requestService(this.objectService, req);
  }

  countObjects(name: string, filter: any, search: string): Observable<any> {
    const req = {
      action: 'count',
      object: name,
      filter: filter,
    };
    if(search != null) req['search'] = search;
    return this.requestService(this.objectService, req);
  }  

  executeObject(name: string, uid: string, func: string, param?: any, timeout?: number) {
    const req = {
      action: 'execute',
      object: name,
      uid: uid,
      function: func,
      options: {
        addrelated: false,
        addvalidation: true
      }
    };
    if(param != null) req["param"] = param;
    return this.requestService(this.objectService, req, timeout);
  }
  
  executeGlobal(func: string, param: any, timeout?: number) {
    const req = {
      action: 'execute',
      function: func,
      param: param
    };
    return this.requestService(this.objectService, req, timeout);
  }

  aggregateObjects(name: string, filter: any, search: string, tuple: any, metrics: any, base: any, page: number = 0, pageSize: number = 50): Observable<any> {
    const req = {
      action: 'aggregate',
      object: name,
      filter: filter,
      search: search,
      tuple: tuple,
      metrics: metrics,
      page: page,
      pagesize: pageSize,
      options: {
        addrelated: true
      }
    };
    if(base != null) req["base"] = base;
    return this.requestService(this.objectService, req);
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
    return this.requestService(this.objectService, req);
  }

  objectMulti(reqs: any[]): Observable<any> {
    const req = {
      action: 'multi',
      multi: reqs
    };
    return this.requestService(this.objectService, req);
  }

  /******* Files *********/

  uploadFile(file: File, object: string, uid: string) : Observable<any> {
    if(this.canStream()) {
      return this.clientWSService.upload(file, object, uid);
    } else {
      let formData: FormData = new FormData();
      formData.append("mime", file.type);
      formData.append("filesize", file.size.toString());
      formData.append("file", file, file.name);
      if(object != null && uid != null) {
        formData.append("object", object);
        formData.append("uid", uid);
      }
      return this.http.post<any>(this.baseUrl + '/' + this.fileService, formData, httpOptions);  
    }
  }

  unlinkFile(fileuid: string, objectname: string, uid: string) : Observable<any> {
    const req = {
      action: 'unlink',
      fileuid: fileuid,
      object: objectname,
      uid: uid
    };
    const headers = new HttpHeaders().set('Content-Type', 'text/plain; charset=utf-8');
    return this.requestService(this.fileService, req);
  }

  listFiles(object: string, uid: string): Observable<any> {
    return this.requestService(this.fileService, {
      action: "list",
      object: object,
      uid: uid
    });
  }

  /******* Domain *********/

  /*listDomainFunctions(category: string): Observable<any> {
    const req = {
      action: 'listfunctions',
      category: category
    };
    return this.requestService(this.domainService, req);
  }

  executeDomain(func: string, domain: string, param: any, timeout?: number) {
    const req = {
      action: 'execute',
      name: func,
      domain: domain,
      param: param,
    };
    return this.requestService(this.domainService, req, timeout);
  }*/

  /******* Reporting Service *********/
  
  listReports(category: string): Observable<any> {
    return this.http.get<any>(this.baseUrl + '/' + this.reportService + '?action=list&category=' + category, httpJSONOptions);
  }


  /******* User Preference Service *********/

  getUserPreference(type: string, name: string): Observable<any> {
    const req = {
      action: 'get',
      type: type,
      name: name
    };
    return this.requestService(this.userprefService, req);
  }

  putUserPreference(type: string, name: string, value: any): Observable<any> {
    const req = {
      action: 'put',
      type: type,
      name: name,
      value: value
    };
    return this.requestService(this.userprefService, req);
  }


  /********* Process **********/

  listAssignments(filter: any, page: number, pageSize: number): Observable<any> {
    const req = {
      action: 'getassignments',
      filter: filter,
      viewdata:['objectname', 'uid'],
      page: page,
      pageSize: pageSize
    };
    return this.requestService(this.processService, req);
  }

  getAssignmentCount(filter: any): Observable<any> {
    const req = {
      action: 'getassignmentcount',
      filter: filter
    };
    return this.requestService(this.processService, req);
  }

  actionAssignment(pid: string, action: string): Observable<any> {
    return this.requestService(this.processService, {
      action: 'processaction',
      pid: pid,
      processaction: action
    });
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


  /********* Chat **********/  

  listChatUsers(): Observable<any> {
    return this.requestService(this.chatService, {
      action: 'listusers'
    });
  }

  listChats(): Observable<any> {
    return this.requestService(this.chatService, {
      action: 'listconversations'
    });
  }

  listChatMessages(chat: string): Observable<any> {
    return this.requestService(this.chatService, {
      action: 'listmessages',
      conversation: chat
    });
  }

  listAllUnreadChatMessages(): Observable<any> {
    return this.requestService(this.chatService, {
      action: 'listunreadmessages'
    });
  }

  createChat(name: string): Observable<any> {
    return this.requestService(this.chatService, {
      action: 'createconversation',
      name: name
    })
  }

  addUserToChat(chat: string, username: string):  Observable<any> {
    return this.requestService(this.chatService, {
      action: 'adduser',
      conversation: chat,
      username: username
    })
  }

  removeUserFromChat(chat: string, username: string):  Observable<any> {
    return this.requestService(this.chatService, {
      action: 'removeuser',
      conversation: chat,
      username: username
    })
  }

  sendChatMessage(chat:string, body: string): Observable<any> {
    return this.requestService(this.chatService, {
      action: 'sendmessage',
      conversation: chat,
      body: body
    })
  }

  markChatMessageAsRead(message: string): Observable<any> {
    return this.requestService(this.chatService, {
      action: 'markread',
      message: message
    })
  }


  /********* AI **********/  

  nlCommand(model: string, text: string, context: any): Observable<any> {
    return this.requestService(this.aiService, {
      action: 'nlcommand',
      model: model,
      text: text,
      context: context
    });
  }

  nlFeedback(model: string, command: string, sequence: string, points: number): Observable<any> {
    return this.requestService(this.aiService, {
      action: 'feedback',
      model: model,
      command: command,
      sequence: sequence,
      points: points
    });
  }
}

 