import { Input, ViewContainerRef } from '@angular/core';
import { Component, OnInit, ViewChild } from '@angular/core';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbTabComponent } from 'app/rb-tab/rb-tab.component';
import { LogService } from 'app/services/log.service';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-tab-section',
  templateUrl: './rb-tab-section.component.html',
  styleUrls: ['./rb-tab-section.component.css']
})
export class RbTabSectionComponent extends RbContainerComponent implements OnInit {
  @Input('keeplasttab') keeplasttab : boolean = false;
  @Input('secondary') secondary: boolean = false;

  @ViewChild('container', { read: ViewContainerRef, static: true }) container: ViewContainerRef;
  
  tabs: RbTabComponent[] = [];
  selectedTab: RbTabComponent;

  cmTop: number = 100;
  cmLeft: number = 100;
  cmShow: boolean = false;
  cmTab: RbTabComponent;

  constructor(
    public userpref: UserprefService,
    public logService: LogService
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
    this.logService.debug("TabSection " + this.id + ": Activation event " + state + " (selectedTab:" + this.selectedTab + ")");
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

  public register(tab: RbTabComponent) {
    let showtab = this.userpref.getCurrentViewUISwitch('tab',  tab.label);
    if(showtab == null || showtab == true) {
      this.tabs.push(tab);
      let lasttab = this.keeplasttab == true && this.id != null ? this.userpref.getCurrentViewUISwitch('tabsection', this.id) : null;
      if(this.selectedTab == null && (lasttab == null && tab.isdefault == true) || (lasttab != null && tab.label == lasttab)) {
        this.selectTab(tab);
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

  openPopup(event: any, tab: RbTabComponent) {
    this.cmTab = tab;
    this.cmTop = event.clientY;
    this.cmLeft = event.clientX;
    this.cmShow = true;
  }

  closePopup() {
    this.cmShow = false;
  }

  hideTabDomain() {
    this.userpref.setUISwitch('domain', 'tab', this.cmTab.label, false);
    if(this.tabs.indexOf(this.cmTab) > -1) {
      this.tabs.splice(this.tabs.indexOf(this.cmTab), 1);
      if(this.selectedTab == this.cmTab) {
        this.selectedTab = null;
        this.cmTab.deactivate();
      }
    }
    this.cmShow = false;
    this.cmTab = null;
  }

  overflows(element) : boolean {
    if (element.offsetWidth < element.scrollWidth) {
      return true;
    } else {
      return false;
    } 
  }

  scrollRight(element) {
    element.scrollLeft += 60;
  }

  scrollLeft(element) {
    element.scrollLeft -= 60;
  }

}
