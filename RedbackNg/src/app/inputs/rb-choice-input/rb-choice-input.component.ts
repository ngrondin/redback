import { Component, Input } from '@angular/core';
import { RbPopupHardlistComponent } from 'app/popups/rb-popup-hardlist/rb-popup-hardlist.component';
import { RbPopupInputComponent } from '../abstract/rb-popup-input';
import { PopupService } from 'app/services/popup.service';


@Component({
  selector: 'rb-choice-input',
  templateUrl: '../abstract/rb-field-input.html',
  styleUrls: ['../abstract/rb-field-input.css']
})
export class RbChoiceInputComponent extends RbPopupInputComponent {
  @Input('choicelist') _choicelist: any;
  @Input('choicelistvariable') choicelistvar: any;
  
  public editedValue: string; 
  defaultIcon: string = 'description';

  constructor(
    public popupService: PopupService
  ) {
    super(popupService);
  }

  public get choicelist(): any {
    let list = null;
    if(this._choicelist != null) {
      list = this._choicelist;
    } else if(this.choicelistvar != null) {
      list = window.redback[this.choicelistvar];
    } 
    return list || [];
  }

  public get selectedItem(): any {
    let val: any = null;
    if(this.rbObject != null) {
      val = this.rbObject.get(this.attribute);
    } else {
      val = this.value;
    }
    for(let opt of this.choicelist) {
      if((opt['value'] != null && opt['value'] == val) 
        || (opt['value'] == null && opt === val) 
        || (opt['value'] != null && typeof opt['value'].getTime == 'function' && val != null && typeof val.getTime == 'function' && opt['value'].getTime() == val.getTime())) {
        return opt;
      }
    }
    return null;
  }

  public get displayvalue(): any {
    let item = this.selectedItem;
    if(item != null) {
      return item['display'] || item['label'];
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
