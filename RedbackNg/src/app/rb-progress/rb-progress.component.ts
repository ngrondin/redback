import { Component, HostBinding, Input } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'rb-progress',
  templateUrl: './rb-progress.component.html',
  styleUrls: ['./rb-progress.component.css']
})
export class RbProgressComponent extends RbDataObserverComponent {
  @Input('attribute') attribute: string;    
  @Input('object') _rbObject: RbObject;
  @Input('value') _value: any = null;
  @Input('variable') variable: any;
  @Input('label') label: string = "Progress";
  @Input('icon') icon: string = "progress_activity";
  @Input('size') size: number;
  @Input('grow') grow: number;
  @Input('margin') margin: boolean = true;
  
  @HostBinding('class.rb-input-margin') get marginclass() { return this.margin }
  @HostBinding('style.flex-grow') get flexgrow() { return this.grow != null ? this.grow : 0;}
  @HostBinding('style.width') get styleWidth() { return (this.size != null ? ((0.88 * this.size) + 'vw'): this.defaultSize != null ? ((0.88 * this.defaultSize) + 'vw'): null);}

  defaultSize: number = 15;

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
    return val != null && !isNaN(val) ? Math.floor(val * 100) : 0;
  }

}
