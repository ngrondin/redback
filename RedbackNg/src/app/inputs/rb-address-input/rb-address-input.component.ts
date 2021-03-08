import { Component, OnInit, Injector, ViewContainerRef, Input } from '@angular/core';
import { RbPopupInputComponent } from 'app/inputs/rb-popup-input/rb-popup-input.component';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { Overlay } from '@angular/cdk/overlay';
import { RbPopupAddressesComponent } from 'app/popups/rb-popup-addresses/rb-popup-addresses.component';

@Component({
  selector: 'rb-address-input',
  templateUrl: '../rb-input-common/rb-input-common.component.html',
  styleUrls: ['../rb-input-common/rb-input-common.component.css']
})
export class RbAddressInputComponent extends RbPopupInputComponent {
  @Input('centerattribute') centerAttribute: string;

  searchValue: string; 
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
    let cfg = {}
    if(this.centerAttribute != null) {
      let geo = this.rbObject.get(this.centerAttribute);
      if(geo != null) {
        cfg['center'] = {
          lat: geo.coords.latitude,
          lng: geo.coords.longitude
        };
        cfg['radius'] = 100000;
      }
    } 
    return cfg;
  }

  public addPopupSubscription(instance: RbPopupComponent) {
    
  }

  public startEditing() {
    this.searchValue = this.rbObject.get(this.attribute);
  }

  public keyTyped(keyCode: number) {
    if((keyCode == 8 || keyCode == 27) && this.searchValue == "") {
      this.closePopup();
      this.rbObject.setValue(this.attribute, null);
    } 
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
