import { Injectable } from '@angular/core';
import { NavigateEvent, NavigateData, DataTarget } from 'app/datamodel';
import { RbViewLoaderComponent } from 'app/rb-view-loader/rb-view-loader.component';
import { ConfigService } from './config.service';
import { UserprefService } from './userpref.service';
import { LoadedView } from 'app/rb-view-loader/rb-view-loader-model';
import { Observable, Observer } from 'rxjs';
import { ModalService } from './modal.service';
import { LogService } from './log.service';

@Injectable({
  providedIn: 'root'
})
export class NavigateService {
  targetViewLoaders: {[key: string]: {component: RbViewLoaderComponent, stack: NavigateData[]}} = {};
  navigateObservers: Observer<NavigateData>[] = [];

  constructor(
    private configService: ConfigService,
    private userprefService: UserprefService,
    private modalService: ModalService,
    private logService: LogService
  ) { 
    window.redback.navigateTo = (event) => this.navigateTo(event);
  }

  registerTarget(name: string, comp: RbViewLoaderComponent) {
    let key = name ?? "default";
    this.targetViewLoaders[key] = {component: comp, stack:[]};
  }

  deregisterTarget(name: string){
    let key = name ?? "default";
    delete this.targetViewLoaders[key];
  }

  getNavigateObservable() : Observable<NavigateData>  {
    return new Observable<NavigateData>((observer) => {
      this.navigateObservers.push(observer);
    });
  }

  async navigateTo(event: NavigateEvent) {
    let targetViewLoader = this.targetViewLoaders[event.target ?? "default"];  
    let objectConfig: any = this.configService.getObjectConfig(event.objectname);
    let view: string = (event.view != null ? event.view : (objectConfig != null ? objectConfig.view : null));
    if(view != null) {
      let datatarget = new DataTarget(event.objectname, event.filter, event.search, event.select);
      let navdata = new NavigateData(event.domain, view, event.tab, datatarget); 
      if(objectConfig != null && event.filter != null && event.filter[objectConfig.labelattribute] != null) {
        navdata.breadcrumbLabel = eval(event.filter[objectConfig.labelattribute]);
      }
      if(event.label != null) {
        navdata.additionalTitle = event.label;
      }
      if(targetViewLoader != null) {
        if(event.reset) {
          targetViewLoader.stack = [];
        }
        targetViewLoader.stack.push(navdata);
        await this.executeViewNavigation(targetViewLoader, navdata);
      }
    } else if(event.tab != null) {
      targetViewLoader.component.currentLoadedView.openTab(event.tab);
    }
  }

  async backTo(target: string, index: number) {
    let targetViewLoader = this.targetViewLoaders[target];
    if(targetViewLoader != null) {
      targetViewLoader.stack.splice(index + 1);
      let navdata = targetViewLoader.stack[index];
      await this.executeViewNavigation(targetViewLoader, navdata);
    }
  }

  async executeViewNavigation(targetViewLoader, navdata) {
    this.modalService.closeAll();
    try {
      await targetViewLoader.component.navigateTo(navdata);
      this.userprefService.setCurrentView(navdata.view);
      this.modalService.setCurrentView(navdata.view);
      this.notifyObservers(navdata);
      this.logService.info("View is :" + navdata.view);
    } catch (err) {
      this.logService.error("Error navigating :" + err);
    }
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
    let targetObject = this.targetViewLoaders[target ?? "default"]; 
    return targetObject != null ? targetObject.component.currentLoadedView : null; 
  }

  notifyObservers(navdata: NavigateData) {
    for(const observer of this.navigateObservers) {
      observer.next(navdata);
    }
  }
}
