import { Component,  Input } from '@angular/core';
import { Time } from 'app/datamodel';
import { RbPopupDatetimeComponent } from 'app/popups/rb-popup-datetime/rb-popup-datetime.component';
import { RbPopupInputComponent } from '../abstract/rb-popup-input';
import { Formatter } from 'app/helpers';
import { PopupService } from 'app/services/popup.service';

@Component({
  selector: 'rb-datetime-input',
  templateUrl: '../abstract/rb-field-input.html',
  styleUrls: ['../abstract/rb-field-input.css']
})
export class RbDatetimeInputComponent extends RbPopupInputComponent {
  @Input('format') format: string = 'YYYY-MM-DD HH:mm';
  
  defaultIcon: string = 'calendar_today';

  constructor(
    public popupService: PopupService
  ) {
    super(popupService);
  }

  public getPersistedDisplayValue(): any {
    return Formatter.formatDateTimeCustom(this.getDateValue(), this.format);
  }

  public setDisplayValue(str: string) {
    this.editedValue = str;
    if(this.popupComponentRef != null) {
      this.popupComponentRef.instance.setSearch(this.editedValue);
    }
  }

  public initEditedValue() {
    this.editedValue = this.getPersistedDisplayValue();
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

  public onKeyTyped(keyCode: number) {
    super.onKeyTyped(keyCode);
    if((keyCode == 8 || keyCode == 27) && (this.editedValue == "" || this.editedValue == null)) {
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