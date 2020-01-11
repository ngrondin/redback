import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ObjectResp, RbObject } from './datamodel';
import { RequestOptionsArgs, RequestOptions } from '@angular/http';
import { CookieService } from 'ngx-cookie-service';

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
  public processService: string;

  constructor(
    private http: HttpClient,
    private cookieService : CookieService
  ) { 
    //this.baseUrl = 'http://localhost';
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
    return this.http.post<any>(this.baseUrl + '/rbos', req, httpOptions);
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
    return this.http.post<any>(this.baseUrl + '/rbos', req, httpOptions);
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
    return this.http.post<any>(this.baseUrl + '/rbos', req, httpOptions);
  }
}
