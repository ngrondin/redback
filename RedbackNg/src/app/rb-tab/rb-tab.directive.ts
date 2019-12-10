import { Directive, Input, Output, EventEmitter } from '@angular/core';

@Directive({
  selector: 'rb-tab',
  exportAs: 'tab'
})
export class RbTabDirective {
  @Input('id') id: string;
  @Input('label') label: string;
  @Input('active') active : boolean;
  
  @Output() register: EventEmitter<any> = new EventEmitter();
  
  constructor() { }

  ngOnInit() {
    this.register.emit(this);
  }

}
