import { Component, ViewChild, ViewContainerRef } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';

@Component({
  selector: 'rb-inline-input',
  templateUrl: './rb-inline-input.component.html',
  styleUrls: ['./rb-inline-input.component.css']
})
export class RbInlineInputComponent extends RbFieldInputComponent {
  showInput: boolean = false;

  inlineInput: any;
  @ViewChild('input') set setInput(comp: ViewContainerRef) {
    if(comp) {
      this.inlineInput = comp;
    }
  }
  constructor() {
    super();
  }

  public clickInlineInput() {
    if(this.showInput == false) {
      this.showInput = true;
      setTimeout(() => {
        this.inlineInput.nativeElement.focus();
      }, 10);
    }
  }

  public finishEditing() {
    this.showInput = false;
    if(this.hadUserEdit) {
      this.commit(this.editedValue);
    }
    super.finishEditing();
  }

}
