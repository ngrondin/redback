import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from '../datamodel';
import {FormControl, FormGroupDirective, NgForm, Validators} from '@angular/forms';

@Component({
  selector: 'rb-input',
  templateUrl: './rb-input.component.html',
  styleUrls: ['./rb-input.component.css']
})
export class RbInputComponent implements OnInit {

  @Input('label') label: string;
  @Input('icon') icon: string;
  @Input('size') size: number;
  @Input('editable') editable: boolean;
  @Input('object') rbObject: RbObject;
  @Input('attribute') attribute: string;
  @Output('change') change = new EventEmitter();

  editedValue: string;

  constructor() { }

  ngOnInit() {
  }

  public get value(): string {
    if(this.rbObject != null) {
      if(this.attribute == 'uid') {
        return this.rbObject.uid;
      } else {
        return this.rbObject.data[this.attribute];
      }
    } else {
      return null;  
    }
  }

  public set value(val: string) {
    this.editedValue = val;
  }

  public get readonly(): boolean {
    if(this.rbObject != null && this.rbObject.validation[this.attribute] != null)
      return !(this.editable && this.rbObject.validation[this.attribute].editable);
    else
      return true;      
  }

  public get widthString() : string {
    if(this.size != null)
      return (15*this.size) + 'px';
    else
      return '100%';
  }

  commit() {
    if(this.attribute != 'uid') {
      this.rbObject.setValue(this.attribute, this.editedValue);
    }
    this.change.emit(this.editedValue);
  }

}
