import { Input, ViewContainerRef } from '@angular/core';
import { Component, OnInit, ViewChild } from '@angular/core';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbTabComponent } from 'app/rb-tab/rb-tab.component';
import { UserprefService } from 'app/services/userpref.service';
import { Subscription } from 'rxjs/internal/Subscription';

@Component({
  selector: 'rb-tab-section',
  templateUrl: './rb-tab-section.component.html',
  styleUrls: ['./rb-tab-section.component.css']
})
export class RbTabSectionComponent extends RbContainerComponent implements OnInit {

  @ViewChild('container', { read: ViewContainerRef, static: true }) container: ViewContainerRef;
  
  tabs: RbTabComponent[] = [];
  activeTab: RbTabComponent;

  cmTop: number = 100;
  cmLeft: number = 100;
  cmShow: boolean = false;
  cmTab: RbTabComponent;

  constructor(
    public userpref: UserprefService
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
      if(this.activeTab != null) {
        this.activeTab.activate();
      }
    } else {
      if(this.activeTab != null) {
        this.activeTab.deactivate();
      }
    }    
  }

  public register(tab: RbTabComponent) {
    let swtch = this.userpref.getUISwitch('tab',  tab.label);
    if(swtch == null || swtch == true) {
      this.tabs.push(tab);
      if(tab.isdefault == true) {
        this.selectTab(tab);
      } else {
        tab.active = false;
      }
    } else {
      tab.active = false;
    }
  }


  public selectTab(tab: RbTabComponent) {
    if(this.activeTab != null && this.activeTab != tab) {
      this.activeTab.deactivate();
    }
    this.activeTab = tab;
    if(this.active == true) {
      this.activeTab.activate();
    }
  }

  public isTabActive(tab: RbTabComponent): boolean {
    return (this.active && this.activeTab != null && this.activeTab == tab);
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
      if(this.activeTab == this.cmTab) {
        this.activeTab = null;
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
