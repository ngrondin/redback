import { Component, Input, OnInit } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';

@Component({
  selector: 'rb-string-input',
  templateUrl: '../abstract/rb-field-input.html',
  styleUrls: ['../abstract/rb-field-input.css']
})
export class RbStringInputComponent extends RbFieldInputComponent {
  
  constructor() {
    super();
  }

  public finishEditing() {
    this.commit(this.editedValue);
    super.finishEditing();
  }

}
