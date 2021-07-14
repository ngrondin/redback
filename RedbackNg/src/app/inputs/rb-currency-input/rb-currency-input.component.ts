import { Input } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';

@Component({
  selector: 'rb-currency-input',
  templateUrl: '../abstract/rb-field-input.html',
  styleUrls: ['../abstract/rb-field-input.css']
})
export class RbCurrencyInputComponent extends RbFieldInputComponent  {
  @Input('decimalCount') decimalCount: number = 2;
  @Input('thousandsSeparator') thousandsSeparator: string = ",";
  @Input('decimalSeparator') decimalSeparator: string = ".";

  defaultIcon: string = 'attach_money';

  constructor() {
    super();
   }

  get displayvalue() : string {
    let ret: string = null;
    if(this.isEditing) {
      ret = this.editedValue;
    } else {
      if(this.rbObject != null && this.rbObject.data[this.attribute] != null) {
        let amount = this.rbObject.data[this.attribute];
        const negativeSign = amount < 0 ? "-" : "";
        let i = parseInt(amount = Math.abs(Number(amount) || 0).toFixed(this.decimalCount)).toString();
        let j = (i.length > 3) ? i.length % 3 : 0;
        ret = negativeSign + (j ? i.substr(0, j) + this.thousandsSeparator : '') + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + this.thousandsSeparator) + (this.decimalCount ? this.decimalSeparator + Math.abs(amount - parseInt(i)).toFixed(this.decimalCount).slice(2) : "");
      } else {
        ret = null;
      }
    }
    return ret;
  }

  set displayvalue(val: string) {
    this.editedValue = val;
  }

  public onFocus(event: any) {
    super.onFocus(event);
    if(this.isEditing) event.target.select();
  }

  public startEditing() {
    let val = this.displayvalue;
    super.startEditing();
    this.editedValue = val;
  }

  public finishEditing() {
    let val = parseFloat(this.editedValue);
    this.commit(val);
    super.finishEditing()
  }
}
