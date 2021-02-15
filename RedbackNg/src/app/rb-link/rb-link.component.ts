import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';

@Component({
  selector: 'rb-link',
  templateUrl: './rb-link.component.html',
  styleUrls: ['./rb-link.component.css']
})
export class RbLinkComponent extends RbDataObserverComponent {
  @Input('dataset') dataset: RbDatasetComponent;
  @Input('attribute') attribute: string;
  @Input('view') view: string;

  @Output() navigate: EventEmitter<any> = new EventEmitter();

  
  constructor() {
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

  get rbObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : null;
  }

  public navigateTo() {
    if(this.rbObject != null && this.attribute != null) {
      let target = {};
      if(this.attribute == 'uid') {
        target['object'] = this.rbObject.objectname;
        target['filter'] = {uid: "'" + this.rbObject.uid + "'"};
      } else {
        let related = this.rbObject.related[this.attribute];
        if(related != null) {
          target['object'] = related.objectname;
          target['filter'] = {uid: "'" + related.uid + "'"};
        }
      }
      if(this.view != null) {
        target['view'] = this.view;
      } 
      this.navigate.emit(target);
    }
  }
}
