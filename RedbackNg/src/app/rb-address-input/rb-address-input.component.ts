import { Component, OnInit, Injector, ViewContainerRef } from '@angular/core';
import { RbPopupInputComponent } from 'app/rb-popup-input/rb-popup-input.component';
import { RbPopupComponent } from 'app/rb-popup/rb-popup.component';
import { Overlay } from '@angular/cdk/overlay';
import { RbPopupAddressesComponent } from 'app/rb-popup-addresses/rb-popup-addresses.component';

@Component({
  selector: 'rb-address-input',
  templateUrl: './rb-address-input.component.html',
  styleUrls: ['./rb-address-input.component.css']
})
export class RbAddressInputComponent extends RbPopupInputComponent implements OnInit {

  searchValue: string; 

  constructor(
    public injector: Injector,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef
  ) {
    super(injector, overlay, viewContainerRef);
  }


  ngOnInit(): void {
  }
  
  public get displayvalue(): string {
    let val: string = null;
    if(this.popupComponentRef != null) {
      val = this.searchValue;
    } else if(this.rbObject != null) {
      val = this.rbObject.get(this.attribute);
      this.checkValueChange(val);
    }
    return val;
  }

  public set displayvalue(str: string) {
    this.searchValue = str;
    if(this.popupComponentRef != null) {
      let currentValue = this.searchValue;
      setTimeout(()=> {
        if(this.searchValue == currentValue) {
          this.popupComponentRef.instance.setSearch(this.searchValue);
        }
      }, 1000);     
    }
  }

  public getPopupClass() {
    return RbPopupAddressesComponent;
  }

  public getPopupConfig() {
    return {};
  }

  public addPopupSubscription(instance: RbPopupComponent) {
    
  }

  public startEditing() {
    this.searchValue = this.rbObject.get(this.attribute);
  }

  public finishEditing() {
    this.rbObject.setValue(this.attribute, this.searchValue);
  }

  public finishEditingWithSelection(value: any) {
    this.rbObject.setValue(this.attribute, value);
  }

  public erase() {
  }

  public cancelEditing() {
  }


}
