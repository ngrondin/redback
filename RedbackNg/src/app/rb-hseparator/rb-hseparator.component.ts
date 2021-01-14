import { Component, OnInit } from '@angular/core';
import { RbComponent } from 'app/abstract/rb-component';

@Component({
  selector: 'rb-hseparator',
  templateUrl: './rb-hseparator.component.html',
  styleUrls: ['./rb-hseparator.component.css']
})
export class RbHseparatorComponent extends RbComponent {
  
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
