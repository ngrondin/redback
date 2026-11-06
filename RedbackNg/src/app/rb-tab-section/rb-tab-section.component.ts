import { Input, QueryList, ViewChildren, ViewContainerRef } from '@angular/core';
import { Component, OnInit, ViewChild } from '@angular/core';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbColumnSelectorComponent } from 'app/popups/rb-column-selector/rb-column-selector.component';
import { RbTabComponent } from 'app/rb-tab/rb-tab.component';
import { LogService } from 'app/services/log.service';
import { PopupService } from 'app/services/popup.service';
import { UserprefService } from 'app/services/userpref.service';
import { RbRowViewContainerDirective } from 'app/utils/rowviewcontainer/rb-rowviewcontainer.directive';

@Component({
  selector: 'rb-tab-section',
  templateUrl: './rb-tab-section.component.html',
  styleUrls: ['./rb-tab-section.component.css']
})
export class RbTabSectionComponent extends RbContainerComponent implements OnInit {
  @Input('keeplasttab') keeplasttab : boolean = false;
  @Input('secondary') secondary: boolean = false;

  @ViewChild('container', { read: ViewContainerRef, static: true }) container!: ViewContainerRef;
  @ViewChildren(RbRowViewContainerDirective) headers: QueryList<RbRowViewContainerDirective>;
  
  allTabs: RbTabComponent[] = [];
  tabs: RbTabComponent[] = [];
  selectedTab: RbTabComponent | null = null;
  selectFirstShown: boolean = false;

  constructor(
    public userpref: UserprefService,
    public logService: LogService,
    public popupService: PopupService
  ) {
    super();
  }

  containerInit() {
  }

  containerDestroy() {
  }

  onDatasetEvent(event: any) {
  }

  onActivationEvent(state: any) {
    if(state == true) {
      if(this.selectedTab != null) {
        this.selectedTab.activate();
      }
    } else {
      if(this.selectedTab != null) {
        this.selectedTab.deactivate();
      }
    }    
  }

  get prefName(): string {
    return this.id ?? "default";
  }

  get hiddenTabs(): string[] {
    return this.userpref.getCurrentViewUISwitch('tabselector', this.prefName)?.hiddentabs ?? [];
  }

  tabKey(tab: RbTabComponent): string {
    return tab.id ?? tab.label ?? "Unnamed";
  }

  //Called in tab init
  public register(tab: RbTabComponent) {
    let showtab = this.userpref.getCurrentViewUISwitch('tab', this.tabKey(tab));
    if(showtab == null || showtab == true) {
      this.allTabs.push(tab);
      let hidden = this.hiddenTabs.includes(this.tabKey(tab));
      if(!hidden) {
        this.tabs.push(tab);
      }
      let lasttab = this.keeplasttab == true && this.id != null ? this.userpref.getCurrentViewUISwitch('tabsection', this.id) : null;
      let wanted = (this.selectedTab == null && lasttab == null && tab.isdefault == true) || (lasttab != null && tab.label == lasttab);
      this.selectFirstShown = this.selectFirstShown || (wanted && hidden);
      let selected = this.selectFirstShown ? this.tabs[0] : wanted && !hidden ? tab : null;
      if(selected != null) {
        this.selectFirstShown = false;
        this.selectedTab = selected;
        if(this.active) {
          this.selectedTab.activate();
        }
      }
    }
  }

  public selectTab(tab: RbTabComponent) {
    if(this.selectedTab != null && this.selectedTab != tab) {
      this.selectedTab.deactivate();
    }
    this.selectedTab = tab;
    if(this.keeplasttab == true && this.id != null) {
      this.userpref.setUISwitch('user', 'tabsection', this.id, tab.label);
    }
    if(this.active == true) {
      this.selectedTab.activate();
    }
  }

  public isTabActive(tab: RbTabComponent): boolean {
    return (this.active && this.selectedTab != null && this.selectedTab == tab);
  }

  openTabSelector(event: any, tab: RbTabComponent) {
    event.preventDefault();
    let items = this.allTabs.filter(t => t.show && t.label != null).map(t => ({key: this.tabKey(t), label: t.label, shown: this.tabs.includes(t)}));
    let anchor = this.headers.find(h => h.object == tab).viewContainerRef;
    let popup = this.popupService.openPopup(anchor, RbColumnSelectorComponent, {items: items}, event.buttons != 0);
    popup.instance.selected.subscribe(selected => {
      this.popupService.closePopup();
      let hidden = selected.filter((i: any) => !i.shown).map((i: any) => i.key);
      this.userpref.setUISwitch('user', 'tabselector', this.prefName, {hiddentabs: hidden});
      this.tabs = this.allTabs.filter(t => !hidden.includes(this.tabKey(t)));
      if(this.selectedTab != null && !this.tabs.includes(this.selectedTab)) {
        this.selectTab(this.tabs.find(t => t.show));
      }
    });
  }

  overflows(element: any) : boolean {
    if (element.offsetWidth < element.scrollWidth) {
      return true;
    } else {
      return false;
    } 
  }

  scrollRight(element: any) {
    element.scrollLeft += 60;
  }

  scrollLeft(element: any) {
    element.scrollLeft -= 60;
  }

}
