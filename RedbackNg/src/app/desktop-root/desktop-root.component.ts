import { Component, OnInit, Input, SimpleChanges, HostListener } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { DomSanitizer } from '@angular/platform-browser';
import { ViewTarget } from 'app/datamodel';
import { ConfigService } from 'app/services/config.service';
import { DragService } from 'app/rb-drag/drag.service';
import { UserprefService } from 'app/services/userpref.service';
import { ApiService } from 'app/services/api.service';



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
  viewTargetStack: ViewTarget[] = [];
  title: string = "Welcome";
 
  constructor(
    private configService : ConfigService,
    public dragService: DragService,
    public userprefService: UserprefService,
    private domSanitizer: DomSanitizer,
    private cookieService: CookieService,
    public apiService: ApiService
  ) { }

  ngOnInit() {
    if(this.initialView != null) {
      setTimeout(() => this.pushViewTarget(new ViewTarget(this.version, this.initialView, null, {}), true), 500);
    }
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
      let target = new ViewTarget(this.version, view, $event.object, $event.filter); 
      if(objectConfig != null && $event.filter != null && $event.filter[objectConfig.labelattribute] != null) {
        target.breadcrumbLabel = eval($event.filter[objectConfig.labelattribute]);
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

}
