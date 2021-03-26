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
      if(isNaN(val) && val != '-' && !isNaN(this.editedValue)) {
        var curVal = this.editedValue;
        setTimeout(() => {this.editedValue = curVal}, 50);
      }
      this.editedValue = val;
    } 
  }

  public onFocus(event: any) {
    super.onFocus(event);
    event.target.select();
  }
  
  public onBlur(event: any) {
    super.onBlur(event);  
  }

  public onKeydown(event: any) {
    super.onKeydown(event);
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
