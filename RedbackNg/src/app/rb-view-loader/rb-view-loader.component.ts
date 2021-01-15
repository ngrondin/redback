import { Component, ViewChild, ViewContainerRef, ComponentRef, Compiler, ComponentFactory, NgModule, ModuleWithComponentFactories, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange, TypeDecorator } from '@angular/core';
import { Http } from '@angular/http';
import { __asyncDelegator } from 'tslib';
import { ApiService } from 'app/services/api.service';
import { DataService } from 'app/services/data.service';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { RbTabSectionComponent } from 'app/rb-tab-section/rb-tab-section.component';
import { RbTabComponent } from 'app/rb-tab/rb-tab.component';
import { UserprefService } from 'app/services/userpref.service';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbDatasetGroupComponent } from 'app/rb-datasetgroup/rb-datasetgroup.component';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { Injector } from '@angular/core';
import { RbAggregatesetComponent } from 'app/rb-aggregateset/rb-aggregateset.component';

import { RbFilesetComponent } from 'app/rb-fileset/rb-fileset.component';

import { DataTarget, ViewTarget } from 'app/datamodel';
import { componentRegistry } from './rb-view-loader-registry';


export class LoadedView {
  title: string;
  rootComponentRefs: ComponentRef<Component>[] = [];
  topDatasets: RbDatasetComponent[] = [];

  attach(container: ViewContainerRef) {
    for(let item of this.rootComponentRefs) {
      container.insert(item.hostView);
    }
  }

  detach(container: ViewContainerRef) {
    this.rootComponentRefs.forEach(item => {
      container.detach(container.indexOf(item.hostView))
    });
    this.clearData();
  }

  clearData() {
    for(let dataset of this.topDatasets) {
      dataset.clearData();
    }
  }

