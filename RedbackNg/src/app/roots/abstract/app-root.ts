import { OnInit, Input, ViewChild, HostListener, Component, ViewContainerRef } from "@angular/core";
import { MatSidenav } from "@angular/material/sidenav";
import { DomSanitizer } from "@angular/platform-browser";
import { AppInjector } from "app/app.module";
import { ViewTarget } from "app/datamodel";
import { RbUsermenuComponent } from "app/rb-usermenu/rb-usermenu.component";
import { ApiService } from "app/services/api.service";
import { ClientWSService } from "app/services/clientws.service";
import { ConfigService } from "app/services/config.service";
import { DialogService } from "app/services/dialog.service";
import { DragService } from "app/services/drag.service";
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
    viewTargetStack: ViewTarget[] = [];
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
        window.redback.navigateTo = (event) => this.navigateTo(event);
     }
  
    ngOnInit() {
      this.nlActionService.navigateTo = (event: any) => this.navigateTo(event);
      this.subscription = this.events.subscribe((event) => {
        if(event == 'init' && this.initialView != null) {
          let iv = this.userprefService.getInitialView();
          if(iv != null) {
            this.pushViewTarget(new ViewTarget(iv.domain, iv.view, null, null, {}, null), true);
          } else {
            this.pushViewTarget(new ViewTarget(null, this.initialView, null, null, {}, null), true);
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
        let target = new ViewTarget($event.domain, view, $event.tab, $event.object, $event.filter, $event.search); 
        if(objectConfig != null && $event.filter != null && $event.filter[objectConfig.labelattribute] != null) {
          target.breadcrumbLabel = eval($event.filter[objectConfig.labelattribute]);
        }
        if($event.label != null) {
          target.additionalTitle = $event.label;
        }
        this.pushViewTarget(target, $event.reset);
        this.navigated();
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

    openUserMenu() {
      let popupComponentRef = this.popupService.openPopup(this.userMenuContainerRef, RbUsermenuComponent, {});
      popupComponentRef.instance.cancelled.subscribe(() => this.popupService.closePopup());
      popupComponentRef.instance.selected.subscribe(value => {
        if(value == 'logout') {
          this.logout();
        } else if(value == 'prefs') {
          this.toggleRightDrawer("prefs");
        } else {
          this.navigateTo({view: value});
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