import { Component, Input } from '@angular/core';
import { RbAggregateDisplayComponent } from '../abstract/rb-aggregate-display';

@Component({
  selector: 'rb-hbar-graph',
  templateUrl: './rb-hbar-graph.component.html',
  styleUrls: ['./rb-hbar-graph.component.css']
})
export class RbHbarGraphComponent extends RbAggregateDisplayComponent {
  @Input('legendposition') legendposition: string = 'right';
  @Input('valueonbar') valueonbar: boolean = false;
  @Input('valuetargetlegend') valuetargetlegend: any;
  @Input('singlecolor') singlecolor: string = null;
}
