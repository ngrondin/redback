import { Component, OnInit, Input } from '@angular/core';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'rb-duration-input',
  templateUrl: './rb-duration-input.component.html',
  styleUrls: ['./rb-duration-input.component.css']
})
export class RbDurationInputComponent implements OnInit {
  @Input('label') label: string;
  @Input('icon') icon: string;
  @Input('size') size: number;
  @Input('editable') editable: boolean;
  @Input('object') rbObject: RbObject;
  @Input('attribute') attribute: string;

  editing: boolean;
  editingValue: string;

  constructor() { }

  ngOnInit() {
    this.editing = false;
  }

  get displayvalue() : string {
    if(this.editing) {
      return this.editingValue;
    } else {
      if(this.rbObject != null && this.rbObject.data[this.attribute] != null) {
        let ms = this.rbObject.data[this.attribute];
        let years = Math.floor(ms / 31536000000);
        let weeks = Math.floor((ms % 31536000000) / 604800000);
        let days = Math.floor((ms % 604800000) / 86400000);
        let hours = Math.floor((ms % 86400000) / 3600000);
        let minutes = Math.floor((ms % 3600000) / 60000);
        let seconds = Math.floor((ms % 60000) / 1000);
        let milli = Math.floor((ms % 1000));
        let val = "";
        if(years != 0)
          val = val + " " + years + "y";
        if(weeks != 0)
          val = val + " " + weeks + "w";
        if(days != 0)
          val = val + " " + days + "d";
        if(hours != 0)
          val = val + " " + hours + "h";
        if(minutes != 0)
          val = val + " " + minutes + "m";
        if(seconds != 0)
          val = val + " " + seconds + "s";
        if(milli != 0)
          val = val + " " + milli + "ms";
        return val.substr(1);
      } else {
        return null;
      }
    }
  }

  set displayvalue(val: string) {
    this.editingValue = val;
  }

  
  public get readonly(): boolean {
    if(this.rbObject != null && this.rbObject.validation[this.attribute] != null)
      return !(this.editable && this.rbObject.validation[this.attribute].editable);
    else
      return true;      
  }

  public keydown(event: any) {

  }

  public focus(event: any) {
    
  }

  public blur(event: any) {
    
  }

  public commit() {
    let val = 0;
    let multiplier = -1;
    let str: string = "";
    let strParts: string[] = this.editingValue.split('');
    for(let c of strParts) {
      if(c == 'y' || c == 'Y')
        multiplier = 31536000000;
      else if(c == 'w' || c == 'W')
        multiplier = 604800000;
      else if(c == 'd' || c == 'D')
        multiplier = 86400000;
      else if(c == 'h' || c == 'H')
        multiplier = 3600000;
      else if(c == 'm' || c == 'M')
        multiplier = 60000;
      else if(c == 's' || c == 'S')
        multiplier = 1000;
      else 
        str = str + c;

      if(multiplier > -1) {
        val += Number.parseFloat(str) * multiplier;
        str = "";
        multiplier = -1;
      }  
    }
    if(str.length > 0)
      val = val + Number.parseInt(str);

    this.rbObject.setValue(this.attribute, val);
    this.editing = false;
    this.editingValue = null;
  }

}
