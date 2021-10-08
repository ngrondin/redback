import { Input } from '@angular/core';
import { Component } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbInputComponent } from '../abstract/rb-input';

@Component({
  selector: 'rb-code-input',
  templateUrl: './rb-code-input.component.html',
  styleUrls: ['./rb-code-input.component.css']
})
export class RbCodeInputComponent extends RbInputComponent {

  @Input('rows') rows: number;
  @Input('mode') mode: string;

  editedValue: string;

  theme: string = "eclipse";

  constructor() {
    super();
  }

  inputInit() {
  }

  public get displayvalue(): any {
    let ret = this.value;
    if(ret == null) {
      ret = "";
    } else if(typeof ret === 'object') {
      ret = JSON.stringify(ret, null, 2);
    }
    return ret;
  }

  public set displayvalue(val: any) {
    this.editedValue = val;
  }

  public get config(): any {
    let ret = {
      printMargin: false
    };
    return ret;
  }

  public get width():  any {
    if(this.size != null) {
      return this.size * 15;
    } else {
      return null;
    }
  }

  public get height():  any {
    if(this.rows != null) {
      return this.rows * 15;
    } else {
      return null;
    }
  }

  public onBlur(value: any) {
    this.commit(this.editedValue);
  }
 

}
