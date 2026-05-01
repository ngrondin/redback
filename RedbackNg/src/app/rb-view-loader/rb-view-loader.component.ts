import { Component, ViewChild, ViewContainerRef, ComponentRef, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange, TypeDecorator } from '@angular/core';
import { __asyncDelegator } from 'tslib';
import { ApiService } from 'app/services/api.service';
import { NavigateBackData } from 'app/datamodel';
import { componentRegistry } from './rb-view-loader-registry';
import { BuildService } from 'app/services/build.service';
import { NavigateService } from 'app/services/navigate.service';
import { LoadedView } from './rb-view-loader-model';
import { LogService } from 'app/services/log.service';



@Component({
  selector: 'rb-view-loader',
  templateUrl: './rb-view-loader.component.html',
  styleUrls: ['./rb-view-loader.component.css']
})
export class RbViewLoaderComponent implements OnInit {
  @Input('name') name = 'default';
  @ViewChild('container', { read: ViewContainerRef, static: true }) container?: ViewContainerRef;

  public currentLoadedView: LoadedView | null = null;
  private viewCache: any = {};
  private factoryRegistry: any = {};

  constructor(
    private apiService: ApiService,
    private componentFactoryResolver:ComponentFactoryResolver,
    private buildService: BuildService,
    private navigateService: NavigateService,
    private logService: LogService
  ) { 
  }

  ngOnInit() {
    for(let key of Object.keys(componentRegistry)) {
      let componentClass = componentRegistry[key];
      let factory = this.componentFactoryResolver.resolveComponentFactory(componentClass);
      this.factoryRegistry[key] = factory;
    }
    this.navigateService.registerTarget(this.name, this);
  }

  navigateTo(viewName: string): Promise<void> {
    return new Promise((resolve, reject) => {
      this.apiService.getView(viewName).subscribe({
        next: (resp) => {
          this.showView(viewName, resp);
          resolve();
        },
        error: (err) => reject(err),
        complete: () => resolve()
      });
    })
  }

  showView(viewName: string, viewConfig: any) {
    if(this.currentLoadedView != null) {
      this.currentLoadedView.detachFrom(this.container!);
    }
    //navData.title = viewConfig.label;
    let hash = JSON.stringify(viewConfig).split("").reduce(function(a,b){a=((a<<5)-a)+b.charCodeAt(0);return a&a},0);      
    let entry: LoadedView = this.viewCache[hash];
    if(entry == null) {
      entry = new LoadedView(viewName, viewConfig.label);
      if(viewConfig['content'] != null) {
        for(let item of viewConfig['content']) {
          let context: any = {activator: entry};
          entry.rootComponentRefs.push(this.buildService.buildConfigRecursive(null, item, context, entry));
        }
      }
      if(viewConfig.onload != null) {
        Function(viewConfig.onload).call(window.redback);
      }
      this.viewCache[hash] = entry;
    }
    if(entry != null) {
      this.currentLoadedView = entry;
      window.redback.currentLoadedView = entry;
      //let newEmptyDataTargets = entry.setDataTargets(navData.dataTargets);
      //newEmptyDataTargets.forEach(target => navData.dataTargets.push(target));      
      //entry.setCompTargets(navData.compTargets);
      entry.attachTo(this.container!);
    }
    this.logService.info("ViewLoader - Loaded view " + viewName);
  }
  
}
