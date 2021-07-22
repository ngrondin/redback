import { HostBinding } from '@angular/core';
import { Component, Input, OnInit } from '@angular/core';
import { RbAggregateDisplayComponent } from '../abstract/rb-aggregate-display';

@Component({
  selector: 'rb-number-tiles',
  templateUrl: './rb-number-tiles.component.html',
  styleUrls: ['./rb-number-tiles.component.css']
})
export class RbNumberTilesComponent extends RbAggregateDisplayComponent {
  @Input('rows') rows: number = 1;
  @Input('cols') cols: number = 1;
  @Input('width') width: number;
  @Input('height') height: number;
  @HostBinding('style.width.px') get widthStyle() { return this.width != null ? this.width : null;}
  @HostBinding('style.height.px') get heightStyle() { return this.height != null ? this.height : null;}

  constructor() {
    super();
  }

}
