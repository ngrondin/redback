import { Component, ViewChild, ViewContainerRef, ComponentRef, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange, TypeDecorator } from '@angular/core';
import { __asyncDelegator } from 'tslib';
import { ApiService } from 'app/services/api.service';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { DataTarget, NavigateData } from 'app/datamodel';
import { componentRegistry } from './rb-view-loader-registry';
import { RbSetComponent } from 'app/abstract/rb-set';
import { BuildService } from 'app/services/build.service';
import { RbTabSectionComponent } from 'app/rb-tab-section/rb-tab-section.component';
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
  @ViewChild('container', { read: ViewContainerRef, static: true }) container: ViewContainerRef;

  public currentLoadedView: LoadedView = null;
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

  navigateTo(navData: NavigateData): Promise<void> {
    return new Promise((resolve, reject) => {
      this.apiService.getView(navData.view, navData.domain).subscribe({
        next: (resp) => {
          this.showView(navData, resp);
          resolve();
        },
        error: (err) => reject(err),
        complete: () => resolve()
      });
    })
  }

  showView(navData: NavigateData, viewConfig: any) {
    if(this.currentLoadedView != null) {
      this.currentLoadedView.detachFrom(this.container);
    }
    navData.title = viewConfig.label;
    let hash = JSON.stringify(viewConfig).split("").reduce(function(a,b){a=((a<<5)-a)+b.charCodeAt(0);return a&a},0);      
    let entry: LoadedView = this.viewCache[hash];
    if(entry == null) {
      entry = new LoadedView(navData.view, viewConfig.label);
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
      entry.setTarget(navData.dataTargets);
      if(navData.tab != null) entry.openTab(navData.tab);
      entry.attachTo(this.container);
    }
    this.logService.info("ViewLoader - Loaded view " + navData.view);
  }
  
}
