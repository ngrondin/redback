import { Component, OnInit, Input, ComponentRef, Injector, ViewContainerRef, ViewChild, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { OverlayRef, Overlay } from '@angular/cdk/overlay';
import { RbPopupListComponent } from 'app/rb-popup-list/rb-popup-list.component';
import { CONTAINER_DATA } from 'app/tokens';
import { PortalInjector, ComponentPortal } from '@angular/cdk/portal';
import { DateTimePopupConfig, RbPopupDatetimeComponent } from 'app/rb-popup-datetime/rb-popup-datetime.component';
import { RbInputCommonComponent } from 'app/rb-input-common/rb-input-common.component';
import { RbPopupInputComponent } from 'app/rb-popup-input/rb-popup-input.component';
import { RbPopupComponent } from 'app/rb-popup/rb-popup.component';

@Component({
  selector: 'rb-datetime-input',
  templateUrl: './rb-datetime-input.component.html',
  styleUrls: ['./rb-datetime-input.component.css']
})
export class RbDatetimeInputComponent extends RbPopupInputComponent implements OnInit {

  @Input('format') format: string;

  @ViewChild('input', { read: ViewContainerRef }) inputContainerRef: ViewContainerRef;

  overlayRef: OverlayRef;
  popupDatetimeComponentRef: ComponentRef<RbPopupDatetimeComponent>;

  constructor(
    public injector: Injector,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef
  ) {
    super(injector, overlay, viewContainerRef);
   }

  ngOnInit() {
    if(this.format == null)
      this.format = 'YYYY-MM-DD HH:mm';
  }

  public get displayvalue(): string {
    let val: string = null;
    if(this.attribute != null) {
      if(this.rbObject != null) {
        let iso : string = this.rbObject.get(this.attribute);
        if(iso != null) {
          val = this.formatDate(new Date(iso));
        } else {
          val = null;
        }
      } else {
        val = null;  
      }
    } else {
      if(this._value != null) {
        val = this.formatDate(new Date(this._value));
      } else {
        val = null;
      }
    }
    this.checkValueChange(val);
    return val;
  }

  public set displayvalue(val: string) {
    
  }

  private formatDate(dt: Date) : string {
    let val = this.format;
    val = val.replace('YYYY', dt.getFullYear().toString());
    val = val.replace('YY', (dt.getFullYear() % 100).toString());
    val = val.replace('MM', this.convertToStringAndPad(dt.getMonth() + 1, 2));
    val = val.replace('DD', this.convertToStringAndPad(dt.getDate(), 2));
    val = val.replace('HH', this.convertToStringAndPad(dt.getHours(), 2));
    val = val.replace('mm', this.convertToStringAndPad(dt.getMinutes(), 2));
    return val;
  }

  private convertToStringAndPad(num: number, n: number) : string {
    let ret :string = num.toString();
    while(ret.length < n) 
      ret = '0' + ret;
    return ret;
  }

  public getPopupClass() {
    return RbPopupDatetimeComponent;
  }

  public getPopupConfig() {
    return {
      initialDate: this.rbObject != null && this.rbObject.data[this.attribute] != null ? new Date(this.rbObject.data[this.attribute]) : new Date(),
      datePart: this.format.indexOf('YY') > -1 || this.format.indexOf('MM') > -1 || this.format.indexOf('DD') > -1 ? true : false,
      hourPart: this.format.indexOf('HH') > -1 ? true : false,
      minutePart: this.format.indexOf('mm') > -1 ? true : false
    };
  }

  public addPopupSubscription(instance: RbPopupComponent) {
    
  }

  public startEditing() {
    
  }

  public finishEditing() {
    
  }

  public finishEditingWithSelection(value: any) {
    let dt: Date = value;
    if(this.attribute != null) {
      if(this.rbObject != null) {
        let val: string = (dt != null ? dt.toISOString() : null);
        this.previousValue = (dt != null ? this.formatDate(dt) : null);
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
