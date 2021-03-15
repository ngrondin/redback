import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbInputComponent } from '../abstract/rb-input';

@Component({
  selector: 'rb-switch-input',
  templateUrl: './rb-switch-input.component.html',
  styleUrls: ['./rb-switch-input.component.css']
})
export class RbSwitchInputComponent extends RbInputComponent {

  mode = 'checkbox';

  constructor() {
    super();
    this.defaultSize = null;
  }

  inputInit() {
  }

  public get displayvalue(): boolean {
    let ret = this.value;
    if(ret == null) {
      ret = false;
    }
    return ret;
  }

  public set displayvalue(val: boolean) {
    
  }

  public toggle() {
    let newValue = null;
    if(this.value == null || this.value == false) {
      newValue = true;
    } else {
      newValue = false;
    }
    this.commit(newValue);
  }

}
