import { Component, EventEmitter, HostListener, OnInit, Output } from '@angular/core';
import { RbComponent } from 'app/abstract/rb-component';

@Component({
  selector: 'rb-list-item',
  templateUrl: './rb-list-item.component.html',
  styleUrls: ['./rb-list-item.component.css']
})
export class RbListItemComponent extends RbComponent {
  constructor() {
    super();
  }

  componentInit() {
  }

  componentDestroy() {
  }

  onActivationEvent(state: boolean) {
  }
}
