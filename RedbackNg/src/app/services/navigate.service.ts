import { Injectable } from '@angular/core';
import { NavigateEvent, NavigateData } from 'app/datamodel';
import { RbViewLoaderComponent } from 'app/rb-view-loader/rb-view-loader.component';
import { ConfigService } from './config.service';
import { UserprefService } from './userpref.service';
import { LoadedView } from 'app/rb-view-loader/rb-view-loader-model';
import { Observable, Observer } from 'rxjs';
import { ModalService } from './modal.service';

@Injectable({
  providedIn: 'root'
})
export class NavigateService {
  targets: {[key: string]: {component: RbViewLoaderComponent, stack: NavigateData[]}} = {};
  navigateObservers: Observer<NavigateData>[] = [];

  constructor(
    private configService: ConfigService,
    private userprefService: UserprefService,
    private modalService: ModalService
  ) { 
    window.redback.navigateTo = (event) => this.navigateTo(event);
  }

  registerTarget(name: string, comp: RbViewLoaderComponent) {
    let key = name ?? "default";
    this.targets[key] = {component: comp, stack:[]};
  }

  deregisterTarget(name: string){
    let key = name ?? "default";
    delete this.targets[key];
  }

  getNavigateObservable() : Observable<NavigateData>  {
    return new Observable<NavigateData>((observer) => {
      this.navigateObservers.push(observer);
    });
  }

  async navigateTo(event: NavigateEvent) {
    let target = this.targets[event.target ?? "default"];  
    let objectConfig: any = this.configService.getObjectConfig(event.objectname);
    let view: string = (event.view != null ? event.view : (objectConfig != null ? objectConfig.view : null));
    if(view != null) {
      let data = new NavigateData(event.domain, view, event.tab, event.objectname, event.filter, event.search, event.objectuid); 
      if(objectConfig != null && event.filter != null && event.filter[objectConfig.labelattribute] != null) {
        data.breadcrumbLabel = eval(event.filter[objectConfig.labelattribute]);
      }
      if(event.label != null) {
        data.additionalTitle = event.label;
      }
      if(target != null) {
        if(event.reset) {
          target.stack = [];
        }
        target.stack.push(data);
        this.modalService.closeAll();
        await target.component.navigateTo(data);
        this.userprefService.setCurrentView(data.view);
        this.modalService.setCurrentView(data.view);
        this.notifyObservers(data);
      }
    } else if(event.tab != null) {
      target.component.currentLoadedView.openTab(event.tab);
    }
  }

  backTo(target: string, index: number) {
    let targetObject = this.targets[target];
    if(targetObject != null) {
      targetObject.stack.splice(index + 1);
      let data = targetObject.stack[index];
      this.modalService.closeAll();
      targetObject.component.navigateTo(data);
      this.userprefService.setCurrentView(data.view);
      this.modalService.setCurrentView(data.view);
      this.notifyObservers(data);
    }
  }

  getCurrentNavigateData(target?: string): NavigateData {
    let targetObject = this.targets[target ?? "default"];  
    if(targetObject != null) {
      if(targetObject.stack.length > 0) {
        return targetObject.stack[targetObject.stack.length - 1];
      }
    }
    return null;
  }

  getCurrentNavigateStack(target?: string): NavigateData[] {
    let targetObject = this.targets[target ?? "default"];  
    if(targetObject != null) {
      return targetObject.stack;
    }
    return [];
  }

  getCurrentLoadedView(target?: string): LoadedView {
    let targetObject = this.targets[target ?? "default"]; 
    return targetObject != null ? targetObject.component.currentLoadedView : null; 
  }

  notifyObservers(navdata: NavigateData) {
    for(const observer of this.navigateObservers) {
      observer.next(navdata);
    }
  }
}
