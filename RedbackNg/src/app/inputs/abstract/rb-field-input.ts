import { Component, EventEmitter, Output } from '@angular/core';
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
  
  @HostBinding('class.rb-input-margin') get marginclass() { return this.margin }
  @HostBinding('style.flex-grow') get flexgrow() { return this.grow != null ? this.grow : 0;}
  @HostBinding('style.width') get styleWidth() { return (this.size != null ? ((0.88 * this.size) + 'vw'): this.defaultSize != null ? ((0.88 * this.defaultSize) + 'vw'): null);}

  editedValue: any;
  isEditing: boolean = false;
  
  constructor() {
    super();
    this.defaultIcon = "description";
  }

  public onFocus(event: any) {
    if(!this.isEditing) {
      this.startEditing();
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
    } 
  }


  public onKeyup(event: any) {
  }


  public startEditing() {
    if(this.attribute != null && this.dataset != null && this.rbObject == null) {
      let text = this.dataset.list.length == 0 ? 'No record currently exist to edit. ' : 'No record has currently been selected to edit. ';
      text = text + 'Do you want to create a new one?';
      this.dialogService.openDialog(text, [{label: "Yes", callback: () => this.dataset.create()}, {label: "No", callback: () => {}}]);
    } else if(!this.readonly) {
      this.isEditing = true;
      this.editedValue = null;
    } 

  }

  public finishEditing() {
    this.isEditing = false;
    this.editedValue = null;    
  }


  public cancelEditing() {
    this.isEditing = false;
    this.editedValue = null;
  }

  public commit(val: any, related: RbObject = null) {
    super.commit(val, related);
  }
}
