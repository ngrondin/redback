import { Component,  Input, Injector, ViewContainerRef } from '@angular/core';
import { Time } from 'app/datamodel';
import { Overlay } from '@angular/cdk/overlay';
import { RbPopupDatetimeComponent } from 'app/popups/rb-popup-datetime/rb-popup-datetime.component';
import { RbPopupInputComponent } from '../abstract/rb-popup-input';

@Component({
  selector: 'rb-datetime-input',
  templateUrl: '../abstract/rb-field-input.html',
  styleUrls: ['../abstract/rb-field-input.css']
})
export class RbDatetimeInputComponent extends RbPopupInputComponent {
  @Input('format') format: string = 'YYYY-MM-DD HH:mm';
  
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
    if(this.isEditing) {
      val = this.editedValue;
    } else {
      val = this.formatDateTime(this.getDateValue());
    }
    return val;
  }

  public set displayvalue(str: string) {
    this.editedValue = str;
    if(this.popupComponentRef != null) {
      this.popupComponentRef.instance.setSearch(this.editedValue);
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
    if(this.value == null) {
      iso = null;
    } else if(typeof this.value == "string") {
      iso = this.value;
    }  else if(typeof this.value.atDate == 'function') {
      iso = this.value.toString();
    } else if(typeof this.value.getTime == 'function') {
      iso = this.value.toISOString();
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

  public startEditing() {
    super.startEditing();
    if(this.rbObject != null && this.rbObject.data[this.attribute] != null) {
      this.editedValue = this.formatDateTime(this.getDateValue());
    } else {
      this.editedValue = '';
    }
  }

  public onKeyTyped(keyCode: number) {
    super.onKeyTyped(keyCode);
    if((keyCode == 8 || keyCode == 27) && (this.editedValue == "" || this.editedValue == null)) {
      //this.finishEditing();
      this.finishEditingWithSelection(null);
    } 
  }

  public finishEditingWithSelection(value: any) {
    super.finishEditingWithSelection(value);
    let dt: Date = value;
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
    this.commit(val);
  }


}
