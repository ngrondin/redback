import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbInputCommonComponent } from 'app/inputs/rb-input-common/rb-input-common.component';

@Component({
  selector: 'rb-switch-input',
  templateUrl: './rb-switch-input.component.html',
  styleUrls: ['./rb-switch-input.component.css']
})
export class RbSwitchInputComponent extends RbInputCommonComponent {

  mode = 'checkbox';

  constructor() {
    super();
    this.defaultSize = null;
  }

  public get displayvalue(): boolean {
    if(this.rbObject != null) {
      return this.rbObject.data[this.attribute];
    } else {
      return false;  
    }
  }

  public set displayvalue(val: boolean) {
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
