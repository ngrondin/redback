import { Component, OnInit, Input } from '@angular/core';
import { RbNotification, RbNotificationAction, RbObject } from 'app/datamodel';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { NotificationService } from 'app/services/notification.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'rb-processactions',
  templateUrl: './rb-processactions.component.html',
  styleUrls: ['./rb-processactions.component.css']
})
export class RbProcessactionsComponent extends RbDataObserverComponent  {
  @Input('round') round: boolean = false;
  @Input('hideonempty') hideonempty: boolean = false;

  notification: RbNotification;
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
    if(event == 'select') {
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
      let notification = event.notification;
      if(this.rbObject != null && notification.data != null && this.rbObject.objectname == notification.data.objectname && this.rbObject.uid == notification.data.uid) {
        this.notification = notification;
      }
    } else if(event.type == 'completion') {
      if(this.notification != null && this.notification.process == event.process && this.notification.pid == event.pid && this.notification.code == event.code) {
        this.notification = null;
      }
    }
  }

  get rbObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : null;
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

  activate() {
  }

  deactivate() {
  }

  getNotification() {
    if(this.rbObject != null) {
      this.notificationService.getNotificationFor(this.rbObject.objectname, this.rbObject.uid).subscribe(notif => this.notification = notif);
    }
  }

  clickAction(action: string) {
    let notif = this.notification;
    this.notificationService.actionNotification(this.notification, action).subscribe(resp => {
      if(this.notification === notif) {
        this.notification = null;
      }
    });
  }

  receiveActionResponse(resp: any) {

  }
}
