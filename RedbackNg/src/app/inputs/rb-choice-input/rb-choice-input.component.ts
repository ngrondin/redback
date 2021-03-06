import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbInputCommonComponent } from 'app/inputs/rb-input-common/rb-input-common.component';

@Component({
  selector: 'rb-choice-input',
  templateUrl: './rb-choice-input.component.html',
  styleUrls: ['./rb-choice-input.component.css']
})
export class RbChoiceInputComponent extends RbInputCommonComponent {
  @Input('choicelist') choicelist: any;
  @Output('change') change = new EventEmitter();

  public editedValue: string; 
  defaultIcon: string = 'description';

  constructor() {
    super();
  }

  public get selectedvalue(): any {
    let val: any = null;
    if(this.rbObject != null) {
      let v = this.rbObject.get(this.attribute);
      for(let opt of this.choicelist) {
        if(opt['value'] == v)
          val = opt['value'];
      }
    } else {
      val = null;  
    }
    this.checkValueChange(val);
    return val;
  }

  public get displayValue(): any {
    let dv: any = null;
    if(this.rbObject != null) {
      let v = this.rbObject.get(this.attribute);
      for(let opt of this.choicelist) {
        if(opt['value'] == v)
          dv = opt['display'];
      }
    } else {
      dv = "";  
    }
    return dv;
  }

  public set selectedvalue(val: any) {
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
    this.change.emit(this.editedValue);
  }
}