  resetData(dataTarget: DataTarget) {
    for(let dataset of this.topDatasets) {
      if(dataTarget != null && (dataTarget.objectname == null || (dataTarget.objectname == dataset.object))) {
        dataset.dataTarget = dataTarget;
      }
      setTimeout(() => dataset.reset(), 1);
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
  //@Output() titlechange: EventEmitter<any> = new EventEmitter();

  private currentView: string;
  //private componentRef: ComponentRef<ViewContainerComponent>;
  //private rootComponentRefs: ComponentRef<Component>[] = [];
  private currentLoadedView: LoadedView = null;
  //private factoryCache: any = {};
  private viewCache: any = {};
  //private mode: number = 2;
  private factoryRegistry: any = {};

  constructor(
    private injector: Injector,
    private http: Http,
    //private compiler: Compiler,
    private apiService: ApiService,
    private dataService: DataService,
    //private userprefService: UserprefService,
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
      if(this.target.view != this.currentView) {
        this.dataService.clearAllLocalObject();
        /*if(this.mode == 1) {
          let url: string = this.apiService.baseUrl + '/' + this.apiService.uiService + '/' + this.target.type + '/' + this.target.version + '/' + this.target.view;
          this.http.get(url, { withCredentials: true, responseType: 0 }).subscribe(
            res => {
              this.userprefService.setCurrentView(this.target.view);
              this.compileTemplate(res.text());
            }
          );
        } else if(this.mode == 2) {*/
          let url: string = this.apiService.baseUrl + '/' + this.apiService.uiService + '/viewcc/' + this.target.version + '/' + this.target.view;
          this.http.get(url, { withCredentials: true, responseType: 0 }).subscribe(
            res => {
              this.buildConfig(res.json())
            }
          );
        //}
        this.currentView = this.target.view;
      } /*else if(this.componentRef != null) {
        this.componentRef.instance.currentTarget = this.target;
      } */
    } 
  }

  /*
  compileTemplate(body: string) {
    let hash = body.split("").reduce(function(a,b){a=((a<<5)-a)+b.charCodeAt(0);return a&a},0);              
    let factory = this.factoryCache[hash];
    if(factory == null) {
      const componentClass = class RuntimeComponentClass extends ViewContainerComponent {};
      const typeDecorator: TypeDecorator = Component({ selector: 'rb-view-container', template: body});
      const decoratedComponent = typeDecorator(componentClass);
      const moduleClass = class RuntimeComponentModule { };
      const decoratedNgModule = NgModule({ imports: [CommonModule, RedbackModule], declarations: [decoratedComponent] })(moduleClass);
      const module: ModuleWithComponentFactories<any> = this.compiler.compileModuleAndAllComponentsSync(decoratedNgModule);
      factory = module.componentFactories.find(f => f.componentType == decoratedComponent);
      this.factoryCache[hash] = factory;
    } 
    
    if (this.componentRef) {
      this.componentRef.destroy();
      this.componentRef = null; 
    }

    let newViewComponentRef : ComponentRef<ViewContainerComponent> = this.container.createComponent(factory);
    newViewComponentRef.instance.currentTarget = this.target;
    newViewComponentRef.instance.navigate.subscribe(e => this.navigate.emit(e));
    this.componentRef = newViewComponentRef;
  }
*/

  buildConfig(config: any) {
    console.time('build');
    if(this.currentLoadedView != null) {
      this.currentLoadedView.detach(this.container);
    }
    let hash = JSON.stringify(config).split("").reduce(function(a,b){a=((a<<5)-a)+b.charCodeAt(0);return a&a},0);      
    let entry: LoadedView = this.viewCache[hash];
    if(entry == null) {
      entry = new LoadedView();
      entry.title = config.label;
      entry.topDatasets = [];
      if(config['content'] != null) {
        for(let item of config['content']) {
          entry.rootComponentRefs.push(this.buildConfigRecursive(null, item, {}, entry.topDatasets));
        }
      }
      this.viewCache[hash] = entry;
    }
    if(entry != null) {
      this.currentLoadedView = entry;
      entry.attach(this.container);
      entry.resetData(this.target.dataTarget);
      this.target.title = entry.title;
    }
    console.timeEnd('build');
  }

  buildConfigRecursive(parent: ViewContainerRef, config: any, context: any, topDatasets: RbDatasetComponent[]) {
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
        if(input == 'dataset' && context['dataprovider'] != null && context['dataprovider'] instanceof RbDatasetComponent) {
          val = context['dataprovider'];
        } else if(input == 'datasetgroup' && context['dataprovider'] != null && context['dataprovider'] instanceof RbDatasetGroupComponent) {
          val = context['dataprovider'];
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
      if(newInstance instanceof RbDatasetComponent && newInstance['master'] == null) {
        topDatasets.push(newInstance);
      }
      var outputs: any = factory['componentDef']['outputs'];
      if(outputs['navigate'] != null && newInstance['navigate'] != null && newInstance['navigate'].subscribe != null) {
        newInstance['navigate'].subscribe(e => this.navigate.emit(e))
      }
      if(config['content'] != null) {
        if(newInstance instanceof RbContainerComponent && newInstance.container != null) {
          let newContext = Object.assign({}, context);
          if(newInstance instanceof RbDatasetComponent) {
            newContext['dataprovider'] = newInstance;
          } else if(newInstance instanceof RbDatasetGroupComponent) {
            newContext['dataprovider'] = newInstance;
          } else if(newInstance instanceof RbFilesetComponent) {
            newContext['fileset'] = newInstance;
          } else if(newInstance instanceof RbAggregatesetComponent) {
            newContext['aggregateset'] = newInstance;
          } else if(newInstance instanceof RbTabComponent) {
            newContext['activator'] = newInstance;
          } else if(newInstance instanceof RbTabSectionComponent) {
            newContext['tabsection'] = newInstance;
          }
          let newContainer: RbContainerComponent = <RbContainerComponent>newInstance;
          for(let childConfig of config['content']) {
            this.buildConfigRecursive(newContainer.container, childConfig, newContext, topDatasets);
          };
        } else {
          console.log('Type ' + config.type + ' has contents but is not a RbContainerComponent');
        }
      }
    }
    return newComponentRef;
  }

}
