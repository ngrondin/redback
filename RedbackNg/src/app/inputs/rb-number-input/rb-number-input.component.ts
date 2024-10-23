import { Component, OnInit } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';

@Component({
  selector: 'rb-number-input',
  templateUrl: '../abstract/rb-field-input.html',
  styleUrls: ['../abstract/rb-field-input.css']
})
export class RbNumberInputComponent extends RbFieldInputComponent {

  constructor() {
    super();
    this.defaultIcon = "dialpad";
  }
  
  public setDisplayValue(val: any) {
    if(this.isEditing) {
      if(isNaN(val) && val != '-' && !isNaN(this.editedValue)) {
        var curVal = this.editedValue;
        setTimeout(() => {this.editedValue = curVal}, 0);
      }
      this.editedValue = val;
    } 
  }

  public finishEditing() {
    if(this.hadUserEdit) {
      this.commit(parseFloat(this.editedValue));
    }
    super.finishEditing();
  }
}
