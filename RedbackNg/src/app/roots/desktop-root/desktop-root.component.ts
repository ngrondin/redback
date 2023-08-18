import { Component, OnInit, Input, SimpleChanges, HostListener, TemplateRef, ViewChild } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { DomSanitizer } from '@angular/platform-browser';
import { ConfigService } from 'app/services/config.service';
import { UserprefService } from 'app/services/userpref.service';
import { ApiService } from 'app/services/api.service';
import { DragService } from 'app/services/drag.service';
import { ClientWSService } from 'app/services/clientws.service';
import { DialogService } from 'app/services/dialog.service';
import { MatSidenav } from '@angular/material/sidenav';
import { AppRootComponent } from '../abstract/app-root';
import { ChatService } from 'app/services/chat.service';
import { NotificationService } from 'app/services/notification.service';



@Component({
  selector: 'desktop-root',
  templateUrl: './desktop-root.component.html',
  styleUrls: ['./desktop-root.component.css']
})
export class DesktopRootComponent extends AppRootComponent {
  @ViewChild("rightdrawer") rightdrawer: MatSidenav;

  rightDrawerShowing: string = null;
  
  constructor(
    public chatService: ChatService,
    public notificationService: NotificationService
  ) {
    super();
  }

  closeRightDrawer() {
    this.rightdrawer.close();
  }

  toggleNotifications() {
    if(this.rightdrawer.opened) {
      this.rightdrawer.close();
      if(this.rightDrawerShowing != 'notifications') {
        setTimeout(() => this.toggleNotifications(), 250);
      } 
    } else {
      this.rightDrawerShowing = 'notifications';
      this.rightdrawer.open();
    }
  }

  toggleChat() {
    if(this.rightdrawer.opened) {
      this.rightdrawer.close();
      if(this.rightDrawerShowing != 'chat') {
        setTimeout(() => this.toggleChat(), 250);
      } 
    } else {
      this.rightDrawerShowing = 'chat';
      this.rightdrawer.open();
    }
  }

  navigated() {
    if(this.rightdrawer.opened) {
      this.rightdrawer.close();
    }
  }

}
