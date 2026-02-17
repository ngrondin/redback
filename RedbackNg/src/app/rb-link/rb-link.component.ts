import { Component, OnInit, Output, EventEmitter, Input, HostBinding } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { NavigateEvent, RbObject } from 'app/datamodel';
import { LinkConfig } from 'app/helpers';
import { NavigateService } from 'app/services/navigate.service';

@Component({
  selector: 'rb-link',
  templateUrl: './rb-link.component.html',
  styleUrls: ['./rb-link.component.css']
})
export class RbLinkComponent extends RbDataObserverComponent {
  @Input('attribute') attribute: string;
  @Input('datatargets') datatargets: any[];
  @Input('view') view: string;
  @Input('margin') margin: boolean = true;
  @Input('filtersingleobject') filtersingleobject: boolean = true;

  @HostBinding('style.margin-top') get topmargin() { return this.margin ? 'min(1.144vw, 22px)' : '0px'; }
    
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
    if(this.rbObject != null) {
      let cfg = {
        view: this.view,
        datatargets: this.datatargets
      };
      if(this.datatargets == null) {
        let objectname = this.rbObject.objectname;
        let objectuid = this.rbObject.uid;
        if(this.attribute != null) {
          let related = this.rbObject.getRelated(this.attribute);
          if(related != null) {
            objectname = related.objectname;
            objectuid = related.uid;
          } else {
            objectuid = this.rbObject.get(this.attribute);
          }
        }
        if(this.filtersingleobject) {
          cfg.datatargets = [{objectname: objectname, filter: {uid: "'" + objectuid + "'"}}];
        } else {
          cfg.datatargets = [{objectname: objectname, select: {uid: objectuid}}];
        }
      }
      let linkcfg = new LinkConfig(cfg);
      let event = linkcfg.getNavigationEvent(this.rbObject, this.dataset, {});
      this.navigateService.navigateTo(event);
    }
  }
}
