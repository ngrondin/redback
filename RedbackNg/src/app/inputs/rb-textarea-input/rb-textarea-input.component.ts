import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { RbInputCommonComponent } from 'app/inputs/rb-input-common/rb-input-common.component';

@Component({
  selector: 'rb-textarea-input',
  templateUrl: './rb-textarea-input.component.html',
  styleUrls: ['./rb-textarea-input.component.css']
})
export class RbTextareaInputComponent extends RbInputCommonComponent {
  @Input('rows') rows: number = 3;

  editedValue: string;
  defaultIcon: string = 'description';

  constructor() {
    super();
  }

  public get value(): string {
    if(this.rbObject != null) {
      let val = this.rbObject.data[this.attribute];
      if(typeof val == 'object') {
        return JSON.stringify(val, null, 2);
      } else {
        return val;
      }
    } else {
      return null;  
    }
  }

  public set value(val: string) {
    this.editedValue = val;
  }

  public get readonly(): boolean {
    if(this.rbObject != null && this.rbObject.validation[this.attribute] != null)
      return !(this.editable && this.rbObject.validation[this.attribute].editable);
    else
      return true;      
  }

  commit() {
    this.rbObject.setValue(this.attribute, this.editedValue);
    this.change.emit(this.editedValue);
  }


}
