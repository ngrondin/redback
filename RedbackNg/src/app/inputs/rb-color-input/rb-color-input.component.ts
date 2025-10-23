import { Component, ComponentRef, HostBinding, Input, ViewChild, ViewContainerRef } from '@angular/core';
import { RbPopupColorComponent } from 'app/popups/rb-popup-color/rb-popup-color.component';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { PopupService } from 'app/services/popup.service';
import { RbInputComponent } from '../abstract/rb-input';

@Component({
  selector: 'rb-color-input',
  templateUrl: './rb-color-input.component.html',
  styleUrls: ['./rb-color-input.component.css', '../abstract/rb-field-input.css']
})
export class RbColorInputComponent extends RbInputComponent {
  @Input('margin') margin: boolean = true;
  @HostBinding('class.rb-input-margin') get marginclass() { return this.margin }
  @ViewChild('colorbox', { read: ViewContainerRef }) colorboxContainerRef: ViewContainerRef;

  popupComponentRef: ComponentRef<RbPopupComponent>;
  
  constructor( 
    public popupService: PopupService
  ) {
    super();
    this.defaultSize = 5;
  }

  get color(): string {
    return this.value;
  }

  openPicker() {
    this.popupComponentRef = this.popupService.openPopup(this.colorboxContainerRef, RbPopupColorComponent, {});
    this.popupComponentRef.instance.selected.subscribe(value => this.onPopupValueSelected(value));
    this.popupComponentRef.instance.cancelled.subscribe(() => this.onPopupCancel());
  }

  onPopupValueSelected(value) {
    this.popupService.closePopup();
    this.commit(value);
  }

  onPopupCancel() {
    this.popupService.closePopup();
  }
}
