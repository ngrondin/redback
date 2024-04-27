import { Component, ViewChild, ViewContainerRef, ComponentRef, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange, TypeDecorator } from '@angular/core';
import { __asyncDelegator } from 'tslib';
import { ApiService } from 'app/services/api.service';
import { DataService } from 'app/services/data.service';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { Injector } from '@angular/core';
import { DataTarget, ViewTarget } from 'app/datamodel';
import { componentRegistry } from './rb-view-loader-registry';
import { RbSetComponent } from 'app/abstract/rb-set';
import { HttpClient } from '@angular/common/http';7
import { BuildService } from 'app/services/build.service';
import { RbTabSectionComponent } from 'app/rb-tab-section/rb-tab-section.component';


export class LoadedView extends RbActivatorComponent {
  rootComponentRefs: ComponentRef<Component>[] = [];
  topSets: RbSetComponent[] = [];
  topTabs: RbTabSectionComponent[] = [];

  constructor(
    public title: string,
    public navigate: EventEmitter<any>
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
    for(let tabsection of this.topTabs) {
      for(let tab of tabsection.tabs) {
        if(tab.id == tabid || tab.label == tabid) {
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

  @Input('target') private target: ViewTarget;
  @ViewChild('container', { read: ViewContainerRef, static: true }) container: ViewContainerRef;
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  private currentView: string;
  public currentLoadedView: LoadedView = null;
  private viewCache: any = {};
  private factoryRegistry: any = {};

  constructor(
    private injector: Injector,
    private http: HttpClient,
    private apiService: ApiService,
    private dataService: DataService,
    private componentFactoryResolver:ComponentFactoryResolver,
    private buildService: BuildService
  ) { 
  }

  ngOnInit() {
    for(let key of Object.keys(componentRegistry)) {
      let componentClass = componentRegistry[key];
      let factory = this.componentFactoryResolver.resolveComponentFactory(componentClass);
      this.factoryRegistry[key] = factory;
    }
  }

  ngOnChanges(changes : SimpleChange) {
    if("target" in changes && this.target != null) {
      let url: string = this.apiService.baseUrl + '/' + this.apiService.uiService + '/view/' + (this.target.domain != null ? this.target.domain + '/' : '') + this.target.view;
      this.http.get(url, { withCredentials: true, responseType: 'json' }).subscribe(
        resp => {
          this.showView(resp)
        }
      );
      this.currentView = this.target.view;
    } 
  }

  showView(config: any) {
    if(this.currentLoadedView != null) {
      this.currentLoadedView.detachFrom(this.container);
    }
    this.target.title = config.label;
    let hash = JSON.stringify(config).split("").reduce(function(a,b){a=((a<<5)-a)+b.charCodeAt(0);return a&a},0);      
    let entry: LoadedView = this.viewCache[hash];
    if(entry == null) {
      console.time('build');
      entry = new LoadedView(config.label, this.navigate);
      if(config['content'] != null) {
        for(let item of config['content']) {
          let context: any = {activator: entry};
          entry.rootComponentRefs.push(this.buildService.buildConfigRecursive(null, item, context, entry));
        }
      }
      if(config.onload != null) {
        Function(config.onload).call(window.redback);
      }
      this.viewCache[hash] = entry;
      console.timeEnd('build');
    }
    if(entry != null) {
      this.currentLoadedView = entry;
      window.redback.currentLoadedView = entry;
      entry.setTarget(this.target.dataTarget);
      if(this.target.tab != null) entry.openTab(this.target.tab);
      entry.attachTo(this.container);
    }
  }

  
}
