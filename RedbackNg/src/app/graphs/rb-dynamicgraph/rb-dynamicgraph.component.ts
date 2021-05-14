
import { Component, Input} from '@angular/core';
import { RbAggregateDisplayComponent } from '../abstract/rb-aggregate-display';

@Component({
  selector: 'rb-dynamicgraph',
  templateUrl: './rb-dynamicgraph.component.html',
  styleUrls: ['./rb-dynamicgraph.component.css']
})
export class RbDynamicGraphComponent extends RbAggregateDisplayComponent {
  @Input('graphtype') type: String;
  

}
