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
import { LogService } from "app/services/log.service";
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
    //@ViewChild("rightdrawer") rightdrawer: MatSidenav;

    drawerShowing: string = null;
    drawerOpen: boolean = false;
    subscription: Subscription;
    
    title: string = "Welcome";
    showNLBox: boolean = false;
    //showDrawer: boolean = false;
    
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
    logService: LogService;

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
        this.logService = AppInjector.get(LogService);
     }
  
    ngOnInit() {
      this.subscription = this.events.subscribe((event) => {
        if(event == 'init' && this.initialView != null) {
          let iv = this.userprefService.getInitialView();
          if(iv != null) {
            this.navigateService.navigateTo({domain: iv.domain, view: iv.view})
          } else {
            this.navigateService.navigateTo({view: this.initialView})
          }
        }
      });
      this.navigateService.getNavigateObservable().subscribe((navdata) => this.navigated());
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
        } else if(value == 'logs') {
          this.logService.export();
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
      if(this.drawerOpen) {
        if(this.drawerShowing == type) {
          this.closeRightDrawer();
        } else {
          this.closeRightDrawer();
          setTimeout(() => {
            this.drawerShowing = type;
            this.drawerOpen = true;
          }, 500);
        }
      } else {
        this.drawerShowing = type;
        this.drawerOpen = true
      }
    }

    toggleNLBox() {
      this.showNLBox = !this.showNLBox;
    }

    closeRightDrawer() {
      this.drawerOpen = false;
    }

    navigated() {
      if(this.drawerOpen) {
        this.closeRightDrawer();
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