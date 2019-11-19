import { Directive } from '@angular/core';

@Directive({
  selector: 'rb-list-scroll'
})
export class RbListScrollDirective {

  public list: string = 'allo';
  constructor() { 
    this.list = 'all1';
  }

}
