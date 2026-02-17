import { Component, Input } from '@angular/core';
import { RbAggregateDisplayComponent } from '../abstract/rb-aggregate-display';

@Component({
  selector: 'rb-stacked-graph',
  templateUrl: './rb-stacked-graph.component.html',
  styleUrls: ['./rb-stacked-graph.component.css']
})
export class RbStackedGraphComponent extends RbAggregateDisplayComponent  {
  @Input('legendposition') legendposition: string = 'right';
  @Input('codeorder') codeorder: string = null;
  @Input('verticalxlabels') verticalxlabels: boolean = false;
  
  constructor() {
    super();
  }


}
