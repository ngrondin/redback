import { Component, ViewChild, ViewContainerRef, ComponentRef, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange, TypeDecorator } from '@angular/core';
import { __asyncDelegator } from 'tslib';
import { ApiService } from 'app/services/api.service';
import { DataService } from 'app/services/data.service';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { Injector } from '@angular/core';
import { DataTarget, NavigateData } from 'app/datamodel';
import { componentRegistry } from './rb-view-loader-registry';
import { RbSetComponent } from 'app/abstract/rb-set';
import { HttpClient } from '@angular/common/http';7
import { BuildService } from 'app/services/build.service';
import { RbTabSectionComponent } from 'app/rb-tab-section/rb-tab-section.component';
import { NavigateService } from 'app/services/navigate.service';


export class LoadedView extends RbActivatorComponent {
  rootComponentRefs: ComponentRef<Component>[] = [];
  topSets: RbSetComponent[] = [];
  tabSections: RbTabSectionComponent[] = [];

  constructor(
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
    this.activate();
  }


  detachFrom(container: ViewContainerRef) {
    this.rootComponentRefs.forEach(item => {
      container.detach(container.indexOf(item.hostView))
    });
    this.deactivate();
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
          console.log("Open tab");
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

}


@Component({
  selector: 'rb-view-loader',
  templateUrl: './rb-view-loader.component.html',
  styleUrls: ['./rb-view-loader.component.css']
})
export class RbViewLoaderComponent implements OnInit {
  @Input('name') name = 'default';
  @ViewChild('container', { read: ViewContainerRef, static: true }) container: ViewContainerRef;

  public currentLoadedView: LoadedView = null;
  private viewCache: any = {};
  private factoryRegistry: any = {};

  constructor(
    private apiService: ApiService,
    private componentFactoryResolver:ComponentFactoryResolver,
    private buildService: BuildService,
    private navigateService: NavigateService
  ) { 
  }

  ngOnInit() {
    for(let key of Object.keys(componentRegistry)) {
      let componentClass = componentRegistry[key];
      let factory = this.componentFactoryResolver.resolveComponentFactory(componentClass);
      this.factoryRegistry[key] = factory;
    }
    this.navigateService.registerTarget(this.name, this);
  }

  navigateTo(navData: NavigateData): Promise<void> {
    return new Promise((resolve, reject) => {
      this.apiService.getView(navData.view, navData.domain).subscribe(resp => {
        this.showView(navData, resp);
        resolve();
      });
    })
  }

  showView(navData: NavigateData, viewConfig: any) {
    if(this.currentLoadedView != null) {
      this.currentLoadedView.detachFrom(this.container);
    }
    navData.title = viewConfig.label;
    let hash = JSON.stringify(viewConfig).split("").reduce(function(a,b){a=((a<<5)-a)+b.charCodeAt(0);return a&a},0);      
    let entry: LoadedView = this.viewCache[hash];
    if(entry == null) {
      console.time('build');
      entry = new LoadedView(viewConfig.label/*, this.navigate*/);
      if(viewConfig['content'] != null) {
        for(let item of viewConfig['content']) {
          let context: any = {activator: entry};
          entry.rootComponentRefs.push(this.buildService.buildConfigRecursive(null, item, context, entry));
        }
      }
      if(viewConfig.onload != null) {
        Function(viewConfig.onload).call(window.redback);
      }
      this.viewCache[hash] = entry;
      console.timeEnd('build');
    }
    if(entry != null) {
      this.currentLoadedView = entry;
      window.redback.currentLoadedView = entry;
      entry.setTarget(navData.dataTarget);
      if(navData.tab != null) entry.openTab(navData.tab);
      entry.attachTo(this.container);
      console.log('attach');
    }
  }

  openTab(tabid: string) {
    if(this.currentLoadedView != null) {
      console.log('opentab');
      this.currentLoadedView.openTab(tabid);
    }
  }
  
}
