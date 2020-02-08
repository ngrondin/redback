import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ObjectResp, RbObject } from './datamodel';
import { RequestOptionsArgs, RequestOptions } from '@angular/http';
import { CookieService } from 'ngx-cookie-service';
import { ToastrService } from 'ngx-toastr';

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

  constructor(
    private http: HttpClient,
    private cookieService : CookieService,
    private toastr: ToastrService
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

  createObject(name: string, data: any) {
    const req = {
      action: 'create',
      object: name,
      data: data,
      options: {
        addrelated: true,
        addvalidation: true
      }
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

  listAssignments(filter: any): Observable<any> {
    const req = {
      action: 'getassignments',
      filter: filter,
      viewdata:['objectname', 'uid']
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
