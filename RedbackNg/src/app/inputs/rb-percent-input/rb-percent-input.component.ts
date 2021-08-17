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


  public get displayvalue(): any {
    if(this.isEditing) {
      return this.editedValue;
    } else {
      if(this.value != null)
        return this.value + " %";
      else 
        return null;
    }
  }
  
  public set displayvalue(val: any) {
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

  public onFocus(event: any) {
    super.onFocus(event);
    if(this.isEditing) event.target.select();
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
