import { Component } from '@angular/core';
import { AppRootComponent } from '../abstract/app-root';
import { ChatService } from 'app/services/chat.service';
import { NotificationService } from 'app/services/notification.service';
import { ConfigService } from 'app/services/config.service';
import { LogService } from 'app/services/log.service';


@Component({
  selector: 'desktop-root',
  templateUrl: './desktop-root.component.html',
  styleUrls: ['./desktop-root.component.css']
})
export class DesktopRootComponent extends AppRootComponent {
  
  constructor(
    public chatService: ChatService,
    public notificationService: NotificationService,
  ) {
    super();
  }






}
