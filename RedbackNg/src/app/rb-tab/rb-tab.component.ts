import { HostBinding } from '@angular/core';
import { Component, Input, OnInit, SimpleChange } from '@angular/core';
import { RbContainerComponent } from 'app/rb-container/rb-container.component';
import { RbTabSectionComponent } from 'app/rb-tab-section/rb-tab-section.component';

@Component({
  selector: 'rb-tab',
  templateUrl: './rb-tab.component.html',
  styleUrls: ['./rb-tab.component.css']
})
export class RbTabComponent extends RbContainerComponent implements OnInit {
  @Input('tabsection') tabsection : RbTabSectionComponent;
  @Input('label') label : string;
  @Input('isdefault') isdefault : boolean = false;

  constructor(
  ) {
    super();
  }

  ngOnChanges(changes : SimpleChange) {
    if('tabsection' in changes && this.tabsection != null) {
      this.tabsection.register(this);
    }
  }

  ngOnInit(): void {
  }

  public get active() : boolean {
    return this.tabsection.isTabActive(this);
  }

  @HostBinding('style.display') get visitility() {
    return this.active ? 'flex' : 'none';
  }


}
