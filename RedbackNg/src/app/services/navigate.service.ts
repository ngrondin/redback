import { Injectable } from '@angular/core';
import { NavigateEvent, NavigateData } from 'app/datamodel';
import { RbViewLoaderComponent } from 'app/rb-view-loader/rb-view-loader.component';
import { ConfigService } from './config.service';
import { UserprefService } from './userpref.service';

@Injectable({
  providedIn: 'root'
})
export class NavigateService {
  targets: {[key: string]: {component: RbViewLoaderComponent, stack: NavigateData[]}} = {};

  constructor(
    private configService: ConfigService,
    private userprefService: UserprefService
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

  async navigateTo(event: NavigateEvent) {
    let target = this.targets[event.target ?? "default"];  
    let objectConfig: any = this.configService.objectsConfig[event.object];
    let view: string = (event.view != null ? event.view : (objectConfig != null ? objectConfig.view : null));
    if(view != null) {
      let data = new NavigateData(event.domain, view, event.tab, event.object, event.filter, event.search); 
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
        await target.component.navigateTo(data);
        this.userprefService.setCurrentView(data.view);
      }
    } else if(event.tab != null) {
      target.component.openTab(event.tab);
    }
  }

  backTo(target: string, index: number) {
    let targetObject = this.targets[target];
    if(targetObject != null) {
      targetObject.stack.splice(index + 1);
      let data = targetObject.stack[index];
      targetObject.component.navigateTo(data);
      this.userprefService.setCurrentView(data.view);
    }
  }

  getCurrentNavigateData(target: string): NavigateData {
    let targetObject = this.targets[target];
    if(targetObject != null) {
      if(targetObject.stack.length > 0) {
        return targetObject.stack[targetObject.stack.length - 1];
      }
    }
    return null;
  }

  getCurrentNavigateStack(target: string): NavigateData[] {
    let targetObject = this.targets[target];
    if(targetObject != null) {
      return targetObject.stack;
    }
    return [];
  }
}
