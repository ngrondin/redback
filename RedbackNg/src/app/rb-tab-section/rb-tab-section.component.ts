import { Input, ViewContainerRef } from '@angular/core';
import { Component, OnInit, ViewChild } from '@angular/core';
import { RbContainerComponent } from 'app/rb-container/rb-container.component';
import { RbTabComponent } from 'app/rb-tab/rb-tab.component';
import { RbTabDirective } from 'app/rb-tab/rb-tab.directive';
import { UserprefService } from 'app/userpref.service';

@Component({
  selector: 'rb-tab-section',
  templateUrl: './rb-tab-section.component.html',
  styleUrls: ['./rb-tab-section.component.css']
})
export class RbTabSectionComponent extends RbContainerComponent implements OnInit {
  @Input('tab') tab : RbTabComponent;

  @ViewChild('container', { read: ViewContainerRef, static: true }) container: ViewContainerRef;
  
  tabs: RbTabComponent[] = [];
  activeTab: RbTabComponent;

  constructor(
    public userpref: UserprefService
  ) {
    super();
  }

  ngOnInit(): void {
  }

  public get active() : boolean {
    if(this.tab == null) {
      return true;
    } else {
      return this.tab.active;
    }
  }

  public register(tab: RbTabComponent) {
    this.tabs.push(tab);
    if(tab.isdefault == true) {
      this.activeTab = tab;
    }
  }

  public select(tab: RbTabComponent) {
    this.activeTab = tab;
  }

  public isTabActive(tab: RbTabComponent): boolean {
    return (this.active && this.activeTab != null && this.activeTab == tab);
  }
}
