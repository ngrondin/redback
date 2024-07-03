import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observer, Subscriber } from 'rxjs';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({
  providedIn: 'root'
})
export class SecurityService {
  private tokenObservers: Observer<string>[] = [];
  private refreshRequesters: Observer<void>[] = [];
  
  public baseUrl: string;
  public refreshToken: string;
  public expiresAt: number;
  public accessToken: string;
  public refreshPath: string;

  constructor(
    private http: HttpClient,
  ) { 
    this.accessToken = localStorage.getItem("access_token");
    this.expiresAt = parseInt(localStorage.getItem("expires_at"));
    this.refreshToken = localStorage.getItem("refresh_token");
    this.refreshPath = localStorage.getItem("refresh_path");
  }

  public observeToken(): Observable<string> {
    return new Observable((observer) => {
      this.tokenObservers.push(observer);
    });
  }

  public checkToken() {
    return new Observable((observer) => {
      let now = (new Date()).getTime();
      if(this.expiresAt != null && now > this.expiresAt - (180000)) {
        this.refreshRequesters.push(observer);
        console.log("Access Token has expired");
        if(this.refreshRequesters.length == 1) {
          console.log("Refreshing tokens");
          this.http.get<any>(this.baseUrl + this.refreshPath, {headers:{"accept":"application/json"}}).subscribe({
            next: (value) => {
              this.accessToken = value.access_token;
              this.expiresAt = value.expires_at;
              this.refreshToken = value.refresh_token;
              this.refreshPath = value.refresh_path;
              localStorage.setItem("access_token", this.accessToken);
              localStorage.setItem("expires_at", this.expiresAt.toString());
              localStorage.setItem("refresh_token", this.refreshToken);
              localStorage.setItem("refresh_path", this.refreshPath);
              this.tokenObservers.forEach(o => o.next(this.accessToken));
              this.refreshRequesters.forEach(o => o.next())
              console.log("Access Token renewed: " + this.accessToken);
            },
            error: (err) => {
              this.refreshRequesters.forEach(o => o.error(err));
              this.refreshRequesters = [];
              console.error("Error refreshing access token: " + err.toString());
            },
            complete: () => {
              this.refreshRequesters.forEach(o => o.complete())
              this.refreshRequesters = [];
            }
          });
        } else {
          console.log("Tokens already refreshing");
        }
      } else {
        observer.next();
        observer.complete();
      }  
    });
  }
}
