import { Component, HostBinding, Input, OnInit } from '@angular/core';
import { RbComponent } from 'app/abstract/rb-component';

@Component({
  selector: 'rb-spacer',
  templateUrl: './rb-spacer.component.html',
  styleUrls: ['./rb-spacer.component.css']
})
export class RbSpacerComponent extends RbComponent {
  @Input('size') size: number;
  @HostBinding('style.flex-grow') get flexgrow() { return this.size == null ? 1 : 0;}
  @HostBinding('style.width') get width() { return this.size != null ? this.size + "px" : null;}
  @HostBinding('style.height') get height() { return this.size != null ? this.size + "px" : null;}

  constructor() {
    super();
  }

  componentInit() {
  }

  componentDestroy() {
  }

  onActivationEvent(state: boolean) {
  }

}
