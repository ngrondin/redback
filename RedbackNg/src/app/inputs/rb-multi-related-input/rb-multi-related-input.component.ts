import { Component, Input } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { PopupService } from 'app/services/popup.service';
import { RbPopupInputComponent } from '../abstract/rb-popup-input';
import { RbPopupListComponent } from 'app/popups/rb-popup-list/rb-popup-list.component';

@Component({
  selector: 'rb-multi-related-input',
  templateUrl: './rb-multi-related-input.component.html',
  styleUrls: ['../abstract/rb-field-input.css', './rb-multi-related-input.component.css']
})
export class RbMultiRelatedInputComponent extends RbPopupInputComponent {
  @Input('displayattribute') displayattribute: string;
  @Input('sortattribute') sortattribute: string;
  @Input('parentattribute') parentattribute: string;
  @Input('childattribute') childattribute: string;

  highlightedObject: RbObject;
  defaultIcon: string = 'description';

  constructor(
    public popupService: PopupService
  ) {
    super(popupService);
  }

  public getMultiValues(): string[] {
    if(this.rbObject != null) {
      let check = this.value; //this is to force a check if changed
      let arr = this.rbObject.get(this.attribute);
      if(Array.isArray(arr)) {
        let relarr = this.rbObject.related[this.attribute];
        if(relarr != null && Array.isArray(relarr)) {
          return relarr.map(i => i.get(this.displayattribute));
        }
      }
    }
    return [];
  }

  public getPersistedDisplayValue(): any {
    return null;
  }

  public setDisplayValue(str: string) {
    this.editedValue = str;
    if(this.isEditing) {
      let currentValue = this.editedValue;
      setTimeout(()=> {
        if(this.editedValue == currentValue)
        this.popupComponentRef.instance.setSearch(this.editedValue);
      }, 500);     
    }
  }

  public initEditedValue() {
    this.editedValue = "";
  }

  public getPopupClass() {
    return RbPopupListComponent;
  }

  public getPopupConfig() {
    return {
      rbObject: this.rbObject, 
      attribute: this.attribute, 
      displayattribute: this.displayattribute, 
      sortattribute: this.sortattribute,
      parentattribute: this.parentattribute, 
      childattribute: this.childattribute
    };
  }

  public onKeyTyped(keyCode: number) {
    super.onKeyTyped(keyCode);
    if(keyCode == 8 && this.editedValue == "") {
      let arr = this.rbObject.get(this.attribute);
      if(arr.length > 0) {
        this.rbObject.setValue(this.attribute, arr.slice(0, arr.length - 1));
      } else {
        this.finishEditingWithSelection(null);
      }
    } 
  }

 public finishEditingWithSelection(value: any) {
    super.finishEditingWithSelection(value);
    let arr = [...this.rbObject.get(this.attribute)];
    if(!Array.isArray(arr)) {
        arr = [];
    }
    let object: RbObject = value;
    let link = this.rbObject.validation[this.attribute].related.link;
    let val = object != null ? link == 'uid' ? object.uid : object.data[link] : null;
    arr.push(val);
    this.commit(arr);
  }
}
