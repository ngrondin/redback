import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { RbFieldInputComponent } from '../abstract/rb-field-input';

@Component({
  selector: 'rb-textarea-input',
  templateUrl: './rb-textarea-input.component.html',
  styleUrls: ['../abstract/rb-field-input.css']
})
export class RbTextareaInputComponent extends RbFieldInputComponent {
  @Input('rows') rows: number = 3;

  defaultIcon: string = 'description';

  constructor() {
    super();
  }

  public get displayvalue(): any {
    if(this.isEditing) {
      return this.editedValue;
    } else {
      return this.value;
    }
  }
  
  public set displayvalue(val: any) {
    if(this.isEditing) {
      this.editedValue = val;
    } 
  }
  
  /*public get displayvalue(): string {
    if(this.rbObject != null) {
      let val = this.rbObject.data[this.attribute];
      if(val == null || val == "") {
        return null;
      } else if(typeof val == 'object') {
        return JSON.stringify(val, null, 2);
      } else {
        return val;
      }
    } else {
      return null;  
    }
  }

  public set displayvalue(val: string) {
    this.editedValue = val;
  }*/

  public onKeydown(event: any) {
    if(event.keyCode == 27) {
      this.cancelEditing();
    } 
  }

  public startEditing() {
    super.startEditing();
    this.editedValue = this.value;
  }

  public finishEditing() {
    this.commit(this.editedValue);
    super.finishEditing();
  }

}
