import { HostBinding } from '@angular/core';
import { Component, Input, OnInit, SimpleChange } from '@angular/core';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { RbTabSectionComponent } from 'app/rb-tab-section/rb-tab-section.component';

@Component({
  selector: 'rb-tab',
  templateUrl: './rb-tab.component.html',
  styleUrls: ['./rb-tab.component.css']
})
export class RbTabComponent extends RbActivatorComponent implements OnInit {
  @Input('tabsection') tabsection : RbTabSectionComponent;
  @Input('label') label : string;
  @Input('isdefault') isdefault : boolean = false;

  constructor(
  ) {
    super();
  }

  activatorInit() {
    this.tabsection.register(this);
  }

  activatorDestroy() {
  }

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
  }

  @HostBinding('style.display') get visitility() {
    return this.activatorOn ? 'flex' : 'none';
  }


}
