import { Component, OnInit } from '@angular/core';
import { RbContainerComponent } from 'app/abstract/rb-container';


@Component({
  selector: 'rb-hsection',
  templateUrl: './rb-hsection.component.html',
  styleUrls: ['./rb-hsection.component.css']
})
export class RbHsectionComponent extends RbContainerComponent implements OnInit {
  constructor() {
    super();
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
