import { Component, ViewChild, ViewContainerRef, ComponentRef, Compiler, ComponentFactory, NgModule, ModuleWithComponentFactories, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange, TypeDecorator } from '@angular/core';
import { __asyncDelegator } from 'tslib';
import { ApiService } from 'app/services/api.service';
import { DataService } from 'app/services/data.service';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { RbTabSectionComponent } from 'app/rb-tab-section/rb-tab-section.component';
import { RbTabComponent } from 'app/rb-tab/rb-tab.component';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbDatasetGroupComponent } from 'app/rb-datasetgroup/rb-datasetgroup.component';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { Injector } from '@angular/core';
import { RbAggregatesetComponent } from 'app/rb-aggregateset/rb-aggregateset.component';

import { RbFilesetComponent } from 'app/rb-fileset/rb-fileset.component';

import { DataTarget, ViewTarget } from 'app/datamodel';
import { componentRegistry } from './rb-view-loader-registry';
import { RbSetComponent } from 'app/abstract/rb-set';
import { HttpClient } from '@angular/common/http';
import { RbModalComponent } from 'app/rb-modal/rb-modal.component';


export class LoadedView extends RbActivatorComponent {
  title: string;
  rootComponentRefs: ComponentRef<Component>[] = [];
  topSets: RbSetComponent[] = [];

  constructor(t: string) {
    super();
    this.title = t;
    //this.active = true;
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
        if(dataset.ignoretarget == false && (dataTarget.objectname == null || (dataTarget.objectname != null && dataTarget.objectname == dataset.object))) {
          dataset.setDataTarget(dataTarget);
        }
      }
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
  private currentLoadedView: LoadedView = null;
  private viewCache: any = {};
  private factoryRegistry: any = {};

  constructor(
    private injector: Injector,
    private http: HttpClient,
    private apiService: ApiService,
    private dataService: DataService,
    private componentFactoryResolver:ComponentFactoryResolver
  ) { }

  ngOnInit() {
    for(let key of Object.keys(componentRegistry)) {
      let componentClass = componentRegistry[key];
      let factory = this.componentFactoryResolver.resolveComponentFactory(componentClass);
      this.factoryRegistry[key] = factory;
    }
  }

  ngOnChanges(changes : SimpleChange) {
    if("target" in changes && this.target != null) {
      let url: string = this.apiService.baseUrl + '/' + this.apiService.uiService + '/viewcc/' + this.target.version + '/' + this.target.view;
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
      entry = new LoadedView(config.label);
      if(config['content'] != null) {
        for(let item of config['content']) {
          let context: any = {activator: entry};
          entry.rootComponentRefs.push(this.buildConfigRecursive(null, item, context, entry));
        }
      }
      this.viewCache[hash] = entry;
      console.timeEnd('build');
    }
    if(entry != null) {
      this.currentLoadedView = entry;
      entry.setTarget(this.target.dataTarget);
      entry.attachTo(this.container);
    }
  }

  buildConfigRecursive(parent: ViewContainerRef, config: any, context: any, loadedView: LoadedView) {
    let newComponentRef: ComponentRef<Component> = null;
    let factory = this.factoryRegistry[config.type]; 
    if(factory != null) {
      if(parent != null) {
        newComponentRef = parent.createComponent(factory);
      } else {
        newComponentRef = factory.create(this.injector); 
      }
      let newInstance = newComponentRef.instance;
      var inputs = factory['componentDef']['declaredInputs'];
      for(var input of Object.keys(inputs)) {
        let val: any = null;
        if(input == 'dataset' && context['dataset'] != null && context['dataset'] instanceof RbDatasetComponent) {
          val = context['dataset'];
        } else if(input == 'datasetgroup' && context['datasetgroup'] != null && context['datasetgroup'] instanceof RbDatasetGroupComponent) {
          val = context['datasetgroup'];
        } else if(input == 'fileset' && context['fileset'] != null && context['fileset'] instanceof RbFilesetComponent) {
          val = context['fileset'];
        } else if(input == 'aggregateset' && context['aggregateset'] != null && context['aggregateset'] instanceof RbAggregatesetComponent) {
          val = context['aggregateset'];
        } else if(input == 'activator' && context['activator'] != null && context['activator'] instanceof RbActivatorComponent) {
          val = context['activator'];
        } else if(input == 'tabsection' && context['tabsection'] != null && context['tabsection'] instanceof RbTabSectionComponent) {
          val = context['tabsection'];
        } else if(config[input] != null) {
          val = config[input];
        }
        if(val != null) {
          newInstance[inputs[input]] = val;
        }
      };
      if(newInstance instanceof RbSetComponent && newInstance['master'] == null) {
        loadedView.topSets.push(newInstance);
      }
      var outputs: any = factory['componentDef']['outputs'];
      if(outputs['navigate'] != null && newInstance['navigate'] != null && newInstance['navigate'].subscribe != null) {
        newInstance['navigate'].subscribe(e => this.navigate.emit(e))
      }
      if(config['content'] != null) {
        if(newInstance instanceof RbContainerComponent && newInstance.container != null) {
          let newContext = Object.assign({}, context);
          if(newInstance instanceof RbDatasetComponent) {
            newContext['dataset'] = newInstance;
          } else if(newInstance instanceof RbDatasetGroupComponent) {
            newContext['datasetgroup'] = newInstance;
          } else if(newInstance instanceof RbFilesetComponent) {
            newContext['fileset'] = newInstance;
          } else if(newInstance instanceof RbAggregatesetComponent) {
            newContext['aggregateset'] = newInstance;
          } else if(newInstance instanceof RbTabComponent) {
            newContext['activator'] = newInstance;
          } else if(newInstance instanceof RbModalComponent) {
            newContext['activator'] = newInstance;
          } else if(newInstance instanceof RbTabSectionComponent) {
            newContext['tabsection'] = newInstance;
          }
          let newContainer: RbContainerComponent = <RbContainerComponent>newInstance;
          for(let childConfig of config['content']) {
            this.buildConfigRecursive(newContainer.container, childConfig, newContext, loadedView);
          };
        } else {
          console.log('Type ' + config.type + ' has contents but is not a RbContainerComponent');
        }
      }
    }
    return newComponentRef;
  }

}
