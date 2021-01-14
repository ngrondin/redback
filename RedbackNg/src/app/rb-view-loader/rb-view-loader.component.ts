import { Component, ViewChild, ViewContainerRef, ComponentRef, Compiler, ComponentFactory, NgModule, ModuleWithComponentFactories, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange, TypeDecorator } from '@angular/core';
import { Http } from '@angular/http';
import { __asyncDelegator } from 'tslib';
import { CommonModule } from '@angular/common';
import { RedbackModule } from '../redback.module';
import { ApiService } from 'app/services/api.service';
import { Target } from 'app/desktop-root/desktop-root.component';
import { ViewContainerComponent } from './rb-view-container.component';
import { DataService } from 'app/services/data.service';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { RbLayoutComponent } from 'app/rb-layout/rb-layout.component';
import { RbHsectionComponent } from 'app/rb-hsection/rb-hsection.component';
import { RbVsectionComponent } from 'app/rb-vsection/rb-vsection.component';
import { RbTabSectionComponent } from 'app/rb-tab-section/rb-tab-section.component';
import { RbTabComponent } from 'app/rb-tab/rb-tab.component';
import { UserprefService } from 'app/services/userpref.service';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbInputComponent } from 'app/inputs/rb-input/rb-input.component';
import { RbRelatedInputComponent } from 'app/inputs/rb-related-input/rb-related-input.component';
import { RbFormComponent } from 'app/rb-form/rb-form.component';
import { RbList4Component } from 'app/rb-list4/rb-list4.component';
import { RbListComponent } from 'app/rb-list/rb-list.component';
import { RbButtonComponent } from 'app/rb-button/rb-button.component';
import { RbSearchComponent } from 'app/rb-search/rb-search.component';
import { RbDatasetGroupComponent } from 'app/rb-datasetgroup/rb-datasetgroup.component';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { Injector } from '@angular/core';
import { RbActiongroupComponent } from 'app/rb-actiongroup/rb-actiongroup.component';
import { RbAddressInputComponent } from 'app/inputs/rb-address-input/rb-address-input.component';
import { RbAggregatesetComponent } from 'app/rb-aggregateset/rb-aggregateset.component';
import { RbChoiceInputComponent } from 'app/inputs/rb-choice-input/rb-choice-input.component';
import { RbCodeInputComponent } from 'app/inputs/rb-code-input/rb-code-input.component';
import { RbCurrencyInputComponent } from 'app/inputs/rb-currency-input/rb-currency-input.component';
import { RbDurationInputComponent } from 'app/inputs/rb-duration-input/rb-duration-input.component';
import { RbDynamicformComponent } from 'app/rb-dynamicform/rb-dynamicform.component';
import { RbFileInputComponent } from 'app/inputs/rb-file-input/rb-file-input.component';
import { RbFiledropComponent } from 'app/rb-filedrop/rb-filedrop.component';
import { RbFilelistComponent } from 'app/rb-filelist/rb-filelist.component';
import { RbFilesetComponent } from 'app/rb-fileset/rb-fileset.component';
import { RbTextareaInputComponent } from 'app/inputs/rb-textarea-input/rb-textarea-input.component';
import { RbDatetimeInputComponent } from 'app/inputs/rb-datetime-input/rb-datetime-input.component';
import { RbGanttComponent } from 'app/rb-gantt/rb-gantt.component';
import { RbLinkComponent } from 'app/rb-link/rb-link.component';
import { RbLogComponent } from 'app/rb-log/rb-log.component';
import { RbMapComponent } from 'app/rb-map/rb-map.component';
import { RbModalComponent } from 'app/rb-modal/rb-modal.component';
import { RbProcessactionsComponent } from 'app/rb-processactions/rb-processactions.component';
import { RbSwitchInputComponent } from 'app/inputs/rb-switch-input/rb-switch-input.component';
import { RbTableComponent } from 'app/rb-table/rb-table.component';
import { RbVcollapseComponent } from 'app/rb-vcollapse/rb-vcollapse.component';
import { RbHseparatorComponent } from 'app/rb-hseparator/rb-hseparator.component';
import { RbSpacerComponent } from 'app/rb-spacer/rb-spacer.component';



@Component({
  selector: 'rb-view-loader',
  templateUrl: './rb-view-loader.component.html',
  styleUrls: ['./rb-view-loader.component.css']
})
export class RbViewLoaderComponent implements OnInit {

  @Input('target') private target: Target;
  @ViewChild('container', { read: ViewContainerRef, static: true }) container: ViewContainerRef;
  @Output() navigate: EventEmitter<any> = new EventEmitter();
  @Output() titlechange: EventEmitter<any> = new EventEmitter();

  private currentView: string;
  private componentRef: ComponentRef<ViewContainerComponent>;
  private rootComponentRefs: ComponentRef<Component>[] = [];
  private factoryCache: any = {};
  private viewCache: any = {};
  private mode: number = 2;

