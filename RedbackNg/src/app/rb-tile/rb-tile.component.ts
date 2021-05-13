import { Component, Input, OnInit } from '@angular/core';
import { RbContainerComponent } from 'app/abstract/rb-container';

@Component({
  selector: 'rb-tile',
  templateUrl: './rb-tile.component.html',
  styleUrls: ['./rb-tile.component.css']
})
export class RbTileComponent extends RbContainerComponent {
  @Input('title') title: string;

  constructor() {
    super();
  }

  containerInit() {
  }

  containerDestroy() {
  }

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
  }


}
