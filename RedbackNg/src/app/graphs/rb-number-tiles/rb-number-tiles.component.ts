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
  @Input('format') format: string = null;
  @Input('valuecolorrange') valuecolorrange: any;
  @Input('fullcolor') fullcolor: boolean = false;
  
  constructor() {
    super();
  }

}
