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

  public getPersistedDisplayValue(): any {
    if(this.value != null) {
      let amount = this.value;
      const negativeSign = amount < 0 ? "-" : "";
      let i = parseInt(amount = Math.abs(Number(amount) || 0).toFixed(this.decimalCount)).toString();
      let j = (i.length > 3) ? i.length % 3 : 0;
      return negativeSign + (j ? i.substr(0, j) + this.thousandsSeparator : '') + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + this.thousandsSeparator) + (this.decimalCount ? this.decimalSeparator + Math.abs(amount - parseInt(i)).toFixed(this.decimalCount).slice(2) : "");
    } else {
      return null;
    }
  }

  public initEditedValue() {
    this.editedValue = this.value != null ? this.value.toFixed(this.decimalCount) : null;
  }

  public finishEditing() {
    if(this.hadUserEdit) {
      let val = parseFloat(this.editedValue);
      if(isNaN(val)) {
        val = null;
      }
      this.commit(val);  
    }
    super.finishEditing()
  }
}
