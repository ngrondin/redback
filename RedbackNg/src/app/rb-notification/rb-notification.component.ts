import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { RbNotification } from 'app/datamodel';
import { NotificationService } from 'app/services/notification.service';

@Component({
  selector: 'rb-notification',
  templateUrl: './rb-notification.component.html',
  styleUrls: ['./rb-notification.component.css']
})
export class RbNotificationComponent implements OnInit {

  @Output() navigate: EventEmitter<any> = new EventEmitter();

  constructor(
    private notificationService: NotificationService
  ) { 
  }

  ngOnInit() {
  }

  public get list() : RbNotification[] {
    return this.notificationService.topExceptions;
  }

  public get count() : number {
    return this.notificationService.exceptionCount;
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
