import { HostBinding } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { RbContainerComponent } from 'app/abstract/rb-container';


@Component({
  selector: 'rb-vsection',
  templateUrl: './rb-vsection.component.html',
  styleUrls: ['./rb-vsection.component.css']
})
export class RbVsectionComponent extends RbContainerComponent implements OnInit {
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
