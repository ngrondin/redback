import { OnInit, Input, ViewChild, HostListener, Component, ViewContainerRef } from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { MatSidenav } from "@angular/material/sidenav";
import { DomSanitizer } from "@angular/platform-browser";
import { AppInjector } from "app/app.module";
import { RbAboutComponent } from "app/rb-about/rb-about.component";

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
    @Input() apptitle? : string;
    @Input() logo? : string;
    @Input() username? : string;
    @Input() userdisplay? : string;
    @Input() initialView? : string;
    @Input() version : string = 'default';
    @Input() events? : Observable<string>;

    @ViewChild('usermenubutton', { read: ViewContainerRef }) userMenuContainerRef?: ViewContainerRef;

    drawerShowing: string|null = null;
    drawerOpen: boolean = false;
    subscription?: Subscription;
    
    title: string = "Welcome";
    showNLBox: boolean = false;
    showWSConsole: boolean = false;
    
    configService : ConfigService;
    dragService: DragService;
    dialogService: DialogService;
    userprefService: UserprefService;
    domSanitizer: DomSanitizer;
    cookieService: CookieService;
    apiService: ApiService;
    clientWSService: ClientWSService;
    popupService: PopupService;
    nlActionService: NlactionService;
    navigateService: NavigateService;
    logService: LogService;
    dialog: MatDialog;

    connected: boolean = false;
    pendingRequests: boolean = false;

    constructor(
    ) {
        this.configService = AppInjector.get(ConfigService);
        this.dragService = AppInjector.get(DragService);
        this.dialogService = AppInjector.get(DialogService);
        this.userprefService = AppInjector.get(UserprefService);
        this.domSanitizer = AppInjector.get(DomSanitizer);
        this.cookieService = AppInjector.get(CookieService);
        this.apiService = AppInjector.get(ApiService);
        this.clientWSService = AppInjector.get(ClientWSService);
        this.popupService = AppInjector.get(PopupService);
        this.nlActionService = AppInjector.get(NlactionService);
        this.navigateService = AppInjector.get(NavigateService);
        this.logService = AppInjector.get(LogService);
        this.dialog = AppInjector.get(MatDialog);
     }
  
    ngOnInit() {
      if(this.events != null) {
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
      }
      this.clientWSService.getStateObservable().subscribe((state: any) => {
        this.connected = state.connected;
        this.pendingRequests = state.pendingrequests;
      });
      this.navigateService.getNavigateObservable().subscribe((navdata) => {
        this.navigated();
      });
    }
  
    ngOnDestroy() {
      this.subscription?.unsubscribe();
    }
  
    get logoUrl() : any {
      return this.logo != null ? this.domSanitizer.bypassSecurityTrustResourceUrl(this.logo) : null;
    }
  
    get currentTitle(): string {
      return this.navigateService.getCurrentTitle();
    }
  
    setTitle($event: any) {
      this.title = $event;
    }

    openUserMenu() {
      if(this.userMenuContainerRef != null) {
        let popupComponentRef = this.popupService.openPopup(this.userMenuContainerRef, RbUsermenuComponent, {});
        popupComponentRef.instance.cancelled.subscribe(() => this.popupService.closePopup());
        popupComponentRef.instance.selected.subscribe(value => {
          if(value == 'logout') {
            this.logout();
          } else if(value == 'prefs') {
            this.toggleRightDrawer("prefs");
          } else if(value == 'logs') {
            this.logService.export();
          } else if(value == 'about') {
            this.openAbout();
          } else {
            this.navigateService.navigateTo({view: value});
          }
        });
      }
    }
  
    logout() {
      this.cookieService.deleteAll('/');
      window.location.href = "/logout";
    }
  
    toggleRightDrawer(type: string) {
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

    toggleWSConsole() {
      this.showWSConsole = !this.showWSConsole;
    }

    closeRightDrawer() {
      this.drawerOpen = false;
    }

    navigated() {
      if(this.drawerOpen) {
        this.closeRightDrawer();
      }
    }

    openAbout() {
      this.dialog.open(RbAboutComponent, {
        data: {},
        autoFocus: false,
        restoreFocus: false
      });
    }
  
    @HostListener('mouseup', ['$event']) onMouseUp($event: any) {
      this.dragService.endDrag();
    }
  
    @HostListener('mousemove', ['$event']) onMouseMove($event: any) {
      this.dragService.move($event);
    }
  
    @HostListener('click', ['$event']) onClick($event: any) {
      this.dialogService.hideTooltip();
    }
  }