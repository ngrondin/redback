import { Output } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { Component, Input, OnInit } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';

@Component({
  selector: 'rb-string-input',
  templateUrl: '../abstract/rb-field-input.html',
  styleUrls: ['../abstract/rb-field-input.css']
})
export class RbStringInputComponent extends RbFieldInputComponent {
  @Output('keydown') keydown = new EventEmitter();
  
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
