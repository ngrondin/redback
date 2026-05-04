import { ComponentFactoryResolver, Injectable } from '@angular/core';
import { NavigateEvent, NavigateBackData, NavigateEventDataTarget, NavigateEventCompTarget } from 'app/datamodel';
import { RbViewLoaderComponent } from 'app/rb-view-loader/rb-view-loader.component';
import { ConfigService } from './config.service';
import { UserprefService } from './userpref.service';
import { Observable, Observer } from 'rxjs';
import { ModalService } from './modal.service';
import { LogService } from './log.service';
import { ApiService } from './api.service';
import { componentRegistry, LoadedView } from 'app/loader';
import { BuildService } from './build.service';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { sleep } from 'app/helpers';

class Target {
  component: RbViewLoaderComponent;
  stack: NavigateBackData[] = [];
  title?: string;

  constructor(c: RbViewLoaderComponent) {
    this.component = c;
  }


}

@Injectable({
  providedIn: 'root'
})
export class NavigateService {
  targets: {[key: string]: Target} = {};
  navigateObservers: Observer<NavigateEvent>[] = [];
  viewCache: any = {};
  factoryRegistry: any = {};


  constructor(
    private configService: ConfigService,
    private apiService: ApiService,
    private userprefService: UserprefService,
    private modalService: ModalService,
    private logService: LogService,
    private buildService: BuildService,
    private componentFactoryResolver:ComponentFactoryResolver,
  ) { 
    window.redback.navigateTo = (event: NavigateEvent) => this.navigateTo(event);
    for(let key of Object.keys(componentRegistry)) {
      let componentClass = componentRegistry[key];
      let factory = this.componentFactoryResolver.resolveComponentFactory(componentClass);
      this.factoryRegistry[key] = factory;
    }
  }

  registerTarget(name: string, comp: RbViewLoaderComponent) {
    this.targets[name ?? "default"] = new Target(comp);
  }

  deregisterTarget(name: string){
    delete this.targets[name ?? "default"];
  }

  getNavigateObservable() : Observable<NavigateEvent>  {
    return new Observable<NavigateEvent>((observer) => {
      this.navigateObservers.push(observer);
    });
  }

  async navigateTo(event: NavigateEvent) {
    let target = this.getTarget(event.target);
    if(target != null) {
      let view: string | undefined = event.view;
      if(view == null && event.objectname != null) {
        let objectConfig: any = this.configService.getObjectConfig(event.objectname);
        if(objectConfig != null) {
          view = objectConfig.view
        }
      }
      if(view != null ) {
        if(event.reset == false) {
          if(target.component.currentLoadedView != null) {
            let backData = new NavigateBackData(target.component.currentLoadedView.name, target.component.currentLoadedView.getCurrentlyOpenTab(), this.modalService.currentlyOpenModal);
            backData.breadcrumbLabel = target.component.currentLoadedView.title;
            backData.dataTargets = target.component.currentLoadedView.extractNavigateEventDataTargets();
            target.stack.push(backData)
          }
        } else {
          target.stack = [];
        }
        await this.executeViewChange(target.component, view, event.datatargets, event.comptargets, event.tab, event.modal);
        if(event.label != null) target.title = event.label;
        this.notifyObservers(event);
      } else if(event.tab != null) {
        target.component.currentLoadedView?.openTab(event.tab);
      } else if(event.modal != null) {
        setTimeout(() => this.modalService.open(event.modal!), 100);
      }
    }
  }

  async backTo(index: number, targetname?: string) {
    let target = this.getTarget(targetname);
    if(target != null) {
      let backData = target.stack[index];
      target.stack.splice(index);
      await this.executeViewChange(target.component, backData.view, backData.dataTargets, undefined, backData.tab, backData.modal);
    }
  }

  private async executeViewChange(viewLoader: RbViewLoaderComponent, viewName: string, datatargets?: NavigateEventDataTarget[], comptargets?: NavigateEventCompTarget[], tab?: string, modal?: string) : Promise<LoadedView> {
    try {
      this.modalService.closeAll();
      viewLoader.currentLoadedView?.deactivate();
      viewLoader.detachCurrentLoadedView();
      let entry: LoadedView = await this.getLoadedView(viewName);
      window.redback.currentLoadedView = entry;
      this.userprefService.setCurrentView(viewName); //Some comp inits will use this service, so it needs to be update first
      this.modalService.setCurrentView(viewName);
      viewLoader.attachNewLoadedView(entry);
      await sleep(1); //This is to make sure the components are fully attached and initiated
      if(datatargets != null) entry.filterSortDataSets(datatargets);
      if(comptargets != null) entry.configureComponents(comptargets);
      entry.activate();
      if(tab != null) entry.openTab(tab);
      if(modal != null) this.modalService.open(modal);
      return entry;
    } catch (err) {
      this.logService.error("NavService: Error executing view change :" + err);
      throw err;
    }
  }

  private async getLoadedView(viewName: string) : Promise<LoadedView> {
    let viewConfig = await this.loadView(viewName);
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
    return entry;
  }

  private async loadView(viewName: string): Promise<any> {
    return new Promise((resolve, reject) => {
      this.apiService.getView(viewName).subscribe({
        next: (resp) => {
          resolve(resp);
        },
        error: (err) => reject(err)
      });
    })
  }

  getCurrentNavigateBackStack(targetname?: string): NavigateBackData[] {
    return this.getTarget(targetname)?.stack ?? [];
  }

  getCurrentTitle(targetname?: string): string {
    let target = this.getTarget(targetname);
    return target.title ?? target.component.currentLoadedView?.title ?? "";
  }

  getCurrentTopDataSets(targetname?: string) : RbDatasetComponent[] {
    return this.getTarget(targetname).component.currentLoadedView?.getTopActiveDatasets() ?? [];
  }

  private getTarget(name?: string) : Target {
    return this.targets[name ?? "default"];
  }

  private notifyObservers(navdata: NavigateEvent) {
    for(const observer of this.navigateObservers) {
      observer.next(navdata);
    }
  }
}
