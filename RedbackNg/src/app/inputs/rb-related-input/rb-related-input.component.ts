import { Component, OnInit, Input, ViewContainerRef, Injector } from '@angular/core';
import { Overlay } from  '@angular/cdk/overlay';
import { RbObject } from '../../datamodel';
import { RbPopupListComponent } from '../../popups/rb-popup-list/rb-popup-list.component';
import { RbPopupInputComponent } from 'app/inputs/rb-popup-input/rb-popup-input.component';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';

@Component({
  selector: 'rb-related-input',
  templateUrl: '../rb-input-common/rb-input-common.component.html',
  styleUrls: ['../rb-input-common/rb-input-common.component.css']
})
export class RbRelatedInputComponent extends RbPopupInputComponent  {

  @Input('displayattribute') displayattribute: string;
  @Input('parentattribute') parentattribute: string;
  @Input('childattribute') childattribute: string;

  searchValue: string; 
  highlightedObject: RbObject;
  defaultIcon: string = 'description';

  constructor(
    public injector: Injector,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef
  ) {
    super(injector, overlay, viewContainerRef);
  }

  public get displayvalue(): string {
    let val: string = null;
    if(this.overlayRef != null) {
      val = this.searchValue;
    } else if(this.rbObject != null && this.rbObject.related[this.attribute] != null ) {
      val = this.rbObject.related[this.attribute].get(this.displayattribute);
      this.checkValueChange(val);
    }
    return val;
  }

  public set displayvalue(str: string) {
    this.searchValue = str;
    if(this.popupComponentRef != null) {
      let currentValue = this.searchValue;
      setTimeout(()=> {
        if(this.searchValue == currentValue)
        this.popupComponentRef.instance.setSearch(this.searchValue);
      }, 500);     
    }
  }

  public getPopupClass() {
    return RbPopupListComponent;
  }

  public getPopupConfig() {
    return {
      rbObject: this.rbObject, 
      attribute: this.attribute, 
      displayattribute: this.displayattribute, 
      parentattribute: this.parentattribute, 
      childattribute: this.childattribute
    };
  }

  public addPopupSubscription(instance: RbPopupComponent) {
    
  }


  public startEditing() {
    if(this.rbObject != null && this.rbObject.related[this.attribute] != null ) {
      this.searchValue = this.rbObject.related[this.attribute].get(this.displayattribute);
    } else {
      this.searchValue = '';
    }
  }

  public keyTyped(keyCode: number) {
    if((keyCode == 8 || keyCode == 27) && this.searchValue == "") {
      this.closePopup();
      this.rbObject.setValueAndRelated(this.attribute, null, null);
    } 
  }

  public finishEditing() {

  }
  
  public finishEditingWithSelection(value: any) {
    let object: RbObject = value;
    this.setValue(object);
  }

  public cancelEditing() {
  }

  private setValue(object: RbObject) {
    let link = this.rbObject.validation[this.attribute].related.link;
    let val = (link == 'uid') ? object.uid : object.data[link];
    this.rbObject.setValueAndRelated(this.attribute, val, object);
  }
}
