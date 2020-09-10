import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'rb-textarea-input',
  templateUrl: './rb-textarea-input.component.html',
  styleUrls: ['./rb-textarea-input.component.css']
})
export class RbTextareaInputComponent implements OnInit {

  @Input('label') label: string;
  @Input('icon') icon: string;
  @Input('size') size: number;
  @Input('rows') rows: number = 3;
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
      let val = this.rbObject.data[this.attribute];
      if(typeof val == 'object') {
        return JSON.stringify(val, null, 2);
      } else {
        return val;
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
    this.rbObject.setValue(this.attribute, this.editedValue);
    this.change.emit(this.editedValue);
  }


}
