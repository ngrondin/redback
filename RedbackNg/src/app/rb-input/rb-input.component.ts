import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from '../datamodel';
import {FormControl, FormGroupDirective, NgForm, Validators} from '@angular/forms';
import { RbInputCommonComponent } from 'app/rb-input-common/rb-input-common.component';

@Component({
  selector: 'rb-input',
  templateUrl: './rb-input.component.html',
  styleUrls: ['./rb-input.component.css']
})
export class RbInputComponent extends RbInputCommonComponent implements OnInit {
/*
  @Input('label') label: string;
  @Input('icon') icon: string;
  @Input('size') size: number;
  @Input('editable') editable: boolean;
  @Input('object') rbObject: RbObject;
  @Input('attribute') attribute: string;
  @Output('change') change = new EventEmitter();

  editedValue: string;
*/

  constructor() {
    super();
   }

  ngOnInit() {
    if(this.icon == null) {
      this.icon = 'description';
    }
  }

  public get value(): string {
    if(this.rbObject != null) {
      if(this.attribute == 'uid') {
        return this.rbObject.uid;
      } else {
        return this.rbObject.get(this.attribute);
      }
    } else {
      return null;  
    }
  }

  public set value(val: string) {
    this.editedValue = val;
  }

  /*
  public get readonly(): boolean {
    if(this.rbObject != null && this.rbObject.validation[this.attribute] != null)
      return !(this.editable && this.rbObject.validation[this.attribute].editable);
    else
      return true;      
  }
  */

  public get widthString() : string {
    if(this.size != null)
      return (15*this.size) + 'px';
    else
      return '100%';
  }

  commit() {
    this.rbObject.setValue(this.attribute, this.editedValue);
    this.change.emit(this.editedValue);
  }

}
