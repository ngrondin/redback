import { Component, OnInit, Input } from '@angular/core';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'rb-switch-input',
  templateUrl: './rb-switch-input.component.html',
  styleUrls: ['./rb-switch-input.component.css']
})
export class RbSwitchInputComponent implements OnInit {

  @Input('label') label: string;
  @Input('icon') icon: string;
  @Input('editable') editable: boolean;
  @Input('object') rbObject: RbObject;
  @Input('attribute') attribute: string;
  //@Input('mode') mode: string;
  
  mode = 'checkbox';
  editedValue: boolean;

  constructor() { }

  ngOnInit() {
  }

  public get value(): boolean {
    if(this.rbObject != null) {
      return this.rbObject.data[this.attribute];
    } else {
      return false;  
    }
  }

  public set value(val: boolean) {
    this.editedValue = val;
  }
  commit() {
    if(this.attribute != 'uid') {
      this.rbObject.setValue(this.attribute, this.editedValue);
    }
  }
}
