import { Component, OnInit } from '@angular/core';
import { RbContainerComponent } from 'app/abstract/rb-container';

@Component({
  selector: 'rb-vcollapse',
  templateUrl: './rb-vcollapse.component.html',
  styleUrls: ['./rb-vcollapse.component.css']
})
export class RbVcollapseComponent extends RbContainerComponent {
  open: boolean = false;

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

  public get isOpen(): boolean {
    return this.open;
  }

  public toggle() {
    this.open = !this.open;
  }

}
