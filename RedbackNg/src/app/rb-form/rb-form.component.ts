import { Component, OnInit } from '@angular/core';
import { RbContainerComponent } from 'app/abstract/rb-container';

@Component({
  selector: 'rb-form',
  templateUrl: './rb-form.component.html',
  styleUrls: ['./rb-form.component.css']
})
export class RbFormComponent extends RbContainerComponent {
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
