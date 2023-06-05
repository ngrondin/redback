import { Component } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';
import { EditorChangeContent, EditorChangeSelection } from 'ngx-quill';
import Quill from 'quill'

@Component({
  selector: 'rb-richtext-input',
  templateUrl: './rb-richtext-input.component.html',
  styleUrls: ['./rb-richtext-input.component.css']
})
export class RbRichtextInputComponent extends RbFieldInputComponent {
  
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

  created(event: Quill) {
  }

  changedEditor(event: EditorChangeContent | EditorChangeSelection) {
  }
  
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