  private registry = {
    "dataset": RbDatasetComponent,
    "layout": RbLayoutComponent,
    "form": RbFormComponent,
    "hsection": RbHsectionComponent,
    "vsection": RbVsectionComponent,
    "tabsection": RbTabSectionComponent,
    "tab": RbTabComponent,
    "input": RbInputComponent,
    "relatedinput": RbRelatedInputComponent,
    "list3": RbListComponent,
    "list4": RbList4Component,
    "button": RbButtonComponent,
    "search": RbSearchComponent,
    "actiongroup": RbActiongroupComponent,
    "addressinput": RbAddressInputComponent,
    "aggregateset": RbAggregatesetComponent,
    "choiceinput": RbChoiceInputComponent,
    "codeinput": RbCodeInputComponent,
    "currencyinput": RbCurrencyInputComponent,
    "durationinput": RbDurationInputComponent,
    "dynamicform": RbDynamicformComponent,
    "fileinput": RbFileInputComponent,
    "filedrop": RbFiledropComponent,
    "filelist": RbFilelistComponent,
    "fileset": RbFilesetComponent,
    "textarea": RbTextareaInputComponent,
    "datepicker": RbDatetimeInputComponent,
    "dateinput": RbTextareaInputComponent,
    "gantt": RbGanttComponent,
    "link": RbLinkComponent,
    "log": RbLogComponent,
    "map": RbMapComponent,
    "modal": RbModalComponent,
    "processactionsbutton": RbProcessactionsComponent,
    "switch": RbSwitchInputComponent,
    "table": RbTableComponent,
    "vcollapse": RbVcollapseComponent,
    "hseparator": RbHseparatorComponent,
    "spacer": RbSpacerComponent
  }

  constructor(
    private injector: Injector,
    private http: Http,
    private compiler: Compiler,
    private apiService: ApiService,
    private dataService: DataService,
    private userprefService: UserprefService,
    private componentFactoryResolver:ComponentFactoryResolver
  ) { }

  ngOnInit() {
  }

  ngOnChanges(changes : SimpleChange) {
    if("target" in changes && this.target != null) {
      if(this.target.view != this.currentView) {
        this.dataService.clearAllLocalObject();
        if(this.mode == 1) {
          let url: string = this.apiService.baseUrl + '/' + this.apiService.uiService + '/' + this.target.type + '/' + this.target.version + '/' + this.target.view;
          this.http.get(url, { withCredentials: true, responseType: 0 }).subscribe(
            res => {
              this.userprefService.setCurrentView(this.target.view);
              this.compileTemplate(res.text());
            }
          );
        } else if(this.mode == 2) {
          let url: string = this.apiService.baseUrl + '/' + this.apiService.uiService + '/viewcc/' + this.target.version + '/' + this.target.view;
          this.http.get(url, { withCredentials: true, responseType: 0 }).subscribe(
            res => {
              this.buildConfig(res.json())
            }
          );
        }
        this.currentView = this.target.view;
      } else if(this.componentRef != null) {
        this.componentRef.instance.currentTarget = this.target;
      } 
    } 
  }

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


  buildConfig(config: any) {
    this.rootComponentRefs.forEach(item => this.container.detach(this.container.indexOf(item.hostView)));
    let hash = JSON.stringify(config).split("").reduce(function(a,b){a=((a<<5)-a)+b.charCodeAt(0);return a&a},0);              
    this.rootComponentRefs = this.viewCache[hash];
    if(this.rootComponentRefs == null) {
      this.rootComponentRefs = [];
      let context: any = {};
      if(config['content'] != null) {
        for(let item of config['content']) {
          this.rootComponentRefs.push(this.buildConfigRecursive(item, context));
        }
      }
      this.viewCache[hash] = this.rootComponentRefs;
    }
    if(this.rootComponentRefs != null) {
      for(let item of this.rootComponentRefs) {
        this.container.insert(item.hostView);
      }
    }
    this.target.title = config.label;
  }

  buildConfigRecursive(config: any, context: any) {
    let newComponentRef: ComponentRef<Component> = null;
    let componentClass = this.registry[config.type];
    if(componentClass != null) {
      let factory = this.componentFactoryResolver.resolveComponentFactory(componentClass);
      if(factory != null) {
        newComponentRef = factory.create(this.injector); 
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
              let childComponentRef: ComponentRef<Component> = this.buildConfigRecursive(childConfig, newContext);
              if(childComponentRef != null) {
                newContainer.container.insert(childComponentRef.hostView);
              }
            };
          } else {
            console.log('Type ' + config.type + ' has contents but is not a RbContainerComponent');
          }
        }
      }
    } else {
      console.log('Type ' + config.type + ' has no component');
    }
    return newComponentRef;
  }

}
