import { Overlay } from '@angular/cdk/overlay';
import { Component, Input, Injector, ViewContainerRef } from '@angular/core';

import { RbPopupHardlistComponent } from 'app/popups/rb-popup-hardlist/rb-popup-hardlist.component';
import { RbPopupInputComponent } from '../abstract/rb-popup-input';


@Component({
  selector: 'rb-choice-input',
  templateUrl: '../abstract/rb-field-input.html',
  styleUrls: ['../abstract/rb-field-input.css']
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

  public finishEditingWithSelection(value: any) {
    super.finishEditingWithSelection(value);
    this.commit(value);
  }
}
