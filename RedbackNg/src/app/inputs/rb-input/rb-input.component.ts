import { Component, Input } from '@angular/core';
import { RbInputCommonComponent } from 'app/inputs/rb-input-common/rb-input-common.component';

@Component({
  selector: 'rb-input',
  templateUrl: './rb-input.component.html',
  styleUrls: ['./rb-input.component.css']
})
export class RbInputComponent extends RbInputCommonComponent {
  @Input('type') type: string;

  defaultIcon: string = 'description';

  constructor() {
    super();
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

  public focus(event: any) {
    event.target.select();
    /*if(!this.readonly) {
      setTimeout(() => {event.target.select();}, 200);
    }*/
  }

  commit() {
    this.previousValue = this.editedValue;
    this.rbObject.setValue(this.attribute, this.editedValue);
    this.change.emit(this.editedValue);
  }

}
