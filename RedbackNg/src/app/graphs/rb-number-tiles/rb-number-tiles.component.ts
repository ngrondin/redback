import { Component, Input, OnInit } from '@angular/core';
import { RbGraphsTilesComponent } from 'redbackgraphs';
import { RbAggregateDisplayComponent } from '../abstract/rb-aggregate-display';


@Component({
  selector: 'rb-number-tiles',
  templateUrl: './rb-number-tiles.component.html',
  styleUrls: ['./rb-number-tiles.component.css']
})
export class RbNumberTilesComponent extends RbAggregateDisplayComponent {
  @Input('rows') rows: number;
  @Input('cols') cols: number;

  constructor() {
    super();
  }

  

}
