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

  baseUrl: string;

  constructor(
    private http: HttpClient,
    private cookieService : CookieService
  ) { 
    this.baseUrl = 'http://localhost';
    //this.cookieService.set('rbtoken','eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJlbWFpbCI6Im5ncm9uZGluNzhAZ21haWwuY29tIiwiZXhwIjoxOTIwNTExOTIyMDAwfQ.zQrN7sheh1PuO4fWru45dTPDtkLAqB9Q0WrwGO6yOeo', 1920511922000, "/", "http://localhost", false, 'Lax');
  }


  listObjects(name: string, filter: Object): Observable<any> {
    const req = {
      action: 'list',
      object: name,
      filter: filter,
      options: {
        addrelated: true,
        addvalidation: true
      }
    };
    return this.http.post<any>(this.baseUrl + '/rbos', req, httpOptions);
  }
}
