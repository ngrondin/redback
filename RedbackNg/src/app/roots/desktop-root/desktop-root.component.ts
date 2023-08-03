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



@Component({
  selector: 'desktop-root',
  templateUrl: './desktop-root.component.html',
  styleUrls: ['./desktop-root.component.css']
})
export class DesktopRootComponent extends AppRootComponent {
  @ViewChild("rightdrawer") rightdrawer: MatSidenav;

  rightDrawerShowing: string = null;
  
  constructor(
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
        setTimeout(() => this.toggleNotifications(), 500);
      } 
    } else {
      this.rightDrawerShowing = 'notifications';
      this.rightdrawer.open();
    }
  }


  navigated() {
    if(this.rightdrawer.opened) {
      this.rightdrawer.close();
    }
  }

}
