import { Component, Input, OnInit } from '@angular/core';
import { RbContainerComponent } from 'app/abstract/rb-container';

@Component({
  selector: 'rb-group',
  templateUrl: './rb-group.component.html',
  styleUrls: ['./rb-group.component.css']
})
export class RbGroupComponent extends RbContainerComponent implements OnInit {
  @Input('label') label: string;

  constructor() {
    super();
    if(this.grow == null) this.grow = 0;
    if(this.shrink == null) this.shrink = 0;
  }

  containerInit() {
  }

  containerDestroy() {
  }

  onDatasetEvent(event: any) {
  }

  onActivationEvent(state: boolean) {
  }
}
