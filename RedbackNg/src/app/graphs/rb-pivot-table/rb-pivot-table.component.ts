import { Component, Input } from '@angular/core';
import { RbAggregateDisplayComponent } from '../abstract/rb-aggregate-display';

@Component({
  selector: 'rb-pivot-table',
  templateUrl: './rb-pivot-table.component.html',
  styleUrls: ['./rb-pivot-table.component.css']
})
export class RbPivotTableComponent extends RbAggregateDisplayComponent {
  @Input('format') format?: string;
  @Input('showsum') showsum: boolean = true;
  @Input('fontsize') fontsize: number = 1;

  constructor() {
    super();
  }
}
