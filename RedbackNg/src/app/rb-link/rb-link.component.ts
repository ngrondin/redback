import { Component, OnInit, Output, EventEmitter, Input, HostBinding } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { NavigateService } from 'app/services/navigate.service';

@Component({
  selector: 'rb-link',
  templateUrl: './rb-link.component.html',
  styleUrls: ['./rb-link.component.css']
})
export class RbLinkComponent extends RbDataObserverComponent {
  @Input('attribute') attribute: string;
  @Input('view') view: string;
  @Input('margin') margin: boolean = true;
  @Input('filtersingleobject') filtersingleobject: boolean = true;

  @HostBinding('style.margin-top.vw') get topmargin() { return this.margin ? 1.25 : 0.5; }
  //@HostBinding('style.margin-bottom.vw') get bottommargin() { return this.margin ? 0.55 : 0; }
    
  constructor(
    private navigateService: NavigateService
  ) {
    super();
  }

  dataObserverInit() {
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
  }

  onActivationEvent(state: boolean) {
  }

  public navigateTo() {
    if(this.rbObject != null && this.attribute != null) {
      let event = {};
      let objectuid = null;
      if(this.attribute == 'uid') {
        event['objectname'] = this.rbObject.objectname;
        objectuid = this.rbObject.uid;
      } else {
        let related = this.rbObject.getRelated(this.attribute);
        if(related != null) {
          event['objectname'] = related.objectname;
          objectuid = related.uid;
        } else {
          let relatedUid = this.rbObject.get(this.attribute);
          objectuid = relatedUid;
        }
      }
      if(this.filtersingleobject) {
        event["filter"] = {uid: "'" + objectuid + "'"}
      } else {
        event["select"] = {uid: objectuid}
      }
      if(this.view != null) {
        event['view'] = this.view;
      } 
      this.navigateService.navigateTo(event);
    }
  }
}
