import { Component, Input } from '@angular/core';
import { RbComponent } from 'app/abstract/rb-component';

@Component({
  selector: 'rb-iconbutton',
  templateUrl: './rb-iconbutton.component.html',
  styleUrls: ['./rb-iconbutton.component.css']
})
export class RbIconbuttonComponent extends RbComponent {
  @Input('icon') icon: string; 
  @Input('focus') focus: boolean = false;

  componentInit() {
  }

  componentDestroy() {
  }

  onActivationEvent(state: boolean) {
  }
}
