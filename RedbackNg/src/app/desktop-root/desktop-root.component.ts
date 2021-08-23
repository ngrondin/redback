import { Component, OnInit, Input, SimpleChanges, HostListener, TemplateRef, ViewChild } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { DomSanitizer } from '@angular/platform-browser';
import { ViewTarget } from 'app/datamodel';
import { ConfigService } from 'app/services/config.service';
import { UserprefService } from 'app/services/userpref.service';
import { ApiService } from 'app/services/api.service';
import { DragService } from 'app/services/drag.service';
import { ClientWSService } from 'app/services/clientws.service';
import { Observable } from 'rxjs';
import { Subscription } from 'rxjs/internal/Subscription';
import { DialogService } from 'app/services/dialog.service';
import { MatSidenav } from '@angular/material/sidenav';



@Component({
  selector: 'desktop-root',
  templateUrl: './desktop-root.component.html',
  styleUrls: ['./desktop-root.component.css']
})
export class DesktopRootComponent implements OnInit {
  @Input() apptitle : string;
  @Input() logo : string;
  @Input() username : string;
  @Input() userdisplay : string;
  @Input() initialView : string;
  @Input() version : string = 'default';
  @Input() events : Observable<string>;
  @ViewChild("rightdrawer") rightdrawer: MatSidenav;
  
  subscription: Subscription;
  viewTargetStack: ViewTarget[] = [];
  title: string = "Welcome";
  //rightDrawerIsOpen: boolean = false;
  rightDrawerShowing: string = null;
 
  constructor(
    private configService : ConfigService,
    public dragService: DragService,
    public dialogService: DialogService,
    public userprefService: UserprefService,
    private domSanitizer: DomSanitizer,
    private cookieService: CookieService,
    public apiService: ApiService,
    public clientWSServer: ClientWSService
  ) { }

  ngOnInit() {
    this.subscription = this.events.subscribe((event) => {
      if(event == 'init' && this.initialView != null) {
        let iv = this.userprefService.getInitialView();
        if(iv != null) {
          this.pushViewTarget(new ViewTarget(iv.domain, iv.view, null, {}, null), true);
        } else {
          this.pushViewTarget(new ViewTarget(null, this.initialView, null, {}, null), true);
        }
      }
    });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  get logoUrl() : any {
    return this.domSanitizer.bypassSecurityTrustResourceUrl(this.logo);
  }

  get currentTitle(): string {
    if(this.currentViewTarget != null) {
      return this.currentViewTarget.title;
    } else {
      return "Welcome";
    }
  }

  setTitle($event) {
    this.title = $event;
  }

  navigateTo($event) {
    let objectConfig: any = this.configService.objectsConfig[$event.object];
    let view: string = ($event.view != null ? $event.view : (objectConfig != null ? objectConfig.view : null));
    if(view != null) {
      let target = new ViewTarget($event.domain, view, $event.object, $event.filter, $event.search); 
      if(objectConfig != null && $event.filter != null && $event.filter[objectConfig.labelattribute] != null) {
        target.breadcrumbLabel = eval($event.filter[objectConfig.labelattribute]);
      }
      if($event.label != null) {
        target.additionalTitle = $event.label;
      }
      this.pushViewTarget(target, $event.reset);
      if(this.rightdrawer.opened) {
        this.rightdrawer.close();
      }
    }
  }

  backTo($event) {
    let i = this.viewTargetStack.indexOf($event);
    this.viewTargetStack.splice(i + 1);
  }

  pushViewTarget(target: ViewTarget, resetStack: boolean) {
    if(resetStack) {
      this.viewTargetStack = [];
    }
    this.viewTargetStack.push(target);
    this.userprefService.setCurrentView(target.view);
  }

  get currentViewTarget(): ViewTarget {
    if(this.viewTargetStack.length > 0) {
      return this.viewTargetStack[this.viewTargetStack.length - 1];
    } else {
      return null;
    }
  }

  logout() {
    this.cookieService.deleteAll('/');
    window.location.href = "/logout";
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

  closeRightDrawer() {
    this.rightdrawer.close();
  }

  @HostListener('mouseup', ['$event']) onMouseUp($event) {
    this.dragService.endDrag();
  }

  @HostListener('mousemove', ['$event']) onMouseMove($event) {
    this.dragService.move($event);
  }

  @HostListener('click', ['$event']) onClick($event) {
    this.dialogService.hideTooltip();
  }
}
