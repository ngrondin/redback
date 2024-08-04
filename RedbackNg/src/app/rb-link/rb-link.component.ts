import { Component, OnInit, Output, EventEmitter, Input, HostBinding } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
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

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
  }

  public navigateTo() {
    if(this.rbObject != null && this.attribute != null) {
      let target = {};
      if(this.attribute == 'uid') {
        target['objectname'] = this.rbObject.objectname;
        target['filter'] = {uid: "'" + this.rbObject.uid + "'"};
      } else {
        let related = this.rbObject.getRelated(this.attribute);
        if(related != null) {
          target['objectname'] = related.objectname;
          target['filter'] = {uid: "'" + related.uid + "'"};
        } else {
          let relatedUid = this.rbObject.get(this.attribute);
          target['filter'] = {uid: "'" + relatedUid + "'"};
        }
      }
      if(this.view != null) {
        target['view'] = this.view;
      } 
      this.navigateService.navigateTo(target);
    }
  }
}
