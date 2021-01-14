import { Component, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { RbContainerComponent } from 'app/abstract/rb-container';


@Component({
  selector: 'rb-layout',
  templateUrl: './rb-layout.component.html',
  styleUrls: ['./rb-layout.component.css']
})
export class RbLayoutComponent extends RbContainerComponent  {

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
