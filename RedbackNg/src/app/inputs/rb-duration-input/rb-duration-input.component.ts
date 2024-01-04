import { Component, OnInit, Input } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';

@Component({
  selector: 'rb-duration-input',
  templateUrl: '../abstract/rb-field-input.html',
  styleUrls: ['../abstract/rb-field-input.css']
})
export class RbDurationInputComponent extends RbFieldInputComponent implements OnInit {
  defaultIcon: string = 'timelapse';

  constructor() {
    super();
  }

  public getPersistedDisplayValue(): any {
    if(this.value != null) {
      let ms = this.value;
      let years = Math.floor(ms / 31536000000);
      let weeks = Math.floor((ms % 31536000000) / 604800000);
      let days = Math.floor((ms % 604800000) / 86400000);
      let hours = Math.floor((ms % 86400000) / 3600000);
      let minutes = Math.floor((ms % 3600000) / 60000);
      let seconds = Math.floor((ms % 60000) / 1000);
      let milli = Math.floor((ms % 1000));
      let greaterThanMinute = ms > 60000;
      let greaterThanHour = ms > 3600000;
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
      if(seconds != 0 && !greaterThanHour)
        val = val + " " + seconds + "s";
      if(milli != 0 && !greaterThanMinute)
        val = val + " " + milli + "ms";
      if(ms == 0) 
        val = " 0";
      return val.substr(1);
    } else {
      return null;
    }
  }

  public initEditedValue() {
    this.editedValue = this.getPersistedDisplayValue();
  }

  public finishEditing() {
    if(this.editedValue != null) {
      let val = 0;
      let input = this.editedValue.replace(/\s/g, '');
      input = input.replace(/years/gi, 'y');
      input = input.replace(/year/gi, 'y');
      input = input.replace(/weeks/gi, 'w');
      input = input.replace(/week/gi, 'w');
      input = input.replace(/days/gi, 'd');
      input = input.replace(/day/gi, 'd');
      input = input.replace(/hours/gi, 'h');
      input = input.replace(/hour/gi, 'h');
      input = input.replace(/minutes/gi, 'm');
      input = input.replace(/minute/gi, 'm');
      input = input.replace(/seconds/gi, 's');
      input = input.replace(/second/gi, 's');
      let str: string = "";
      let error: boolean = false;
      for(let c of input.split('')) {
        if((c >= '0' && c <= '9') || c == '.') {
          str = str + c;
        } else {
          let multiplier = -1;
          if(c == 'y' || c == 'Y')
            multiplier = 31536000000.0;
          else if(c == 'w' || c == 'W')
            multiplier = 604800000.0;
          else if(c == 'd' || c == 'D')
            multiplier = 86400000.0;
          else if(c == 'h' || c == 'H')
            multiplier = 3600000.0;
          else if(c == 'm' || c == 'M')
            multiplier = 60000.0;
          else if(c == 's' || c == 'S')
            multiplier = 1000.0;
          if(multiplier > -1) {
            val += Number.parseFloat(str) * multiplier;
          } else {
            error = true;
          }
          str = "";
        }
      }
      if(!error) {
        if(str.length > 0) {
          val = val + (val == 0 ? 3600000.0 : 0) * Number.parseFloat(str);
        }      
        super.commit(val);
      } else {
        super.commit(null);
      }
    } else {
      super.commit(null);
    }
    super.finishEditing();
  }

}
