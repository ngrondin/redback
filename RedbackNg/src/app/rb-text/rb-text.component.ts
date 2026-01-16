import { Component, HostBinding, Input } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { Evaluator } from 'app/helpers';

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
  @Input('expression') _expression: any;
  @Input('size') size: number;
  @Input('margin') margin: boolean = true;
  @Input('alert') alert: boolean = false;
  @Input('icon') icon: string;
  @Input('color') _color: string;
  
  @HostBinding('class.rb-input-margin') get marginclass() { return this.margin }
  @HostBinding('style.width') get styleWidth() { return this.size != null ? ('min(' + (0.88 * this.size) + 'vw, ' + (17 * this.size) + 'px)'): null;}


  dataObserverInit() {
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
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
    } else if(this._expression != null) {
      val = Evaluator.eval(this._expression, this.selectedObject, this.dataset.relatedObject, this.dataset);
    } else {
      val = this._value;
    }
    return val;
  }

  get hasIcon() : boolean {
    return this.icon != null;
  }

  public get alertOn(): boolean {
    return this.alert;
  }

  public get color(): string {
    return this._color;
  }
}
