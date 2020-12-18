import { Component, ViewChild, ViewContainerRef, ComponentRef, Compiler, ComponentFactory, NgModule, ModuleWithComponentFactories, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange, TypeDecorator } from '@angular/core';
import { Http } from '@angular/http';
import { __asyncDelegator } from 'tslib';
import { CommonModule } from '@angular/common';
import { RedbackModule } from '../redback.module';
import { ApiService } from 'app/api.service';
import { Target } from 'app/desktop-root/desktop-root.component';
import { ViewContainerComponent } from './rb-view-container.component';
import { DataService } from 'app/data.service';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { RbLayoutComponent } from 'app/rb-layout/rb-layout.component';
import { RbHsectionComponent } from 'app/rb-hsection/rb-hsection.component';
import { RbVsectionComponent } from 'app/rb-vsection/rb-vsection.component';
import { RbContainerComponent } from 'app/rb-container/rb-container.component';
import { RbTabSectionComponent } from 'app/rb-tab-section/rb-tab-section.component';
import { RbTabComponent } from 'app/rb-tab/rb-tab.component';
import { UserprefService } from 'app/userpref.service';



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
  private factoryCache: any = {};
  private mode: number = 1;

  private registry = {
    "dataset": RbDatasetComponent,
    "layout": RbLayoutComponent,
    "hsection": RbHsectionComponent,
    "vsection": RbVsectionComponent,
    "tabsection": RbTabSectionComponent,
    "tab": RbTabComponent
  }

  constructor(
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
    let label = config.label;
    if(config['content'] != null) {
      config['content'].forEach(item => {
        this.buildConfigRecursive(this.container, item);
      });
    }
  }

  buildConfigRecursive(parentContainerRef: ViewContainerRef, config: any) {
    let componentClass = this.registry[config.type];
    if(componentClass != null) {
      let factory = this.componentFactoryResolver.resolveComponentFactory(componentClass);
      if(factory != null) {
        let newComponentRef: ComponentRef<Component> = parentContainerRef.createComponent(factory);
        if(config['content'] != null) {
          let newContainer: RbContainerComponent = <RbContainerComponent>newComponentRef.instance;
          Object.keys(newComponentRef.instance).forEach(key => {
            console.log(key);
          });
          newContainer.afterViewInit(() => {
            if(newContainer.container != null) {
              config['content'].forEach(childConfig => {
                this.buildConfigRecursive(newContainer.container, childConfig);
              });
            }
          });
        }
      }
    }
    

  }

}
