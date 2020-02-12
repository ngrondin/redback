import { Component, OnInit, Input } from '@angular/core';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'rb-choice-input',
  templateUrl: './rb-choice-input.component.html',
  styleUrls: ['./rb-choice-input.component.css']
})
export class RbChoiceInputComponent implements OnInit {
  @Input('label') label: string;
  @Input('icon') icon: string;
  @Input('size') size: number;
  @Input('editable') editable: boolean;
  @Input('object') rbObject: RbObject;
  @Input('attribute') attribute: string;
  @Input('choicelist') choicelist: any;

  public editedValue: string; 

  constructor() { }

  ngOnInit() {
  }

  public get value(): any {
    if(this.rbObject != null) {
      let v = this.rbObject.get(this.attribute);
      for(let opt of this.choicelist) {
        if(opt['value'] == v)
          return opt['value'];
      }
      return null;
    } else {
      return null;  
    }
  }

  public set value(val: any) {
    this.editedValue = val;
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
  }
}
