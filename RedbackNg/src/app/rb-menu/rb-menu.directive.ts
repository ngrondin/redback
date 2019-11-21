import { Directive } from '@angular/core';

@Directive({
  selector: 'rb-menu'
})
export class RbMenuDirective {
  public largemenu : boolean;

  constructor() { 
    this.largemenu = true;
  }

}
