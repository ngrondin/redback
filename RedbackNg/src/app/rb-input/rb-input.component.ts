import { Component, OnInit, Input } from '@angular/core';
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

  editedValue: string;

  constructor() { }

  ngOnInit() {
  }

  public get value(): string {
    if(this.rbObject != null)
      return this.rbObject.data[this.attribute];
    else
      return null;  
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

  commit() {
    this.rbObject.setValue(this.attribute, this.editedValue);
  }

}
