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
    let evt = {
      object : (notification.data.object != null ? notification.data.object : (notification.data.objectname != null ? notification.data.objectname : null)),
      filter : {
        uid : "'" + notification.data.uid + "'"
      },
      reset : true
    }
    this.navigate.emit(evt);
  }
}
