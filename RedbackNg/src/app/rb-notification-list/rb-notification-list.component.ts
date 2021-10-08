import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { RbNotification } from 'app/datamodel';
import { NotificationService } from 'app/services/notification.service';

@Component({
  selector: 'rb-notification-list',
  templateUrl: './rb-notification-list.component.html',
  styleUrls: ['./rb-notification-list.component.css']
})
export class RbNotificationListComponent implements OnInit {
  @Output() navigate: EventEmitter<any> = new EventEmitter();
  @Output() close: EventEmitter<any> = new EventEmitter();
  
  constructor(
    private notificationService: NotificationService
  ) { 
  }
  
  ngOnInit(): void {
  }

  public get list() : RbNotification[] {
    return this.notificationService.topExceptions;
  }

  public closeNotifications() {
    this.close.emit();
  }

  public selectNotification(notification: RbNotification) {
    if((notification.data.object != null || notification.data.objectname != null) && notification.data.uid != null) {
      this.navigate.emit({
        object : notification.data.object || notification.data.objectname,
        filter : {
          uid : "'" + notification.data.uid + "'"
        },
        reset : true
      });
    }
    if(notification.type == 'notification') {
      let firstAction = notification.actions[0];
      if(firstAction != null) {
        this.notificationService.actionNotification(notification, firstAction.action).subscribe();
      }
    }
  }
}
