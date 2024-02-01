import { Component, EventEmitter, Output, ViewChild, ViewContainerRef } from '@angular/core';
import { HostBinding } from '@angular/core';
import { Input } from '@angular/core';
import { AppInjector } from 'app/app.module';
import { RbObject } from 'app/datamodel';
import { DialogService } from 'app/services/dialog.service';
import { RbInputComponent } from './rb-input';

@Component({template: ''})
export abstract class RbFieldInputComponent extends RbInputComponent {
  @Input('margin') margin: boolean = true;

  @Output('keydown') keydown = new EventEmitter();
  
  @ViewChild('input', { read: ViewContainerRef, static: true }) input: ViewContainerRef;

  @HostBinding('class.rb-input-margin') get marginclass() { return this.margin }
  @HostBinding('style.flex-grow') get flexgrow() { return this.grow != null ? this.grow : 0;}
  @HostBinding('style.width') get styleWidth() { return (this.size != null ? ((0.88 * this.size) + 'vw'): this.defaultSize != null ? ((0.88 * this.defaultSize) + 'vw'): null);}

  isEditing: boolean = false;
  editedValue: any;
  hadUserEdit: boolean = false;
  originalValue: any;
  inputType: string = 'text';
  
  constructor() {
    super();
    this.defaultIcon = "description";
  }

  public onFocus(event: any) {
    if(!this.isEditing) {
      this.startEditing();
      if(this.isEditing) {
        event.target.select();
      }
    }
  }

  public onBlur(event: any) {
    if(this.isEditing) {
      this.finishEditing();
    }
  }

  public onKeydown(event: any) {
    this.keydown.emit(event);
    if(event.keyCode == 13) {
      event.target.blur();
    } else if(event.keyCode == 27) {
      this.cancelEditing();
    } else if(event.keyCode != 9) {
      this.hadUserEdit = true;
    }
  }

  public onKeyup(event: any) {
  }

  public get displayvalue(): any {
    return this.getDisplayValue();
  }

  public getDisplayValue(): any {
    if(this.isEditing) {
      return this.getEditingDisplayValue();
    } else {
      return this.getPersistedDisplayValue();
    }
  } 

  public getPersistedDisplayValue(): any {
    return this.value;
  }

  public getEditingDisplayValue(): any {
    return this.editedValue;
  }

  public set displayvalue(val: any) {
    this.setDisplayValue(val);
  }

  public setDisplayValue(val: any) {
    if(this.isEditing) {
      this.editedValue = val;
    } 
  }

  public initEditedValue() {
    this.editedValue = this.value;
  }

  public startEditing() {
    if(this.attribute != null && this.dataset != null && this.rbObject == null) {
      let text = this.dataset.list.length == 0 ? 'No record currently exist to edit. ' : 'No record has currently been selected to edit. ';
      text = text + 'Do you want to create a new one?';
      this.dialogService.openDialog(text, [{label: "Yes", callback: () => this.dataset.create()}, {label: "No", callback: () => {}}]);
    } else if(!this.readonly) {
      this.originalValue = this.value;
      this.isEditing = true;
      this.hadUserEdit = false;
      this.initEditedValue();
      this.checkPersistedValueChangedWhileEditing();
    } 

  }

  public finishEditing() {
    this.isEditing = false;
    this.editedValue = null;  
    this.hadUserEdit = false;  
  }

  public cancelEditing() {
    this.isEditing = false;
    this.editedValue = null;
  }

  public commit(val: any, related: RbObject = null) {
    super.commit(val, related);
  }

  checkPersistedValueChangedWhileEditing() {
    if(this.isEditing == true && this.hadUserEdit == false) {
      if(this.value != this.originalValue) {
        //console.log("Value Changed from " + this.originalValue + " to " + this.value);
        this.originalValue = this.value;
        this.initEditedValue();
        setTimeout(() => this.input.element.nativeElement.select(), 10);
      }
      setTimeout(() => this.checkPersistedValueChangedWhileEditing(), 250);
    }
  }
}
