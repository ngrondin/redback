import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbInputCommonComponent } from 'app/inputs/rb-input-common/rb-input-common.component';

@Component({
  selector: 'rb-switch-input',
  templateUrl: './rb-switch-input.component.html',
  styleUrls: ['./rb-switch-input.component.css']
})
export class RbSwitchInputComponent extends RbInputCommonComponent implements OnInit {

  //@Input('label') label: string;
  //@Input('icon') icon: string;
  //@Input('editable') editable: boolean;
  //@Input('object') rbObject: RbObject;
  //@Input('attribute') attribute: string;
  //@Input('mode') mode: string;
  //@Output('change') change = new EventEmitter();
  
  mode = 'checkbox';
  editedValue: boolean;

  constructor() {
    super();
  }

  ngOnInit() {
  }
/*
  public get readonly(): boolean {
    if(this.rbObject != null && this.rbObject.validation[this.attribute] != null)
      return !(this.editable && this.rbObject.validation[this.attribute].editable);
    else
      return true;      
  }
*/
  public get value(): boolean {
    if(this.rbObject != null) {
      return this.rbObject.data[this.attribute];
    } else {
      return false;  
    }
  }

  public set value(val: boolean) {
    this.editedValue = val;
  }

  public toggle() {
    if(this.editedValue == null || this.editedValue == false) {
      this.editedValue = true;
    } else {
      this.editedValue = false;
    }
    this.commit();
  }

  commit() {
    if(this.attribute != 'uid') {
      this.rbObject.setValue(this.attribute, this.editedValue);
    }
    this.change.emit(this.editedValue);
  }
}
