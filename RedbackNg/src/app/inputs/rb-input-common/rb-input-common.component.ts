import { HostBinding } from '@angular/core';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';

/*@Component({
  selector: 'rb-input-common',
  templateUrl: './rb-input-common.component.html',
  styleUrls: ['./rb-input-common.component.css'],
})*/
export abstract class RbInputCommonComponent extends RbDataObserverComponent {
  @Input('object') _rbObject: RbObject;
  @Input('label') label: string;
  @Input('icon') _icon: string;
  @Input('showicon') showicon: boolean = true;
  @Input('size') size: number;
  @Input('grow') grow: number;
  @Input('editable') editable: boolean = true;
  @Input('attribute') attribute: string;
  @Input('value') value: any;
  @Output('valueChange') valueChange = new EventEmitter();
  @Output('change') change = new EventEmitter();
  @Output('keyup') keyupEvent = new EventEmitter();
  @HostBinding('class.rb-input-margin') marginclass: boolean = true;
  @HostBinding('style.flex-grow') get flexgrow() { return this.grow != null ? this.grow : 0;}
  @HostBinding('style.width') get styleWidth() { return (this.size != null ? ((0.88 * this.size) + 'vw'): this.defaultSize != null ? ((0.88 * this.defaultSize) + 'vw'): null);}

  previousObject: RbObject;
  previousValue: any;
  editedValue: any;
  flasherOn: boolean = false;
  isEditing: boolean = false;
  defaultIcon: string;
  defaultSize: number = 15;

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

  public get displayvalue(): any {
    return null;//this.value;
  }

  public set displayvalue(val: any) {
    //this.editedValue = val;
  }

  get icon(): string {
    return this._icon != null ? this._icon : this.defaultIcon;
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
      return (0.88 * this.size) + 'vw';
    else
      return '100%';
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

  public focus(event: any) {
    if(!this.readonly) {
      this.isEditing = true;
    }
  }

  public blur(event: any) {
    this.isEditing = false;
  }

  public keydown(event: any) {
  
  }

  public keyup(event: any) {
    this.valueChange.emit(this.editedValue);
    this.keyupEvent.emit(event);
  }

  public commit() {
    //this.value = this.editedValue;
    //this.editedValue = null;
  }
}
