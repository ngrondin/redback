import { Component, ViewChild, ViewContainerRef, ComponentRef, Compiler, ComponentFactory, NgModule, ModuleWithComponentFactories, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange } from '@angular/core';
import { Http } from '@angular/http';
import { __asyncDelegator } from 'tslib';
import { CommonModule } from '@angular/common';
import { RedbackModule } from '../redback.module';
import { ApiService } from 'app/api.service';
import { Target } from 'app/desktop-root/desktop-root.component';
import { RbObject } from 'app/datamodel';


@Component({
  selector: 'rb-view-loader',
  templateUrl: './rb-view-loader.component.html',
  styleUrls: ['./rb-view-loader.component.css']
})
export class RbViewLoaderComponent implements OnInit {

  @Input('target') private target: Target;
  @ViewChild('container', { read: ViewContainerRef, static: false }) container: ViewContainerRef;
  @Output() navigate: EventEmitter<any> = new EventEmitter();
  @Output() titlechange: EventEmitter<any> = new EventEmitter();

  //private currentUrl: string;
  private currentView: string;
  //private currentFilter: string;
  private componentRef: ComponentRef<any>;

  constructor(
    private http: Http,
    private resolver: ComponentFactoryResolver,
    private compiler: Compiler,
    private vcRef: ViewContainerRef,
    private apiService: ApiService
  ) { }

  ngOnInit() {
  }

  ngOnChanges(changes : SimpleChange) {
    if("target" in changes) {
      if(this.target.view != this.currentView) {
        let url: string = this.apiService.baseUrl + '/' + this.apiService.uiService + '/' + this.target.type + '/' + this.target.version + '/' + this.target.view;
        this.http.get(url, { withCredentials: true, responseType: 0 }).subscribe(
          res => this.compileTemplate(res.text())
        );
        this.currentView = this.target.view;
      } 
    } 
  }

  compileTemplate(body: string) {

    @Component({
      selector: 'rb-view-container',
      template: body
    })
    class ViewContainerComponent {
      @Output() navigate: EventEmitter<any> = new EventEmitter();
      @Output() titlechange: EventEmitter<any> = new EventEmitter();
      currentTarget: Target;
      navigateTo(target: any) {
        this.navigate.emit(target);
      }
      setTitle(title: string) {
        this.titlechange.emit(title);
      }
      rememberFilter(filter: any) {
        this.currentTarget.filter = filter;
      }
    };

    @NgModule({ 
      imports: [
        CommonModule,
        RedbackModule
      ], 
      declarations: [
        ViewContainerComponent
      ] 
    })
    class RuntimeComponentModule { }

    let module: ModuleWithComponentFactories<any> = this.compiler.compileModuleAndAllComponentsSync(RuntimeComponentModule);
    let factory = module.componentFactories.find(f => f.componentType === ViewContainerComponent);
    
    if (this.componentRef) {
      this.componentRef.destroy();
      this.componentRef = null; 
    }

    let newViewComponentRef : ComponentRef<ViewContainerComponent> = this.container.createComponent(factory);
    newViewComponentRef.instance.currentTarget = this.target;
    //newViewComponentRef.instance.initialUserFilter = this.target.filter;
    //newViewComponentRef.instance.initialSearch = this.target.search;
    //newViewComponentRef.instance.initialSelectedObject = this.target.selectedObject;
    newViewComponentRef.instance.navigate.subscribe(e => this.navigateTo(e));
    newViewComponentRef.instance.titlechange.subscribe(e => this.setTitle(e));
    this.componentRef = newViewComponentRef;
  }

  navigateTo($event) {
    this.navigate.emit($event);
  }

  setTitle(title: string) {
    this.titlechange.emit(title);
  }

}
