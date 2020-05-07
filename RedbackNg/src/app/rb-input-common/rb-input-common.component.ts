import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'rb-input-common',
  templateUrl: './rb-input-common.component.html',
  styleUrls: ['./rb-input-common.component.css']
})
export class RbInputCommonComponent implements OnInit {
  @Input('label') label: string;
  @Input('icon') icon: string;
  @Input('size') size: number;
  @Input('editable') editable: boolean;
  @Input('object') rbObject: RbObject;
  @Input('attribute') attribute: string;
  @Input('value') value: string;
  @Output('valueChange') valueChange = new EventEmitter();
  @Output('change') change = new EventEmitter();

  previousObject: RbObject;
  previousValue: any;
  editedValue: any;
  flasherOn: boolean = false;

  constructor() { }

  ngOnInit() {
  }


  public checkValueChange(value: any) {
    if(this.previousValue != value && this.previousObject == this.rbObject) {
      this.flash();
    }
    this.previousValue = value;
    this.previousObject = this.rbObject;
  }

  public flash() {
    this.flasherOn = true;
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
}
