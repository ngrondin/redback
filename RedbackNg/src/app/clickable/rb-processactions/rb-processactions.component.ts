import { Component, Input } from '@angular/core';
import { RbNotification, RbNotificationAction, RbObject } from 'app/datamodel';
import { NotificationService } from 'app/services/notification.service';
import { Subscription } from 'rxjs';
import { RbDataButtonComponent } from '../abstract/rb-databutton';

@Component({
  selector: 'rb-processactions',
  templateUrl: '../rb-button/rb-button.html',
  styleUrls: ['../rb-button/rb-button.css']
})
export class RbProcessactionsComponent extends RbDataButtonComponent  {
  @Input('round') round: boolean = false;
  @Input('hideonempty') hideonempty: boolean = false;

  notification: RbNotification;
  notificationRetreived: boolean = false;
  subscription: Subscription;

  constructor(
    private notificationService: NotificationService,
  ) {
    super();
  }

  dataObserverInit() {
    this.subscription = this.notificationService.getObservable().subscribe(notif => this.onNotificationEvent(notif));
  }

  dataObserverDestroy() {
    this.subscription.unsubscribe();
  }

  onDatasetEvent(event: any) {
    if(event.event == 'select') {
      this.getNotification();
    }
  }

  onActivationEvent(event: any) {
    if(this.active == true) {
      this.getNotification();
    }
  }

  onNotificationEvent(event: any) {
    if(event.type = 'notification') {
      if(this.rbObject != null && event.notification.data != null && this.rbObject.objectname == event.notification.data.objectname && this.rbObject.uid == event.notification.data.uid) {
        this.notification = event.notification;
        this.notificationRetreived = true;
      }
    } else if(event.type == 'completion') {
      if(this.notification === event.notification) {
        this.notification = null;
        this.notificationRetreived = false;
      }
    }
  }

  get actions() : RbNotificationAction[] {
    return this.notification != null ? this.notification.actions : []
  }

  get message() : string {
    return this.notification != null ? this.notification.message : "No actions";
  }

  get showround() : boolean {
    return this.round;
  }

  get showstandard() : boolean {
    return !this.round && this.notification == null && !this.hideonempty;
  }

  get showfocus() : boolean {
    return !this.round && this.notification != null;
  }

  click() {

  }
  
  activate() {
  }

  deactivate() {
  }

  getNotification() {
    if(this.rbObject != null) {
      this.notificationService.getNotificationFor(this.rbObject.objectname, this.rbObject.uid).subscribe(notif => {
        this.notification = notif;
        this.notificationRetreived = true;
      });
    }
  }

  clickAction(action: any) {
    let notif = this.notification;
    this.notificationService.actionNotification(this.notification, action.action, action.confirm).subscribe(resp => {
      if(this.notification === notif) {
        this.notification = null;
      }
    });
  }

  receiveActionResponse(resp: any) {

  }
}
