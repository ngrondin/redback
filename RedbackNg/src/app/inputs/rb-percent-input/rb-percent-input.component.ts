import { Component, Input, OnInit } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';

@Component({
  selector: 'rb-percent-input',
  templateUrl: '../abstract/rb-field-input.html',
  styleUrls: ['../abstract/rb-field-input.css']
})
export class RbPercentInputComponent  extends RbFieldInputComponent  {
  @Input('capped') capped = true;

  constructor() {
    super();
    this.defaultSize = 6;
    this.defaultIcon = "trending_up";
  }

  public getPersistedDisplayValue(): any {
    if(this.value != null)
      return this.value + " %";
    else 
      return null; 
  }
  
  public setDisplayValue(val: any) {
    if(this.isEditing) {
      if(isNaN(val) && val != '-' && !isNaN(this.editedValue)) {
        if(this.capped && val <= 100 && val >= -100) {
          var curVal = this.editedValue;
          setTimeout(() => {this.editedValue = curVal}, 50);
        }
      }
      this.editedValue = val;
    } 
  }

  public finishEditing() {
    if(this.hadUserEdit) {
      this.commit(this.editedValue);
    }
    super.finishEditing();
  }

}
