import { ApplicationInitStatus, Injectable } from '@angular/core';
import { RbNotification } from 'app/datamodel';
import { Observer } from 'rxjs';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ClientWSService } from './clientws.service';
import { DataService } from './data.service';
import { ErrorService } from './error.service';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  notifications: RbNotification[] = [];
  exceptionCount: number;
  topExceptions: RbNotification[] = [];
  page: number;
  pageSize: number = 500;
  lastReceived: Date;
  private observers: Observer<any>[] = [];
  private loadObsever: Observer<null>;

  constructor(
    private apiService: ApiService,
    private clientWSService: ClientWSService,
    private dataService: DataService,
    private errorService: ErrorService
  ) {
    this.clientWSService.getNotificationObservable().subscribe(
      json => {
        if(json.completed == true) {
          this.receiveCompletion(json);
        } else {
          this.receiveNotification(json);
        }
        this.calcStats();
      }
    );
    this.clientWSService.getStateObservable().subscribe(state => this.onClientConnection(state));
  }

  onClientConnection(state: boolean) {
    if(state == true) {
      if(this.lastReceived != null) {
        var timeSinceLastReceived = (new Date()).getTime() - this.lastReceived.getTime();
        if(timeSinceLastReceived > 5000) {
          this.load();
        }
      }
    }
  }

  public getObservable() : Observable<any>  {
    return new Observable<any>((observer) => {
      this.observers.push(observer);
    });
  }

  public load() : Observable<null> {
    this.notifications = [];
    this.page = 0;
    this.fetchNextPage();
    return new Observable<null>((observer) => this.loadObsever = observer);
  }

  private fetchNextPage() {
    this.apiService.listAssignments({}, this.page, this.pageSize).subscribe(
      resp => {
        for(let item of resp.result) {
          this.receiveNotification(item);
        }
        if(resp.result.length >= this.pageSize) {
          this.fetchNextPage();
        } else {
          this.loadObsever.next(null);
          this.loadObsever.complete();
          this.calcStats();
        }
      },
      error => {
        this.errorService.receiveHttpError(error)
      }
    );
    this.page++;
  }

  private receiveNotification(json: any) : RbNotification {
    let notif: RbNotification = new RbNotification(json, this);
    this.removeAllProcessNotifications(notif.pid);
    this.notifications.push(notif);
    //console.log('new notification: ' + notif.code + " " + notif.pid);
    this.publish("notification", notif);
    this.lastReceived = new Date();
    return notif;
  }

  private receiveCompletion(json: any) {
    let sub = this.notifications.filter(item => item.process == json.process && item.pid == json.pid && item.code == json.code);
    for(let notif of sub) {
      //console.log('comp notification: ' + notif.code + " " + notif.pid);
      this.publish("completion", notif);
      this.removeAllProcessNotifications(notif.pid);
      this.lastReceived = new Date();
    }
  }

  private calcStats() {
    this.topExceptions = this.notifications.filter(item => item.type == 'exception').slice(0, 100);
    this.exceptionCount = this.notifications.filter(item => item.type == 'exception').length;
  }

  private publish(type: string, notif: RbNotification) {
    this.observers.forEach((observer) => {
      observer.next({type:type, notification: notif});
    }); 
  }

  private removeAllProcessNotifications(pid: string) {
    this.notifications.filter(notif => notif.pid == pid).forEach(notif => this.removeNotification(notif));
  }

  private removeNotification(notif: RbNotification) {
    var i = this.notifications.indexOf(notif);
    if(i > -1) {
      //console.log("remove notification: " + notif.code + " " + notif.pid);
      this.notifications.splice(i, 1);
    }
  }

  public getNotificationFor(objectname: string, uid: string) : Observable<RbNotification> {
    const obs = new Observable<RbNotification>((observer) => {
      let sub = this.notifications.filter(item => item.data != null && item.data.objectname == objectname && item.data.uid == uid);
      if(sub.length > 0) {
        observer.next(sub[0]);
        observer.complete();
      } else {
        this.apiService.listAssignments({"data.objectname": objectname, "data.uid": uid}, 0, 50).subscribe(resp => {
          if(resp.result.length > 0) {
            let notif = this.receiveNotification(resp.result[0]);
            this.calcStats();
            observer.next(notif);
            observer.complete();
          } else {
            observer.next(null);
            observer.complete();
          }
        });
      }
    });
    return obs;
  }

  actionNotification(notification: RbNotification, action: string): Observable<null> {
    const obs = new Observable<null>((observer) => {
      this.apiService.actionAssignment(notification.pid, action).subscribe(
        resp => {
          if(resp != null && resp.rbobjectupdate != null && resp.rbobjectupdate.length > 0 && !this.clientWSService.isConnected()) {
            for(let row of resp.rbobjectupdate) {
              this.dataService.getServerObject(row.objectname, row.uid).subscribe(resp => {});
            }
          }
          this.removeNotification(notification);
          this.calcStats();
          observer.next(null);
          observer.complete();
        },
        error => {
          this.errorService.receiveHttpError(error);
          observer.error(error);
        }
      )
    });
    return obs;
  }
}
