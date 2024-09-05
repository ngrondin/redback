import { Component, HostBinding, Input } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'rb-text',
  templateUrl: './rb-text.component.html',
  styleUrls: ['./rb-text.component.css']
})
export class RbTextComponent extends RbDataObserverComponent {
  @Input('attribute') attribute: string;    
  @Input('object') _rbObject: RbObject;
  @Input('value') _value: any = null;
  @Input('variable') variable: any;
  @Input('margin') margin: boolean = true;
  
  @HostBinding('class.rb-input-margin') get marginclass() { return this.margin }
  
  dataObserverInit() {
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
  }

  get rbObject() : RbObject {
    if(this.dataset != null) {
        return this.dataset.selectedObject;
    } else {
        return this._rbObject;
    }
  }

  get value() : any {
    let val = null;
    if(this.attribute != null) {
      if(this.rbObject != null) {
          if(this.attribute == 'uid') {
              val = this.rbObject.uid;
          } else {
              val = this.rbObject.get(this.attribute);
          } 
      } else {
          val = null;
      }
    } else if(this.variable != null) {
      val = window.redback[this.variable];
    } else {
      val = this._value;
    }
    return val;
  }
}
