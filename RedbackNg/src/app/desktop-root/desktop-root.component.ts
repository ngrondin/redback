import { Component, OnInit, Input, SimpleChanges, HostListener } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { ApiService } from 'app/api.service';
import { DomSanitizer } from '@angular/platform-browser';
import { DataService } from 'app/data.service';
import { RbObject } from 'app/datamodel';
import { ConfigService } from 'app/config.service';
import { DragService } from 'app/rb-drag/drag.service';

export class Target {
  view: string;
  version: string;
  type: string;
  filter: any;
  search: string;
  selectedObject: RbObject;
  title: string;
  _breadcrumbLabel: string;

  constructor(t: string, vs: string, v: string, f: any) {
    this.view = v;
    this.version = vs;
    this.type = t;
    this.filter = f;
    this.search = null;
    this.selectedObject = null;
    this.title = null;
  }

  get breadcrumbLabel(): string {
    if(this._breadcrumbLabel != null) {
      return this._breadcrumbLabel;
    } else {
      return this.title; 
    }
  }

  set breadcrumbLabel(v: string) {
    this._breadcrumbLabel = v;
  }
}

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
  @Input() menuView : string;
  @Input() version : string;
  menuTarget: Target;
  menuMode: string;
  menuWidth: number;
  viewTargetStack: Target[];
  title: string = "Welcome";
 
  constructor(
    private configService : ConfigService,
    public dragService: DragService,
    private domSanitizer: DomSanitizer
  ) { }

  ngOnInit() {
    this.menuMode = 'large';
    this.menuWidth = 251;
    if(this.version == null)
      this.version = 'default';
    if(this.viewTargetStack == null)
      this.viewTargetStack = [];
  }

  ngOnChanges(changes: SimpleChanges) {
    if("menuView" in changes) {
      this.menuTarget = new Target('menu', this.version, this.menuView, null);
        //url: this.apiService.baseUrl + '/' + this.apiService.uiService + '/menu/' + this.version + '/' + this.menuView,
    }
    if("initialView" in changes) {
      this.pushViewTarget(new Target('view', this.version, this.initialView, null), true);
    }
  }

  get logoUrl() : any {
    return this.domSanitizer.bypassSecurityTrustResourceUrl(this.logo);
  }

  setTitle($event) {
    this.title = $event;
  }

  navigateTo($event) {
    let objectConfig: any = this.configService.objectsConfig[$event.object];
    let view: string = ($event.view != null ? $event.view : (objectConfig != null ? objectConfig.view : null));
    if(view != null) {
      let target = new Target('view', this.version, view, $event.filter); 
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

  toggleMenuMode() {
    if(this.menuMode == 'large') {
      this.menuMode = 'small';
      this.menuWidth = 53;
    } else {
      this.menuMode = 'large';
      this.menuWidth = 251;
    }
  }

  pushViewTarget(target: Target, resetStack: boolean) {
    if(resetStack) {
      this.viewTargetStack = [];
    }
    this.viewTargetStack.push(target);
  }

  get currentViewTarget(): Target {
    if(this.viewTargetStack.length > 0) {
      return this.viewTargetStack[this.viewTargetStack.length - 1];
    } else {
      return null;
    }
  }

  logout() {

  }

  @HostListener('mouseup', ['$event']) onMouseUp($event) {
    this.dragService.endDrag();
  }

  @HostListener('mousemove', ['$event']) onMouseMove($event) {
    this.dragService.move($event);
  }
/*
  @HostListener('mouseout', ['$event']) onMouseOut($event) {
    this.dragService.endDrag();
  }
*/
}
