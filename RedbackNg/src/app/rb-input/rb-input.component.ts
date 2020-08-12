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
  @Input('type') type: string;

  constructor() {
    super();
   }

  ngOnInit() {
    if(this.icon == null) {
      this.icon = 'description';
    }
  }

  public get value(): string {
    let val: string = null;
    if(this.rbObject != null) {
      if(this.attribute == 'uid') {
        val = this.rbObject.uid;
      } else {
        val = this.rbObject.get(this.attribute);
      }
    } else {
      val = null;
    }
    this.checkValueChange(val);
    return val;
  }

  public set value(val: string) {
    this.editedValue = val;
  }

  public get widthString() : string {
    if(this.size != null)
      return (15*this.size) + 'px';
    else
      return '100%';
  }

  commit() {
    this.previousValue = this.editedValue;
    this.rbObject.setValue(this.attribute, this.editedValue);
    this.change.emit(this.editedValue);
  }

}
