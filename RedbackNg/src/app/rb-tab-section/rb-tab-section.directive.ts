import { Directive, Input } from '@angular/core';
import { RbTabDirective } from 'app/rb-tab/rb-tab.directive';

@Directive({
  selector: 'rb-tab-section',
  exportAs: 'tabsection'
})
export class RbTabSectionDirective {
  @Input('active') active : boolean;
  
  tabs: RbTabDirective[] = [];
  visibleTab: RbTabDirective;

  constructor() { }

  public register(tab : RbTabDirective) {
    this.tabs.push(tab);
  }

  public select(tab: RbTabDirective) {
    this.visibleTab = tab;
  }

  public isTabVisible(tab: RbTabDirective): boolean {
    return (this.active && this.visibleTab != null && this.visibleTab == tab);
  }
}
