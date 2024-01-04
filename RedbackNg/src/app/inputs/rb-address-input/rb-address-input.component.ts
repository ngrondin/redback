import { Component, Input } from '@angular/core';
import { RbPopupInputComponent } from '../abstract/rb-popup-input';
import { RbPopupAddressesComponent } from 'app/popups/rb-popup-addresses/rb-popup-addresses.component';
import { PopupService } from 'app/services/popup.service';

@Component({
  selector: 'rb-address-input',
  templateUrl: '../abstract/rb-field-input.html',
  styleUrls: ['../abstract/rb-field-input.css']
})
export class RbAddressInputComponent extends RbPopupInputComponent {
  @Input('centerattribute') centerAttribute: string;

  defaultIcon: string = 'description';

  constructor(
    public popupService: PopupService
  ) {
    super(popupService);
  }

  public setDisplayValue(str: string) {
    this.editedValue = str;
    if(this.isEditing) {
      let currentValue = this.editedValue;
      setTimeout(()=> {
        if(this.editedValue == currentValue) {
          this.popupComponentRef.instance.setSearch(this.editedValue);
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

  /*public startEditing() {
    super.startEditing();
    this.editedValue = this.rbObject.get(this.attribute);
  }*/

  public onKeyTyped(keyCode: number) {
    super.onKeyTyped(keyCode);
    if((keyCode == 8 || keyCode == 27) && this.editedValue == "") {
      this.finishEditingWithSelection(null);
    } 
  }

  public finishEditingWithSelection(value: any) {
    super.finishEditingWithSelection(value);
    this.commit(value);
  }

}
