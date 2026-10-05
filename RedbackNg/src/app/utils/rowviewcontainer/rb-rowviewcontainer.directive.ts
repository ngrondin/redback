import { ViewContainerRef, Input } from '@angular/core';
import { Directive } from '@angular/core';

@Directive({
  selector: '[rb-row-view-container-for]'
})
export class RbRowViewContainerDirective {
  @Input('rb-row-view-container-for') object: any;
  
  constructor(public viewContainerRef: ViewContainerRef) {
    console.log("Registered row view")
  }
}
