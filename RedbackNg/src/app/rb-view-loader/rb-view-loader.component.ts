import { Component, ViewChild, ViewContainerRef, ComponentRef, Compiler, ComponentFactory, NgModule, ModuleWithComponentFactories, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange } from '@angular/core';
import { Http } from '@angular/http';
import { __asyncDelegator } from 'tslib';
import { CommonModule } from '@angular/common';
import { RedbackModule } from '../redback.module';
import { ApiService } from 'app/api.service';


@Component({
  selector: 'rb-view-loader',
  templateUrl: './rb-view-loader.component.html',
  styleUrls: ['./rb-view-loader.component.css']
})
export class RbViewLoaderComponent implements OnInit {

  @Input('target') private target: any;
  @ViewChild('container', { read: ViewContainerRef, static: false }) container: ViewContainerRef;
  @Output() navigate: EventEmitter<any> = new EventEmitter();
  @Output() titlechange: EventEmitter<any> = new EventEmitter();

  private currentUrl: string;
  private currentFilter: string;
  private componentRef: ComponentRef<{setInitialUserFilter(filter:any);}>;

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
      if(this.target.url != this.currentUrl) {
        this.http.get(this.target.url, { withCredentials: true, responseType: 0 }).subscribe(
          res => this.compileTemplate(res.text())
        );
        this.currentUrl = this.target.url;
      } else if(this.target.userfilter != this.currentFilter) {
        this.componentRef.instance.setInitialUserFilter(this.target.userfilter);
        this.currentFilter = this.target.userfilter;
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
      initialUserFilter: any;
      navigateTo(target: any) {
        this.navigate.emit(target);
      }
      setTitle(title: string) {
        this.titlechange.emit(title);
      }
      setInitialUserFilter(filter: any) {
        this.initialUserFilter = filter;
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
    newViewComponentRef.instance.setInitialUserFilter(this.target.userfilter);
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
