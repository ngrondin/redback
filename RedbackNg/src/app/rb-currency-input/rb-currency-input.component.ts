import { Input } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { RbInputCommonComponent } from 'app/rb-input-common/rb-input-common.component';

@Component({
  selector: 'rb-currency-input',
  templateUrl: './rb-currency-input.component.html',
  styleUrls: ['./rb-currency-input.component.css']
})
export class RbCurrencyInputComponent extends RbInputCommonComponent implements OnInit {
  @Input('decimalCount') decimalCount: number = 2;
  @Input('thousandsSeparator') thousandsSeparator: string = ",";
  @Input('decimalSeparator') decimalSeparator: string = ".";

  editing: boolean;
  editingValue: string;

  constructor() {
    super();
   }

  ngOnInit() {
    this.editing = false;
  }

  get displayvalue() : string {
    let ret: string = null;
    if(this.editing) {
      ret = this.editingValue;
    } else {
      if(this.rbObject != null && this.rbObject.data[this.attribute] != null) {
        let amount = this.rbObject.data[this.attribute];
        const negativeSign = amount < 0 ? "-" : "";
        let i = parseInt(amount = Math.abs(Number(amount) || 0).toFixed(this.decimalCount)).toString();
        let j = (i.length > 3) ? i.length % 3 : 0;
        return negativeSign + (j ? i.substr(0, j) + this.thousandsSeparator : '') + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + this.thousandsSeparator) + (this.decimalCount ? this.decimalSeparator + Math.abs(amount - parseInt(i)).toFixed(this.decimalCount).slice(2) : "");
        ret = "";
      } else {
        ret = null;
      }
      this.checkValueChange(ret);
    }
    return ret;
  }

  set displayvalue(val: string) {
    this.editingValue = val;
  }

  public keydown(event: any) {

  }

  public focus(event: any) {
    if(!this.readonly) {
      setTimeout(() => {event.target.select();}, 200);
    }
  }

  public blur(event: any) {
    
  }

  public commit() {
    let val = parseFloat(this.editingValue);
    this.rbObject.setValue(this.attribute, val);
    this.editing = false;
    this.editingValue = null;
  }
}
