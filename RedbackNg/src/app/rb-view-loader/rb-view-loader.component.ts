import { Component, ViewChild, ViewContainerRef, ComponentRef, Compiler, ComponentFactory, NgModule, ModuleWithComponentFactories, ComponentFactoryResolver, OnInit, Input } from '@angular/core';
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

  private componentRef: ComponentRef<{}>;

  constructor(
    private http: Http,
    private resolver: ComponentFactoryResolver,
    private compiler: Compiler
  ) { }

  ngOnInit() {
    this.http.get(this.templateUrl, { withCredentials: true, responseType: 0 }).subscribe(res => this.compileTemplate(res.text()));
  }

  compileTemplate(body: string) {

    @Component({
      selector: 'dynamic-component',
      template: body
    })
    class RuntimeComponent { };

    @NgModule({ 
      imports: [
        CommonModule,
        RedbackModule
      ], 
      declarations: [
        RuntimeComponent
      ] 
    })
    class RuntimeComponentModule { }

    let module: ModuleWithComponentFactories<any> = this.compiler.compileModuleAndAllComponentsSync(RuntimeComponentModule);
    let factory = module.componentFactories.find(f => f.componentType === RuntimeComponent);
    
    if (this.componentRef) {
      this.componentRef.destroy();
      this.componentRef = null;
    }

    this.componentRef = this.container.createComponent(factory);
  }


}
