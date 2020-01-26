import { Directive, Output, EventEmitter } from '@angular/core';

@Directive({
  selector: 'rb-view'
})
export class RbViewDirective {
  @Output() afterload: EventEmitter<any> = new EventEmitter();
  
  constructor() { }

  ngOnInit() {
    this.afterload.emit();
  }
}
