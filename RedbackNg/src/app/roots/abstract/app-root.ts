import { OnInit, Input, ViewChild, HostListener, Component, ViewContainerRef } from "@angular/core";
import { MatSidenav } from "@angular/material/sidenav";
import { DomSanitizer } from "@angular/platform-browser";
import { AppInjector } from "app/app.module";

import { RbUsermenuComponent } from "app/rb-usermenu/rb-usermenu.component";
import { ApiService } from "app/services/api.service";
import { ClientWSService } from "app/services/clientws.service";
import { ConfigService } from "app/services/config.service";
import { DialogService } from "app/services/dialog.service";
import { DragService } from "app/services/drag.service";
import { NavigateService } from "app/services/navigate.service";
import { NlactionService } from "app/services/nlaction.service";
import { PopupService } from "app/services/popup.service";
import { UserprefService } from "app/services/userpref.service";
import { CookieService } from "ngx-cookie-service";
import { Observable, Subscription } from "rxjs";

@Component({template: ''})
export abstract class AppRootComponent implements OnInit {
    @Input() apptitle : string;
    @Input() logo : string;
    @Input() username : string;
    @Input() userdisplay : string;
    @Input() initialView : string;
    @Input() version : string = 'default';
    @Input() events : Observable<string>;

    @ViewChild('usermenubutton', { read: ViewContainerRef }) userMenuContainerRef: ViewContainerRef;
    @ViewChild("rightdrawer") rightdrawer: MatSidenav;

    rightDrawerShowing: string = null;
    subscription: Subscription;
    
    title: string = "Welcome";
    showNLBox: boolean = false;
    
    configService : ConfigService;
    dragService: DragService;
    dialogService: DialogService;
    userprefService: UserprefService;
    domSanitizer: DomSanitizer;
    cookieService: CookieService;
    apiService: ApiService;
    clientWSServer: ClientWSService;
    popupService: PopupService;
    nlActionService: NlactionService;
    navigateService: NavigateService;

    constructor(
    ) {
        this.configService = AppInjector.get(ConfigService);
        this.dragService = AppInjector.get(DragService);
        this.dialogService = AppInjector.get(DialogService);
        this.userprefService = AppInjector.get(UserprefService);
        this.domSanitizer = AppInjector.get(DomSanitizer);
        this.cookieService = AppInjector.get(CookieService);
        this.apiService = AppInjector.get(ApiService);
        this.clientWSServer = AppInjector.get(ClientWSService);
        this.popupService = AppInjector.get(PopupService);
        this.nlActionService = AppInjector.get(NlactionService);
        this.navigateService = AppInjector.get(NavigateService);
        
     }
  
    ngOnInit() {
      //this.nlActionService.navigateTo = (event: any) => this.navigateTo(event);
      this.subscription = this.events.subscribe((event) => {
        if(event == 'init' && this.initialView != null) {
          let iv = this.userprefService.getInitialView();
          if(iv != null) {
            this.navigateService.navigateTo({domain: iv.domain, view: iv.view})
            //this.pushViewTarget(new ViewTarget(iv.domain, iv.view, null, null, {}, null), true);
          } else {
            this.navigateService.navigateTo({view: this.initialView})
            //this.pushViewTarget(new ViewTarget(null, this.initialView, null, null, {}, null), true);
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
      let curNavData = this.navigateService.getCurrentNavigateData("default");
      return curNavData != null ? curNavData.title : "Welcome";
    }
  
    setTitle($event) {
      this.title = $event;
    }

    openUserMenu() {
      let popupComponentRef = this.popupService.openPopup(this.userMenuContainerRef, RbUsermenuComponent, {});
      popupComponentRef.instance.cancelled.subscribe(() => this.popupService.closePopup());
      popupComponentRef.instance.selected.subscribe(value => {
        if(value == 'logout') {
          this.logout();
        } else if(value == 'prefs') {
          this.toggleRightDrawer("prefs");
        } else {
          this.navigateService.navigateTo({view: value});
        }
      });
    }
  
    logout() {
      this.cookieService.deleteAll('/');
      window.location.href = "/logout";
    }
  
    toggleRightDrawer(type) {
      if(this.rightdrawer.opened) {
        this.rightdrawer.close();
        if(this.rightDrawerShowing != type) {
          setTimeout(() => this.toggleRightDrawer(type), 250);
        } 
      } else {
        this.rightDrawerShowing = type;
        this.rightdrawer.open();
      }
    }

    toggleNLBox() {
      this.showNLBox = !this.showNLBox;
    }

    closeRightDrawer() {
      this.rightdrawer.close();
    }

    navigated() {
      if(this.rightdrawer.opened) {
        this.rightdrawer.close();
      }
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