import { Directive, Input, Output, EventEmitter } from '@angular/core';

@Directive({
  selector: 'rb-tab-d',
  exportAs: 'tab'
})
export class RbTabDirective {
  @Input('id') id: string;
  @Input('label') label: string;
  @Input('active') active : boolean;
  
  @Output() initialised: EventEmitter<any> = new EventEmitter();
  
  constructor() { }

  ngOnInit() {
    setTimeout(() => {
      this.initialised.emit(this);
    }, 1);      
  }

}
