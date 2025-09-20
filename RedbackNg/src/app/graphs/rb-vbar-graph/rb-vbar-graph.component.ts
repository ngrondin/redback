import { Component, Input } from '@angular/core';
import { RbAggregateDisplayComponent } from '../abstract/rb-aggregate-display';

@Component({
  selector: 'rb-vbar-graph',
  templateUrl: './rb-vbar-graph.component.html',
  styleUrls: ['./rb-vbar-graph.component.css']
})
export class RbVbarGraphComponent extends RbAggregateDisplayComponent {
  @Input('legendposition') legendposition: string = 'right';
  @Input('verticalxlabels') verticalxlabels: boolean = false;
  @Input('valuetargetlegend') valuetargetlegend: any;
  @Input('singlecolor') singlecolor: string = null;
}
