import { HostBinding } from '@angular/core';
import { Input } from '@angular/core';
import { Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbInputComponent } from './rb-input';


export abstract class RbFieldInputComponent extends RbInputComponent {
  @Input('margin') margin: boolean = true;
  @Output('keyup') keyupEvent = new EventEmitter();
  @HostBinding('class.rb-input-margin') get marginclass() { return this.margin }
  @HostBinding('style.flex-grow') get flexgrow() { return this.grow != null ? this.grow : 0;}
  @HostBinding('style.width') get styleWidth() { return (this.size != null ? ((0.88 * this.size) + 'vw'): this.defaultSize != null ? ((0.88 * this.defaultSize) + 'vw'): null);}

  editedValue: any;
  isEditing: boolean = false;

  constructor() {
    super();
    this.defaultIcon = "description";
  }

  inputInit() {
    
  }

  public onFocus(event: any) {
    this.startEditing();
  }

  
  public onBlur(event: any) {
    this.finishEditing();
  }


  public onKeydown(event: any) {
    if(event.keyCode == 13) {
      event.target.blur();
    } else if(event.keyCode == 27) {
      this.cancelEditing();
    } 
  }


  public onKeyup(event: any) {
  }


  public startEditing() {
    if(!this.readonly) {
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
