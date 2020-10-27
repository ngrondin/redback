import { Component, ViewChild, ViewContainerRef, ComponentRef, Compiler, ComponentFactory, NgModule, ModuleWithComponentFactories, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange, TypeDecorator } from '@angular/core';
import { Http } from '@angular/http';
import { __asyncDelegator } from 'tslib';
import { CommonModule } from '@angular/common';
import { RedbackModule } from '../redback.module';
import { ApiService } from 'app/api.service';
import { Target } from 'app/desktop-root/desktop-root.component';
import { ViewContainerComponent } from './rb-view-container.component';
import { DataService } from 'app/data.service';



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

  constructor(
    private http: Http,
    private compiler: Compiler,
    private apiService: ApiService,
    private dataService: DataService
  ) { }

  ngOnInit() {
  }

  ngOnChanges(changes : SimpleChange) {
    if("target" in changes && this.target != null) {
      if(this.target.view != this.currentView) {
        this.dataService.clearAllLocalObject();
        let url: string = this.apiService.baseUrl + '/' + this.apiService.uiService + '/' + this.target.type + '/' + this.target.version + '/' + this.target.view;
        this.http.get(url, { withCredentials: true, responseType: 0 }).subscribe(
          res => this.compileTemplate(res.text())
        );
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
      //const decoratedComponent = typeDecorator(ViewContainerComponent);
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

}
