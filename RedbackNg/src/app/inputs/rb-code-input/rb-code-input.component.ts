import { Input } from '@angular/core';
import { Output } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { EventEmitter } from 'events';

@Component({
  selector: 'rb-code-input',
  templateUrl: './rb-code-input.component.html',
  styleUrls: ['./rb-code-input.component.css']
})
export class RbCodeInputComponent implements OnInit {
  @Input('dataset') dataset: RbDatasetComponent;
  @Input('size') size: number;
  @Input('rows') rows: number;
  @Input('editable') editable: boolean;
  //@Input('object') rbObject: RbObject;
  @Input('attribute') attribute: string;
  @Input('mode') mode: string;
  @Output('change') change = new EventEmitter();

  editedValue: string;

  theme: string = "eclipse";

  constructor() { }

  ngOnInit(): void {
  }

  public get rbObject(): RbObject {
    return this.dataset != null && this.dataset.selectedObject != null ? this.dataset.selectedObject : null;
  }

  public get code(): string {
    let ret = null;
    if(this.rbObject != null) {
      ret = this.rbObject.data[this.attribute];
      if(typeof ret === 'object') {
        ret = JSON.stringify(ret, null, 2);
      }
    } 
    return ret;
  }

  public set code(val: string) {
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

  public onChanged(value: any) {
    this.commit();
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
