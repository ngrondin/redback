import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { RbNotification } from 'app/datamodel';
import { NotificationService } from 'app/services/notification.service';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-notification-list',
  templateUrl: './rb-notification-list.component.html',
  styleUrls: ['./rb-notification-list.component.css']
})
export class RbNotificationListComponent implements OnInit {
  @Output() navigate: EventEmitter<any> = new EventEmitter();
  @Output() close: EventEmitter<any> = new EventEmitter();

  selectedGroup: String = null;
  
  constructor(
    private notificationService: NotificationService,
    private userprefService: UserprefService
  ) { 
  }
  
  ngOnInit(): void {
  }

  public get grouping(): string {
    return this.userprefService.getGlobalPreferenceValue("notifgroup");
  }

  public get groups() : any[] {
    if(this.grouping == "byaction") {
      return this.notificationService.topExceptionsByAction;
    } else if(this.grouping == "byobject") {
      return this.notificationService.topExceptionsByObject;
    } else {
      return this.notificationService.topExceptions;
    }
  }

  public closeNotifications() {
    this.close.emit();
  }

  public clickGroupHeader(groupname: String) {
    if(this.selectedGroup == groupname) {
      this.selectedGroup = null;
    } else {
      this.selectedGroup = groupname;
    }
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
