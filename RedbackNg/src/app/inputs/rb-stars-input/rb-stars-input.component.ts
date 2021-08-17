import { HostBinding } from '@angular/core';
import { Component, Input, OnInit } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';
import { RbInputComponent } from '../abstract/rb-input';

@Component({
  selector: 'rb-stars-input',
  templateUrl: './rb-stars-input.component.html',
  styleUrls: ['./rb-stars-input.component.css']
})
export class RbStarsInputComponent extends RbInputComponent {
  @Input('margin') margin: boolean = true;
  @HostBinding('class.rb-input-margin') get marginclass() { return this.margin }

  position: number[] = [1, 2, 3, 4, 5];
  
  constructor() {
    super();
    this.defaultSize = 10;
  }

  public get displayvalue(): any {
    if(!isNaN(this.value)) {
      return this.value;
    } else {
      return 0;
    }
  }

  public set displayvalue(val: any) {
    this.commit(val);
  }

  public select(val) {
    this.displayvalue = val;
  }
}
