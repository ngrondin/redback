import { Component, OnInit, Input, ViewContainerRef, Injector } from '@angular/core';
import { Overlay } from  '@angular/cdk/overlay';
import { RbObject } from '../../datamodel';
import { RbPopupListComponent } from '../../popups/rb-popup-list/rb-popup-list.component';
import { RbPopupInputComponent } from '../abstract/rb-popup-input';
import { RbPopupHierarchyComponent } from 'app/popups/rb-popup-hierarchy/rb-popup-hierarchy.component';

@Component({
  selector: 'rb-hierarchy-input',
  templateUrl: '../abstract/rb-field-input.html',
  styleUrls: ['../abstract/rb-field-input.css']
})
export class RbHierarchyInputComponent extends RbPopupInputComponent  {

  @Input('displayattribute') displayattribute: string;
  @Input('parentattribute') parentattribute: string;
  @Input('childattribute') childattribute: string;

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
    if(this.isEditing) {
      val = this.editedValue;
    } else if(this.rbObject != null && this.rbObject.related[this.attribute] != null ) {
      let check = this.value; //this is to force a check if changed
      val = this.rbObject.related[this.attribute].get(this.displayattribute);
    }
    return val;
  }

  public set displayvalue(str: string) {
    this.editedValue = str;
    if(this.isEditing) {
      let currentValue = this.editedValue;
      setTimeout(()=> {
        if(this.editedValue == currentValue)
        this.popupComponentRef.instance.setSearch(this.editedValue);
      }, 500);     
    }
  }

  public getPopupClass() {
    return RbPopupHierarchyComponent;
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

  public startEditing() {
    super.startEditing();
    if(this.rbObject != null && this.rbObject.related[this.attribute] != null ) {
      this.editedValue = this.rbObject.related[this.attribute].get(this.displayattribute);
    } else {
      this.editedValue = null;
    }
  }

  public onKeyTyped(keyCode: number) {
    super.onKeyTyped(keyCode);
    if((keyCode == 8 || keyCode == 27) && this.editedValue == "") {
      this.finishEditingWithSelection(null);
    } 
  }

 public finishEditingWithSelection(value: any) {
    super.finishEditingWithSelection(value);
    let object: RbObject = value;
    let link = this.rbObject.validation[this.attribute].related.link;
    let val = object != null ? link == 'uid' ? object.uid : object.data[link] : null;
    this.commit(val, object);
  }

}
