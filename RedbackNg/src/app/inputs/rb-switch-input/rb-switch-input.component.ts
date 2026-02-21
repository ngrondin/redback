import { HostBinding } from '@angular/core';
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbInputComponent } from '../abstract/rb-input';

@Component({
  selector: 'rb-switch-input',
  templateUrl: './rb-switch-input.component.html',
  styleUrls: ['./rb-switch-input.component.css']
})
export class RbSwitchInputComponent extends RbInputComponent {
  @Input('margin') margin: boolean = true;
  @Input('labelafter') labelafter: boolean = false;
  @Input('mode') mode: string = 'checkbox';
  //@HostBinding('class.rb-switch-margin') get marginclass() { return this.margin }
  @HostBinding('class.rb-input-margin') get marginclass() { return this.margin }
  
  constructor() {
    super();
    this.defaultSize = null;
    this.defaultIcon = 'playlist_add_check_circle';
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
