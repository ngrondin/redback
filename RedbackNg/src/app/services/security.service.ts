import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom, Observer, Subscriber } from 'rxjs';
import { Observable } from 'rxjs/internal/Observable';
import { LogService } from './log.service';

@Injectable({
  providedIn: 'root'
})
export class SecurityService {
  private tokenObservers: Observer<string>[] = [];
  private refreshRequesters: Observer<void>[] = [];
  
  public baseUrl!: string;
  public expiresAt?: number;
  public accessToken?: string;
  public refreshPath?: string;

  constructor(
    private http: HttpClient,
    private logService: LogService
  ) {  }

  public observeToken(): Observable<string> {
    return new Observable((observer) => {
      this.tokenObservers.push(observer);
    });
  }

  public get initiated(): boolean {
    return this.accessToken != null && this.expiresAt != null && this.refreshPath != null;
  }

  public get expired(): boolean {
    return this.initiated && (new Date()).getTime() > (this.expiresAt! - 180000);
  }

  public checkToken(): Observable<void> {
    return new Observable<void>((observer) => {
      if(!this.initiated || this.expired) {
        this.refreshRequesters.push(observer);
        if(this.refreshRequesters.length == 1) {
          this._checkToken();
        }
      } else {
        observer.next();
        observer.complete();
      }  
    });
  }

  private async _checkToken() {
    console.log("Checking token, v2");
    try {
      if(!this.initiated) {
        console.log("Retrieving token at " + this.baseUrl + "/check");
        let resp = await firstValueFrom(this.http.get<any>(this.baseUrl + "/check", {headers:{"accept":"application/json"}}));
        if(resp.error != null) throw resp.error;
        this.accessToken = resp.access_token;
        this.expiresAt = resp.expires_at;
        this.refreshPath = resp.refresh_path;
        console.log("Token retrieved");
      }

      if(this.expired) {
        console.log("Refreshing token at " + this.baseUrl + this.refreshPath);
        let resp = await firstValueFrom(this.http.get<any>(this.baseUrl + this.refreshPath, {headers:{"accept":"application/json"}}));
        this.accessToken = resp.access_token;
        this.expiresAt = resp.expires_at;
        this.refreshPath = resp.refresh_path;
        if(this.accessToken != null) {
          this.tokenObservers.forEach(o => o.next(this.accessToken!));
          console.log("Token refreshed");
        }
      }
      this.refreshRequesters.forEach(o => o.next());
      this.refreshRequesters.forEach(o => o.complete());
      this.refreshRequesters = [];      
    } catch(error) {
      console.log(error);
      this.logout();
    }
    console.log("Checking complete");
  }

  public logout() {
    window.location.href = this.baseUrl + this.refreshPath + "?action=invalidate";
  }
}
