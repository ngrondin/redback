import { Component, ComponentFactoryResolver, ComponentRef, Injectable, Injector, ViewContainerRef } from '@angular/core';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbSetComponent } from 'app/abstract/rb-set';
import { RbAggregatesetComponent } from "app/rb-aggregateset/rb-aggregateset.component";
import { RbDatasetComponent } from "app/rb-dataset/rb-dataset.component";
import { RbDatasetGroupComponent } from "app/rb-datasetgroup/rb-datasetgroup.component";
import { RbFilesetComponent } from "app/rb-fileset/rb-fileset.component";
import { RbModalComponent } from "app/rb-modal/rb-modal.component";
import { RbTabSectionComponent } from "app/rb-tab-section/rb-tab-section.component";
import { RbTabComponent } from "app/rb-tab/rb-tab.component";
import { componentRegistry } from 'app/rb-view-loader/rb-view-loader-registry';
import { LoadedView } from 'app/rb-view-loader/rb-view-loader.component';


@Injectable({
  providedIn: 'root'
})
export class BuildService {
  private factoryRegistry: any = {};
  
  constructor(
    private componentFactoryResolver:ComponentFactoryResolver,
    private injector: Injector    
  ) { 
    for(let key of Object.keys(componentRegistry)) {
      let componentClass = componentRegistry[key];
      let factory = this.componentFactoryResolver.resolveComponentFactory(componentClass);
      this.factoryRegistry[key] = factory;
    }
  }

  buildConfigRecursive(parent: ViewContainerRef, config: any, context: any, loadedView?: LoadedView) {
    let newComponentRef: ComponentRef<Component> = null;
    let factory = this.factoryRegistry[config.type]; 
    if(factory != null) {
      if(parent != null) {
        newComponentRef = parent.createComponent(factory);
      } else {
        newComponentRef = factory.create(this.injector); 
      }
      let newInstance = newComponentRef.instance;
      var inputs = factory['componentDef']['declaredInputs'];
      var outputs: any = factory['componentDef']['outputs'];
      for(var input of Object.keys(inputs)) {
        let val: any = null;
        if(input == 'dataset' && context['dataset'] != null && context['dataset'] instanceof RbDatasetComponent) {
          val = context['dataset'];
        } else if(input == 'datasetgroup' && context['datasetgroup'] != null && context['datasetgroup'] instanceof RbDatasetGroupComponent) {
          val = context['datasetgroup'];
        } else if(input == 'fileset' && context['fileset'] != null && context['fileset'] instanceof RbFilesetComponent) {
          val = context['fileset'];
        } else if(input == 'aggregateset' && context['aggregateset'] != null && context['aggregateset'] instanceof RbAggregatesetComponent) {
          val = context['aggregateset'];
        } else if(input == 'activator' && context['activator'] != null && context['activator'] instanceof RbActivatorComponent) {
          val = context['activator'];
        } else if(input == 'tabsection' && context['tabsection'] != null && context['tabsection'] instanceof RbTabSectionComponent) {
          val = context['tabsection'];
        } else if(config[input] != null) {
          val = config[input];
        }
        if(val != null) {
          newInstance[inputs[input]] = val;
        }
      };
      if(loadedView != null) {
        if(newInstance instanceof RbSetComponent && newInstance['master'] == null) {
          loadedView.topSets.push(newInstance);
        }
        if(newInstance instanceof RbTabSectionComponent) {
          loadedView.topTabs.push(newInstance);
        }        
        if(outputs['navigate'] != null && newInstance['navigate'] != null && newInstance['navigate'].subscribe != null) {
          newInstance['navigate'].subscribe(e => loadedView.navigate.emit(e))
        }
      }
      if(config['content'] != null) {
        if(newInstance instanceof RbContainerComponent && newInstance.container != null) {
          let newContext = Object.assign({}, context);
          if(newInstance instanceof RbDatasetComponent) {
            newContext['dataset'] = newInstance;
          } else if(newInstance instanceof RbDatasetGroupComponent) {
            newContext['datasetgroup'] = newInstance;
          } else if(newInstance instanceof RbFilesetComponent) {
            newContext['fileset'] = newInstance;
          } else if(newInstance instanceof RbAggregatesetComponent) {
            newContext['aggregateset'] = newInstance;
          } else if(newInstance instanceof RbTabComponent) {
            newContext['activator'] = newInstance;
          } else if(newInstance instanceof RbModalComponent) {
            newContext['activator'] = newInstance;
          } else if(newInstance instanceof RbTabSectionComponent) {
            newContext['tabsection'] = newInstance;
          }
          let newContainer: RbContainerComponent = <RbContainerComponent>newInstance;
          for(let childConfig of config['content']) {
            this.buildConfigRecursive(newContainer.container, childConfig, newContext, loadedView);
          };
        } else {
          console.log('Type ' + config.type + ' has contents but is not a RbContainerComponent');
        }
      }
    }
    return newComponentRef;
  }
}
