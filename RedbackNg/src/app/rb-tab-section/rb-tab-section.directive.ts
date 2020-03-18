import { Directive, Input } from '@angular/core';
import { RbTabDirective } from 'app/rb-tab/rb-tab.directive';

@Directive({
  selector: 'rb-tab-section',
  exportAs: 'tabsection'
})
export class RbTabSectionDirective {
  @Input('active') active : boolean;
  @Input('initiallyactive') initiallyActiveTabId: string;

  tabs: RbTabDirective[] = [];
  visibleTab: RbTabDirective;

  constructor() { }

  public register(tab : RbTabDirective) {
    this.tabs.push(tab);
    if(this.initiallyActiveTabId != null && this.initiallyActiveTabId == tab.id) {
      this.visibleTab = tab;
    }
  }

  public select(tab: RbTabDirective) {
    this.visibleTab = tab;
  }

  public isTabVisible(tab: RbTabDirective): boolean {
    return (this.active && this.visibleTab != null && this.visibleTab == tab);
  }
}
