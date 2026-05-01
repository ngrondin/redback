import { ComponentFactoryResolver, Injectable } from '@angular/core';
import { NavigateEvent, NavigateBackData } from 'app/datamodel';
import { RbViewLoaderComponent } from 'app/rb-view-loader/rb-view-loader.component';
import { ConfigService } from './config.service';
import { UserprefService } from './userpref.service';
import { Observable, Observer } from 'rxjs';
import { ModalService } from './modal.service';
import { LogService } from './log.service';
import { ApiService } from './api.service';
import { componentRegistry, LoadedView } from 'app/loader';
import { BuildService } from './build.service';

@Injectable({
  providedIn: 'root'
})
export class NavigateService {
  viewLoaders: {[key: string]: RbViewLoaderComponent} = {};
  viewLoaderStacks: {[key: string]: NavigateBackData[]} = {};
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
    let key = name ?? "default";
    this.viewLoaders[key] = comp;
    this.viewLoaderStacks[key] = [];
  }

  deregisterTarget(name: string){
    let key = name ?? "default";
    delete this.viewLoaders[key];
    delete this.viewLoaderStacks[key];
  }

  getNavigateObservable() : Observable<NavigateEvent>  {
    return new Observable<NavigateEvent>((observer) => {
      this.navigateObservers.push(observer);
    });
  }

  async navigateTo(event: NavigateEvent) {
    let targetViewLoader = this.targetViewLoaders[event.target ?? "default"];  
    let view: string | undefined = event.view;
    if(view == null && event.objectname != null) {
      let objectConfig: any = this.configService.getObjectConfig(event.objectname);
      if(objectConfig != null) {
        view = objectConfig.view
      }
    }

    if(view != null && targetViewLoader != null) {
      this.logService.debug("NavService: Navigating to " + view);

      if(event.reset == false) {
        //Extract current situation and create a NavigateBack item


      }

      /*let navdata = new NavigateData(event.domain, view, event.tab, event.modal);
      if(event.datatargets != null) {
        for(let eventtarget of event.datatargets) {
          let datatarget = new DataTarget(eventtarget.datasetid, eventtarget.objectname || event.objectname, eventtarget.filter, eventtarget.search, eventtarget.sort, eventtarget.select);
          navdata.addDataTarget(datatarget);  
        }      
      }
      if(event.comptargets != null) {
        for(let eventtarget of event.comptargets) {
          let comptarget = new CompTarget(eventtarget.compid, eventtarget.data);
          navdata.addCompTarget(comptarget);  
        } 
      }*/
      if(objectConfig != null && navdata.dataTargets.length == 1 && navdata.dataTargets[0].filter != null && navdata.dataTargets[0].filter[objectConfig.labelattribute] != null) {
        navdata.breadcrumbLabel = eval(navdata.dataTargets[0].filter[objectConfig.labelattribute]);
      }
      if(event.label != null) {
        navdata.additionalTitle = event.label;
      }
      if(targetViewLoader != null) {
        if(event.reset) {
          targetViewLoader.stack = [];
        }
        targetViewLoader.stack.push(navdata);
        await this.executeViewChange(targetViewLoader, navdata);
      }
    } else if(event.tab != null) {
      targetViewLoader.component.currentLoadedView.openTab(event.tab);
    } else if(event.modal != null) {
      setTimeout(() => this.modalService.open(event.modal), 100);
    }
  }

  async backTo(target: string, index: number) {
    let targetViewLoader = this.targetViewLoaders[target];
    if(targetViewLoader != null) {
      this.logService.debug("NavService: Navigating back to " + index);

      targetViewLoader.stack.splice(index + 1);
      let navdata = targetViewLoader.stack[index];
      await this.executeViewChange(targetViewLoader, navdata);
    }
  }

  async executeViewChange(targetViewLoader: RbViewLoaderComponent, viewName: string, modal?: string, tab?: string) {
    this.modalService.closeAll();
    try {
      targetViewLoader.detachCurrentLoadedView();
      let entry: LoadedView = await this.getView(viewName);
      targetViewLoader.attachNewLoadedView(entry);
      this.userprefService.setCurrentView(viewName);
      this.modalService.setCurrentView(viewName);
      this.notifyObservers(viewName);
      if(tab != null) {
        setTimeout(() => targetViewLoader.currentLoadedView.openTab(tab), 100);
      } else if(modal != null) {
        setTimeout(() => this.modalService.open(modal), 100);
      }
      this.logService.debug("NavService: View is " + viewName);
    } catch (err) {
      this.logService.error("NavService: Error navigating :" + err);
    }
  }

  async getView(viewName: string) : Promise<LoadedView> {
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
    if(entry != null) {
      window.redback.currentLoadedView = entry;
      //let newEmptyDataTargets = entry.setDataTargets(navData.dataTargets);
      //newEmptyDataTargets.forEach(target => navData.dataTargets.push(target));      
      //entry.setCompTargets(navData.compTargets);
    }
    return entry;
  }

  async loadView(viewName: string): Promise<any> {
    return new Promise((resolve, reject) => {
      this.apiService.getView(viewName).subscribe({
        next: (resp) => {
          resolve(resp);
        },
        error: (err) => reject(err)
      });
    })
  }

  getCurrentNavigateData(target?: string): NavigateData {
    let targetObject = this.targetViewLoaders[target ?? "default"];  
    if(targetObject != null) {
      if(targetObject.stack.length > 0) {
        return targetObject.stack[targetObject.stack.length - 1];
      }
    }
    return null;
  }

  getCurrentNavigateStack(target?: string): NavigateData[] {
    let targetObject = this.targetViewLoaders[target ?? "default"];  
    if(targetObject != null) {
      return targetObject.stack;
    }
    return [];
  }

  getCurrentLoadedView(target?: string): LoadedView {
    return this.getViewLoader(target)?.currentLoadedView;
  }

  getViewLoader(name: string | null) : RbViewLoaderComponent {
    return this.viewLoaders[name ?? "default"];
  }

  getViewLoaderStack(name: string | null) : NavigateBackData[] {
    return this.viewLoaderStacks[name ?? "default"];
  }

  notifyObservers(navdata: NavigateEvent) {
    for(const observer of this.navigateObservers) {
      observer.next(navdata);
    }
  }
}
