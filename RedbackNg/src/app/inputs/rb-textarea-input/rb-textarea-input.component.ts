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
  @Input('rows') rows: number = null;

  defaultIcon: string = 'description';

  constructor() {
    super();
  }
  
  public onKeydown(event: any) {
    if(event.keyCode == 27) {
      this.cancelEditing();
    } 
  }

  public finishEditing() {
    this.commit(this.editedValue);
    super.finishEditing();
  }

}
