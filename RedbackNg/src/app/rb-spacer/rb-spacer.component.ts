import { Component, HostBinding, Input, OnInit } from '@angular/core';
import { RbComponent } from 'app/abstract/rb-component';

@Component({
  selector: 'rb-spacer',
  templateUrl: './rb-spacer.component.html',
  styleUrls: ['./rb-spacer.component.css']
})
export class RbSpacerComponent extends RbComponent {
  @Input('width') _width: number;
  @Input('height') _height: number;
  @HostBinding('style.flex-grow') get flexgrow() { return this._width == null && this._height == null ? 1 : 0;}
  @HostBinding('style.width') get width() { return this._width != null ? (0.88 * this._width) + "vw" : null;}
  @HostBinding('style.height') get height() { return this._height != null ? (0.88 * this._height) + "vw" : null;}

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
