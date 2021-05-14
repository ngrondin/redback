declare const google: any

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ClientWSService } from './clientws.service';


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
  public domainService: string;
  public reportService: string;
  public processService: string;
  public userprefService: string;
  public useCSForAPI: boolean = false;
  public chatService: string; 
  public placesAutocompleteService: any;

  constructor(
    private http: HttpClient,
    private clientWSService: ClientWSService
  ) { 
    this.placesAutocompleteService = new google.maps.places.AutocompleteService();
  }

  private requestService(service: string, request: any) {
    if(this.clientWSService.isConnected() && this.useCSForAPI) {
      return this.clientWSService.request(service, request);
    } else {
      return this.http.post<any>(this.baseUrl + '/' + service, request, httpJSONOptions);
    }
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
    return this.requestService(this.objectService, req);
    //return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
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
    //return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
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
    //return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
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
    return this.requestService(this.objectService, req);
    //return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
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
    return this.requestService(this.objectService, req);
    //return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }

  deleteObject(name: string, uid: string) {
    const req = {
      action: 'delete',
      object: name,
      uid: uid
    };
    return this.requestService(this.objectService, req);
    //return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
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
    return this.requestService(this.objectService, req);
    //return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }
  
  executeGlobal(func: string, param: any) {
    const req = {
      action: 'execute',
      function: func,
      param: param
    };
    return this.requestService(this.objectService, req);
    //return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
  }

  aggregateObjects(name: string, filter: any, search: string, tuple: any, metrics: any, page: number = 0, pageSize: number = 50): Observable<any> {
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
    return this.requestService(this.objectService, req);
    //return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, httpOptions);
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
    //return this.http.post<any>(this.baseUrl + '/' + this.objectService, req, { headers, withCredentials: true, responseType: 'text' as 'json'});
  }

  /******* Files *********/

  uploadFile(file: File, object: string, uid: string) : Observable<any> {
    if(this.clientWSService.isConnected() && this.useCSForAPI) {
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

  listFiles(object: string, uid: string): Observable<any> {
    return this.requestService(this.fileService, {
      action: "list",
      object: object,
      uid: uid
    });
    //return this.http.get<any>(this.baseUrl + '/' + this.fileService + '?action=list&object=' + object + '&uid=' + uid, httpOptions);
  }

  /******* Domain *********/

  listDomainFunctions(category: string): Observable<any> {
    const req = {
      action: 'listfunctions',
      category: category
    };
    return this.requestService(this.domainService, req);
    //return this.http.post<any>(this.baseUrl + '/' + this.domainService, req, httpOptions);
  }

  executeDomain(func: string, domain: string, param: any) {
    const req = {
      action: 'execute',
      name: func,
      domain: domain,
      param: param
    };
    return this.requestService(this.domainService, req);
    //return this.http.post<any>(this.baseUrl + '/' + this.domainService, req, httpOptions);
  }

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
    //return this.http.post<any>(this.baseUrl + '/' + this.userprefService, req, httpOptions);
  }

  putUserPreference(type: string, name: string, value: any): Observable<any> {
    const req = {
      action: 'put',
      type: type,
      name: name,
      value: value
    };
    return this.requestService(this.userprefService, req);
    //return this.http.post<any>(this.baseUrl + '/' + this.userprefService, req, httpOptions);
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
    //return this.http.post<any>(this.baseUrl + '/' + this.processService, req, httpOptions);
  }

  getAssignmentCount(filter: any): Observable<any> {
    const req = {
      action: 'getassignmentcount',
      filter: filter
    };
    return this.requestService(this.processService, req);
    //return this.http.post<any>(this.baseUrl + '/' + this.processService, req, httpOptions);
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

}

 