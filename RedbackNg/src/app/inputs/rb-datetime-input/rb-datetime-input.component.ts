import { Component, OnInit, Input, ComponentRef, Injector, ViewContainerRef, ViewChild, Output, EventEmitter } from '@angular/core';
import { RbObject, Time } from 'app/datamodel';
import { OverlayRef, Overlay } from '@angular/cdk/overlay';
import { RbPopupListComponent } from 'app/popups/rb-popup-list/rb-popup-list.component';
import { CONTAINER_DATA } from 'app/tokens';
import { PortalInjector, ComponentPortal } from '@angular/cdk/portal';
import { DateTimePopupConfig, RbPopupDatetimeComponent } from 'app/popups/rb-popup-datetime/rb-popup-datetime.component';
import { RbInputCommonComponent } from 'app/inputs/rb-input-common/rb-input-common.component';
import { RbPopupInputComponent } from 'app/inputs/rb-popup-input/rb-popup-input.component';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';

@Component({
  selector: 'rb-datetime-input',
  templateUrl: '../rb-input-common/rb-input-common.component.html',
  styleUrls: ['../rb-input-common/rb-input-common.component.css']
})
export class RbDatetimeInputComponent extends RbPopupInputComponent {
  @Input('format') format: string = 'YYYY-MM-DD HH:mm';
  @ViewChild('input', { read: ViewContainerRef }) inputContainerRef: ViewContainerRef;

  editingStr: string;
  defaultIcon: string = 'calendar_today';

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
      val = this.editingStr;
    } else {
      val = this.formatDateTime(this.getDateValue());
      this.checkValueChange(val);
    }
    return val;
  }

  public set displayvalue(str: string) {
    this.editingStr = str;
    if(this.popupComponentRef != null) {
      this.popupComponentRef.instance.setSearch(this.editingStr);
    }
  }

  private formatDateTime(dt: Date) : string {
    let val = null;
    if(dt != null) {
      val = this.format;
      val = val.replace('YYYY', dt.getFullYear().toString());
      val = val.replace('YY', (dt.getFullYear() % 100).toString());
      val = val.replace('MM', (dt.getMonth() + 1).toString().padStart(2, "0"));
      val = val.replace('DD', (dt.getDate()).toString().padStart(2, "0"));
      val = val.replace('HH', (dt.getHours()).toString().padStart(2, "0"));
      val = val.replace('mm', (dt.getMinutes()).toString().padStart(2, "0"));
    }
    return val;
  }

  private getDateValue() : Date {
    let iso: string = this.getISOValue();
    let dt: Date = null;
    if(iso != null) {
      if(iso.startsWith("T")) {
        dt = (new Time(iso)).atDate(new Date());
      } else {
        dt = new Date(iso);
      }
    }
    if(dt != null && isNaN(dt.getTime())) {
      dt = null;
    }
    return dt;
  }

  private getISOValue() : string {
    let iso: string = null;
    if(this.attribute != null) {
      if(this.rbObject != null) {
        iso = this.rbObject.get(this.attribute);
      } 
    } else {
      if(this._value != null) {
        if(typeof this._value == 'string') {
          iso = this._value;
        } else if(typeof this._value.atDate == 'function') {
          iso = this._value.toString();
        } else if(typeof this._value.getTime == 'function') {
          iso = this._value.toISOString();
        }
      } 
    }
    return iso;
  }

  private hasDatePart() : boolean {
    return this.format.indexOf('YY') > -1 || this.format.indexOf('MM') > -1 || this.format.indexOf('DD') > -1;
  }

  public getPopupClass() {
    return RbPopupDatetimeComponent;
  }

  public getPopupConfig() {
    return {
      initialDate: this.getDateValue() || (new Date()),
      datePart: this.format.indexOf('YY') > -1 || this.format.indexOf('MM') > -1 || this.format.indexOf('DD') > -1 ? true : false,
      hourPart: this.format.indexOf('HH') > -1 ? true : false,
      minutePart: this.format.indexOf('mm') > -1 ? true : false
    };
  }

  public addPopupSubscription(instance: RbPopupComponent) {
    
  }

  public startEditing() {
    if(this.rbObject != null && this.rbObject.data[this.attribute] != null) {
      this.editingStr = this.formatDateTime(this.getDateValue());
    } else {
      this.editingStr = '';
    }
  }

  public keyTyped(keyCode: number) {
    if((keyCode == 8 || keyCode == 27) && (this.editingStr == "" || this.editingStr == null)) {
      this.closePopup();
      this.rbObject.setValue(this.attribute, null);
    } 
  }

  public finishEditing() {
    
  }

  public finishEditingWithSelection(value: any) {
    let dt: Date = value;
    if(this.attribute != null) {
      if(this.rbObject != null) {
        let val: string = null;
        if(dt == null) {
          val = null;
        } else if(this.hasDatePart()) {
          val = dt.toISOString();
        } else {
          let time: Time = new Time(); 
          time.setHours(dt.getHours());
          time.setMinutes(dt.getMinutes());
          time.setSeconds(dt.getSeconds());
          val = time.toString();
        }
        this.previousValue = val; 
        this.rbObject.setValue(this.attribute, val);
      }
    } else {
      this.valueChange.emit(dt.toISOString());
    }
  }

  public erase() {
    this.rbObject.setValue(this.attribute, null);
  }

  public cancelEditing() {
    
  }
}
