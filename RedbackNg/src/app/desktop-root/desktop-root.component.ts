import { Component, OnInit, Input, SimpleChanges } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { ApiService } from 'app/api.service';
import { DomSanitizer } from '@angular/platform-browser';
import { DataService } from 'app/data.service';
import { RbObject } from 'app/datamodel';

export class Target {
  view: string;
  version: string;
  type: string;
  filter: any;
  search: string;
  selectedObject: RbObject;
  label: string;

  constructor(v: string, vs: string, t: string, f: any) {
    this.view = v;
    this.version = vs;
    this.type = t;
    this.filter = f;
    this.search = null;
    this.selectedObject = null;

    if(this.filter != null && this.filter.uid != null) {
      this.label = eval(this.filter.uid);
    } else {
      this.label = this.view;
    }
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
  @Input() objectViewMap : any;
  viewTitle: string;
  viewTarget: Target;
  menuTarget: Target;
  menuMode: string;
  menuWidth: number;
  viewTargetStack: Target[];
 
  constructor(
    private cookieService : CookieService,
    private apiService : ApiService,
    private domSanitizer: DomSanitizer
  ) { }

  ngOnInit() {
    this.menuMode = 'large';
    this.menuWidth = 251;
    if(this.version == null)
      this.version = 'default';
    if(this.objectViewMap == null)
      this.objectViewMap = {};
    if(this.viewTargetStack == null)
      this.viewTargetStack = [];
  }

  ngOnChanges(changes: SimpleChanges) {
    if("menuView" in changes) {
      this.menuTarget = new Target(this.menuView, this.version, 'menu', null);
        //url: this.apiService.baseUrl + '/' + this.apiService.uiService + '/menu/' + this.version + '/' + this.menuView,
    }
    if("initialView" in changes) {
      this.pushViewTarget(new Target(this.initialView, this.version, 'view', null), true);
    }
  }

  get logoUrl() : any {
    return this.domSanitizer.bypassSecurityTrustResourceUrl(this.logo);
  }

  navigateTo($event) {
    let view = ($event.view != null ? $event.view : this.objectViewMap[$event.object]);
    this.pushViewTarget(new Target(view, this.version, 'view', $event.filter), $event.reset);
  }

  backTo($event) {
    let i = this.viewTargetStack.indexOf($event);
    this.viewTargetStack.splice(i + 1);
    this.viewTarget = $event;
  }

  setTitle(title: string) {
    this.viewTitle = title;
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

  private pushViewTarget(target: Target, resetStack: boolean) {
    this.viewTarget = target;
    if(resetStack) {
      this.viewTargetStack = [];
    }
    this.viewTargetStack.push(this.viewTarget);
  }


}
