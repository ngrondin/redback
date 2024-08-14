import { ComponentRef, Component, ViewContainerRef } from "@angular/core";
import { RbActivatorComponent } from "app/abstract/rb-activator";
import { RbSetComponent } from "app/abstract/rb-set";
import { DataTarget } from "app/datamodel";
import { RbDatasetComponent } from "app/rb-dataset/rb-dataset.component";
import { RbTabSectionComponent } from "app/rb-tab-section/rb-tab-section.component";



export class LoadedView extends RbActivatorComponent {
    rootComponentRefs: ComponentRef<Component>[] = [];
    topSets: RbSetComponent[] = [];
    tabSections: RbTabSectionComponent[] = [];
  
    constructor(
      public name: string,
      public title: string,
      //public navigate: EventEmitter<any>
    ) {
      super();
    }
  
    activatorInit() {}
  
    activatorDestroy() {}
    
    onDatasetEvent(event: string) {}
  
    onActivationEvent(state: boolean) {}
  
    attachTo(container: ViewContainerRef) {
      for(let item of this.rootComponentRefs) {
        container.insert(item.hostView);
      }
      setTimeout(() => this.activate(), 1);
    }
  
  
    detachFrom(container: ViewContainerRef) {
      this.rootComponentRefs.forEach(item => {
        container.detach(container.indexOf(item.hostView))
      });
      setTimeout(() => this.deactivate(), 1);
    }
  
    clearData() {
      for(let set of this.topSets) {
        set.clear();
      }
    }
  
    setTarget(dataTarget: DataTarget) {
      if(dataTarget != null) {
        for(let dataset of this.topSets) {
          if(dataset.ignoretarget == false && (dataTarget.objectname == null || (dataTarget.objectname != null && dataTarget.objectname == dataset.objectname))) {
            dataset.setDataTarget(dataTarget);
          }
        }
      }
    }
  
    openTab(tabid: String) {
      for(let tabsection of this.tabSections) {
        for(let tab of tabsection.tabs) {
          if(tab.id == tabid || tab.label.toLowerCase() == tabid.toLowerCase()) {
            tabsection.selectTab(tab);
          }
        }
      }
    }
  
    forceRefresh() {
      for(let dataset of this.topSets) {
        dataset.refreshData();
      }    
    }
  
    getTopActiveDatasets() : RbDatasetComponent[] {
        let ret = [];
        for(let set of this.topSets) {
            if(set instanceof RbDatasetComponent && set.active) {
                ret.push(set);
            }
        }
        return ret;
    }
  
  }