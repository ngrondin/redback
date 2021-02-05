import { HostBinding } from '@angular/core';
import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { RbDatetimeInputComponent } from '../rb-datetime-input/rb-datetime-input.component';

@Component({
  selector: 'rb-input-common',
  templateUrl: './rb-input-common.component.html',
  styleUrls: ['./rb-input-common.component.css']
})
export abstract class RbInputCommonComponent extends RbDataObserverComponent {
  @Input('object') _rbObject: RbObject;
  @Input('label') label: string;
  @Input('icon') _icon: string;
  @Input('size') size: number;
  @Input('grow') grow: number;
  @Input('editable') editable: boolean = true;
  @Input('attribute') attribute: string;
  @Input('value') _value: any;
  @Output('valueChange') valueChange = new EventEmitter();
  @Output('change') change = new EventEmitter();
  @HostBinding('class.rb-input-margin') marginclass: boolean = true;
  @HostBinding('style.flex-grow') get flexgrow() { return this.grow != null ? this.grow : 0;}


  previousObject: RbObject;
  previousValue: any;
  editedValue: any;
  flasherOn: boolean = false;
  abstract defaultIcon: string;

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
    return this.dataset != null ? this.dataset.selectedObject : this._rbObject;
  }

  get icon(): string {
    return this._icon != null ? this._icon : this.defaultIcon;
  }

  public checkValueChange(value: any) {
    if(this.previousValue != value && this.previousObject == this.rbObject) {
      this.flash();
    }
    this.previousValue = value;
    this.previousObject = this.rbObject;
  }

  public flash() {
    setTimeout(() => {this.flasherOn = true}, 1);
    setTimeout(() => {this.flasherOn = false}, 100);
  }


  public get readonly(): boolean {
    if(this.attribute != null) {
      if(this.rbObject != null) {
        if(this.attribute == 'uid') {
          return this.rbObject.uid != null;
        } else if(this.rbObject.validation[this.attribute] != null) {
          return !(this.editable && this.rbObject.validation[this.attribute].editable);
        } else {
          return true;      
        }
      } else {
        return true;
      }
    } else {
      return !this.editable;
    }
  }

  public get widthString() : string {
    if(this.size != null)
      return (15*this.size) + 'px';
    else
      return '100%';
  }
}
