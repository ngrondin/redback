import { Component, ViewChild, ViewContainerRef, ComponentRef, Compiler, ComponentFactory, NgModule, ModuleWithComponentFactories, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange } from '@angular/core';
import { Headers, Http, Response } from '@angular/http';
import { __asyncDelegator } from 'tslib';
import { CommonModule } from '@angular/common';
import { RedbackModule } from '../redback.module';
import { MatListModule } from '@angular/material';


@Component({
  selector: 'rb-view-loader',
  templateUrl: './rb-view-loader.component.html',
  styleUrls: ['./rb-view-loader.component.css']
})
export class RbViewLoaderComponent implements OnInit {

  @Input('src') private templateUrl: string;
  @ViewChild('container', { read: ViewContainerRef, static: false }) container: ViewContainerRef;
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  private componentRef: ComponentRef<{}>;

  constructor(
    private http: Http,
    private resolver: ComponentFactoryResolver,
    private compiler: Compiler,
    private vcRef: ViewContainerRef
  ) { }

  ngOnInit() {
  }

  ngOnChanges(changes : SimpleChange) {
    this.http.get(this.templateUrl, { withCredentials: true, responseType: 0 }).subscribe(res => this.compileTemplate(res.text()));
  }

  compileTemplate(body: string) {

    @Component({
      selector: 'rb-view',
      template: body
    })
    class ViewComponent {
      @Output() navigate: EventEmitter<any> = new EventEmitter();
      navigateTo(view : string, title : string) {
        this.navigate.emit({view : view, title : title});
      }
    };

    @NgModule({ 
      imports: [
        CommonModule,
        RedbackModule
      ], 
      declarations: [
        ViewComponent
      ] 
    })
    class RuntimeComponentModule { }

    let module: ModuleWithComponentFactories<any> = this.compiler.compileModuleAndAllComponentsSync(RuntimeComponentModule);
    let factory = module.componentFactories.find(f => f.componentType === ViewComponent);
    
    if (this.componentRef) {
      this.componentRef.destroy();
      this.componentRef = null;
    }

    let newViewComponentRef : ComponentRef<ViewComponent> = this.container.createComponent(factory);
    newViewComponentRef.instance.navigate.subscribe(e => this.navigateTo(e));
    this.componentRef = newViewComponentRef;
  }

  navigateTo($event) {
    this.navigate.emit($event);
  }

}
