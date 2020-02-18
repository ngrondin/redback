import { Component, OnInit, Input, SimpleChanges } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { ApiService } from 'app/api.service';
import { DomSanitizer } from '@angular/platform-browser';
import { DataService } from 'app/data.service';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'desktop-root',
  templateUrl: './desktop-root.component.html',
  styleUrls: ['./desktop-root.component.css']
})
export class DesktopRootComponent implements OnInit {
  @Input() title : string;
  @Input() logo : string;
  @Input() username : string;
  @Input() userdisplay : string;
  @Input() initialView : string;
  @Input() menuView : string;
  @Input() version : string;
  @Input() objectViewMap : any;
  viewTitle: string;
  viewTarget: any;
  menuTarget: any;
  viewTargetStack: any[];
 
  constructor(
    private cookieService : CookieService,
    private apiService : ApiService,
    private domSanitizer: DomSanitizer
  ) { }

  ngOnInit() {
    if(this.version == null)
      this.version = 'default';
    if(this.objectViewMap == null)
      this.objectViewMap = {};
    if(this.viewTargetStack == null)
      this.viewTargetStack = [];
  }

  ngOnChanges(changes: SimpleChanges) {
    if("menuView" in changes) {
      this.menuTarget = {
        url: this.apiService.baseUrl + '/' + this.apiService.uiService + '/menu/' + this.version + '/' + this.menuView,
        userfilter: null
      };
    }
    if("initialView" in changes) {
      this.setViewTarget(this.initialView, null, true);
    }
  }

  get logoUrl() : any {
    return this.domSanitizer.bypassSecurityTrustResourceUrl(this.logo);
  }

  navigateTo($event) {
    if($event.view != null) {
      this.setViewTarget($event.view, $event.filter, $event.reset);
    } else if($event.object != null) {
      this.setViewTarget(this.objectViewMap[$event.object], $event.filter, $event.reset);
    }
  }

  backTo($event) {
    let i = this.viewTargetStack.indexOf($event);
    this.viewTargetStack.splice(i + 1);
    this.viewTarget = $event;
  }

  setTitle(title: string) {
    this.viewTitle = title;
  }

  setViewTarget(view: string, filter: any, resetStack: boolean) {
    this.viewTarget = {
      url: this.apiService.baseUrl + '/' + this.apiService.uiService + '/view/' + this.version + '/' + view,
      userfilter: filter
    };
    if(filter != null && filter.uid != null) {
      this.viewTarget.label = eval(filter.uid);
    } else {
      this.viewTarget.label = view;
    }
    if(resetStack) {
      this.viewTargetStack = [];
    }
    this.viewTargetStack.push(this.viewTarget);
  }


}
