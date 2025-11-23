import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';

@Component({
  selector: 'rb-switch',
  templateUrl: './rb-switch.component.html',
  styleUrls: ['./rb-switch.component.css']
})
export class RbSwitchComponent {
  @Input('checked') checked: boolean = false;
  @Input('disabled') disabled: boolean = false;

  @Output('change') change = new EventEmitter();

  @HostListener('click', ['$event']) 
  onClick($event) {
    if(!this.disabled) {
      this.checked = !this.checked;
      this.change.emit(this.checked);
    }
  }
}
