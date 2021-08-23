import { EventEmitter, Output } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { NotificationService } from 'app/services/notification.service';

@Component({
  selector: 'rb-notification-bell',
  templateUrl: './rb-notification-bell.component.html',
  styleUrls: ['./rb-notification-bell.component.css']
})
export class RbNotificationBellComponent implements OnInit {

  constructor(
    private notificationService: NotificationService
  ) { 
  }

  ngOnInit(): void {
  }

  public get count() : number {
    return this.notificationService.exceptionCount;
  }


}
