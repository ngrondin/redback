import { Component, ViewChild, ViewContainerRef, ComponentRef, Compiler, ComponentFactory, NgModule, ModuleWithComponentFactories, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange, TypeDecorator } from '@angular/core';
import { Http } from '@angular/http';
import { __asyncDelegator } from 'tslib';
import { CommonModule } from '@angular/common';
import { RedbackModule } from '../redback.module';
import { ApiService } from 'app/api.service';
import { Target } from 'app/desktop-root/desktop-root.component';
import { ViewContainerComponent } from './rb-view-container.component';


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

  constructor(
    private http: Http,
    private compiler: Compiler,
    private apiService: ApiService
  ) { }

  ngOnInit() {
  }

  ngOnChanges(changes : SimpleChange) {
    if("target" in changes && this.target != null) {
      if(this.target.view != this.currentView) {
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

    const typeDecorator: TypeDecorator = Component({ selector: 'rb-view-container', template: body});
    const decoratedComponent = typeDecorator(ViewContainerComponent);
    const moduleClass = class RuntimeComponentModule { };
    const decoratedNgModule = NgModule({ imports: [CommonModule, RedbackModule], declarations: [decoratedComponent] })(moduleClass);
    const module: ModuleWithComponentFactories<any> = this.compiler.compileModuleAndAllComponentsSync(decoratedNgModule);
    let factory = module.componentFactories.find(f => f.componentType == decoratedComponent);

    if (this.componentRef) {
      this.componentRef.destroy();
      this.componentRef = null; 
    }

    let newViewComponentRef : ComponentRef<ViewContainerComponent> = this.container.createComponent(factory);
    newViewComponentRef.instance.currentTarget = this.target;
    newViewComponentRef.instance.navigate.subscribe(e => this.navigate.emit(e));
    //newViewComponentRef.instance.titlechange.subscribe(e => this.titlechange.emit(e))
    this.componentRef = newViewComponentRef;
  }

  /*
  navigateTo($event) {
    this.navigate.emit($event);
  }
  */
}
