import { Component, OnInit, Input, SimpleChanges, HostListener } from '@angular/core';
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
  subscription: Subscription;
  viewTargetStack: ViewTarget[] = [];
  title: string = "Welcome";
 
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
        this.pushViewTarget(new ViewTarget(this.version, this.initialView, null, {}, null), true);
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
      let target = new ViewTarget(this.version, view, $event.object, $event.filter, $event.search); 
      if(objectConfig != null && $event.filter != null && $event.filter[objectConfig.labelattribute] != null) {
        target.breadcrumbLabel = eval($event.filter[objectConfig.labelattribute]);
      }
      if($event.label != null) {
        target.additionalTitle = $event.label;
      }
      this.pushViewTarget(target, $event.reset);
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
