import { Overlay } from '@angular/cdk/overlay';
import { Component, OnInit, Input, Output, EventEmitter, Injector, ViewContainerRef } from '@angular/core';

import { RbPopupHardlistComponent } from 'app/popups/rb-popup-hardlist/rb-popup-hardlist.component';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { RbPopupInputComponent } from '../rb-popup-input/rb-popup-input.component';

@Component({
  selector: 'rb-choice-input',
  templateUrl: '../rb-input-common/rb-input-common.component.html',
  styleUrls: ['../rb-input-common/rb-input-common.component.css']
})
export class RbChoiceInputComponent extends RbPopupInputComponent {
  @Input('choicelist') choicelist: any;
  
  public editedValue: string; 
  defaultIcon: string = 'description';

  constructor(
    public injector: Injector,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef
  ) {
    super(injector, overlay, viewContainerRef);
  }

  public get selectedItem(): any {
    let val: any = null;
    if(this.rbObject != null) {
      val = this.rbObject.get(this.attribute);
    } else {
      val = this.value;
    }
    for(let opt of this.choicelist) {
      if(opt['value'] == val) {
        return opt;
      }
    }
    return null;
  }

  public get displayvalue(): any {
    let item = this.selectedItem;
    if(item != null) {
      return item['display'];
    }
    return null;
  }

  public set displayvalue(val: any) {

  }

  public getPopupClass() {
   return RbPopupHardlistComponent;
  }

  public getPopupConfig() {
    return this.choicelist;
  }

  public addPopupSubscription(instance: RbPopupComponent) {
    
  }

  public startEditing() {
    
  }

  public keyTyped(keyCode: number) {
    
  }

  public finishEditing() {
    
  }

  public finishEditingWithSelection(value: any) {
    this.setValue(value);
  }

  public cancelEditing() {
    
  }

  setValue(value: any) {
    if(this.attribute != null) {
      if(this.attribute != 'uid') {
        this.rbObject.setValue(this.attribute, value);
      }
    } else {
      this.value = value
    }
    this.change.emit(value);
    this.valueChange.emit(value);
  }
}
