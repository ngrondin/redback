import { Component, Input } from '@angular/core';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { RbContainerComponent } from 'app/abstract/rb-container';

@Component({
  selector: 'app-rb-hcollapse',
  templateUrl: './rb-hcollapse.component.html',
  styleUrls: ['./rb-hcollapse.component.css']
})
export class RbHcollapseComponent extends RbActivatorComponent {
  @Input('label') label: string = null;
  @Input('control') control: string = 'separator';
  @Input('defaultopen') defaultopen: boolean = false;

  open: boolean = false;

  constructor() {
    super();
  }

  activatorInit() {
    this.open = this.defaultopen;
    if(this.open) this.activate();
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

  public toggle() {
    this.open = !this.open;
    if(this.open) {
      this.activate();
    } else {
      this.deactivate();
    }
  }
}
