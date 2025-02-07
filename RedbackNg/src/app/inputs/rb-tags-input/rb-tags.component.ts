import { Component } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';

@Component({
  selector: 'rb-tags-input',
  templateUrl: './rb-tags.component.html',
  styleUrls: ['./rb-tags.component.css']
})
export class RbTagsInputComponent extends RbFieldInputComponent {
  
  editedTags = [];

  constructor() {
    super();
    this.defaultIcon = "label";
  }

  public get tags(): string[] {
    return this.value ?? [];
  }

  public getDisplayTags(): any {
    if(this.isEditing) {
      return this.editedTags;
    } else {
      return this.tags;
    }
  }

  public getPersistedDisplayValue(): any {
    return null;
  }

  public initEditedValue() {
    this.editedValue = "";
    this.editedTags = [...this.tags];
  }

  public onKeydown(event: any) {
    super.onKeydown(event);
    if(event.keyCode == 8 && this.editedValue == "" && this.editedTags.length > 0) {
      this.editedTags.splice(this.editedTags.length - 1)
    } 
  }

  public finishEditing() {
    if(this.hadUserEdit) {
      if(this.editedValue != null && this.editedValue != "") {
        this.editedTags.push(this.editedValue);
      }
      this.commit(this.editedTags);
    }
    super.finishEditing();
  }
}
