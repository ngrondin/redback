import { Component, Input } from '@angular/core';
import { RbAggregateDisplayComponent } from '../abstract/rb-aggregate-display';

@Component({
  selector: 'rb-stacked-graph',
  templateUrl: './rb-stacked-graph.component.html',
  styleUrls: ['./rb-stacked-graph.component.css']
})
export class RbStackedGraphComponent extends RbAggregateDisplayComponent  {
  @Input('legendposition') legendposition: string = 'right';
  @Input('verticalxlabels') verticalxlabels: boolean = false;
  @Input('valuetargetlegend') valuetargetlegend: any;
  @Input('codeorder') codeorder?: string;

  constructor() {
    super();
  }


}
