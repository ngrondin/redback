import { Component, Input, OnInit } from '@angular/core';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { RbContainerComponent } from 'app/abstract/rb-container';

@Component({
  selector: 'rb-vcollapse',
  templateUrl: './rb-vcollapse.component.html',
  styleUrls: ['./rb-vcollapse.component.css']
})
export class RbVcollapseComponent extends RbActivatorComponent {
  open: boolean = false;
  @Input('label') label: string = null;
  @Input('reverseicon') reverseicon: boolean = false;

  constructor() {
    super();
  }

  activatorInit() {

  }

  activatorDestroy() {

  }

  onDatasetEvent(event: any) {

  }

  onActivationEvent(state: boolean) {

  }

  public get isOpen(): boolean {
    return this.open;
  }

  public get iconSwitch(): boolean {
    return this.reverseicon ? !this.open : this.open;
  }

  public toggle() {
    this.open = !this.open;
    if(this.open) {
      this.activate();
    } else {
      this.deactivate();
    }
  }

}
