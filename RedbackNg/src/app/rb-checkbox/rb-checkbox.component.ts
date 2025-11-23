import { Component, EventEmitter, HostBinding, HostListener, Input, Output } from '@angular/core';

@Component({
  selector: 'rb-checkbox',
  templateUrl: './rb-checkbox.component.html',
  styleUrls: ['./rb-checkbox.component.css']
})
export class RbCheckboxComponent {
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
