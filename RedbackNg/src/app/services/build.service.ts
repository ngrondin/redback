import { Component, ComponentFactoryResolver, ComponentRef, Injectable, Injector, ViewContainerRef } from '@angular/core';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbSetComponent } from 'app/abstract/rb-set';

import { RbDynamicGraphComponent } from "app/graphs/rb-dynamicgraph/rb-dynamicgraph.component";
import { RbNumberTilesComponent } from "app/graphs/rb-number-tiles/rb-number-tiles.component";
import { RbAddressInputComponent } from "app/inputs/rb-address-input/rb-address-input.component";
import { RbChoiceInputComponent } from "app/inputs/rb-choice-input/rb-choice-input.component";
import { RbCodeInputComponent } from "app/inputs/rb-code-input/rb-code-input.component";
import { RbCurrencyInputComponent } from "app/inputs/rb-currency-input/rb-currency-input.component";
import { RbDatetimeInputComponent } from "app/inputs/rb-datetime-input/rb-datetime-input.component";
import { RbDurationInputComponent } from "app/inputs/rb-duration-input/rb-duration-input.component";
import { RbFileInputComponent } from "app/inputs/rb-file-input/rb-file-input.component";
import { RbNumberInputComponent } from "app/inputs/rb-number-input/rb-number-input.component";
import { RbPercentInputComponent } from "app/inputs/rb-percent-input/rb-percent-input.component";
import { RbRelatedInputComponent } from "app/inputs/rb-related-input/rb-related-input.component";
import { RbStarsInputComponent } from "app/inputs/rb-stars-input/rb-stars-input.component";
import { RbStringInputComponent } from "app/inputs/rb-string-input/rb-string-input.component";
import { RbSwitchInputComponent } from "app/inputs/rb-switch-input/rb-switch-input.component";
import { RbTextareaInputComponent } from "app/inputs/rb-textarea-input/rb-textarea-input.component";
import { RbActiongroupComponent } from "app/rb-actiongroup/rb-actiongroup.component";
import { RbAggregatesetComponent } from "app/rb-aggregateset/rb-aggregateset.component";
import { RbButtonComponent } from "app/rb-button/rb-button.component";
import { RbCalendarComponent } from "app/rb-calendar/rb-calendar.component";
import { RbDatasetComponent } from "app/rb-dataset/rb-dataset.component";
import { RbDatasetGroupComponent } from "app/rb-datasetgroup/rb-datasetgroup.component";
import { RbDynamicformComponent } from "app/rb-dynamicform/rb-dynamicform.component";
import { RbFiledropComponent } from "app/rb-filedrop/rb-filedrop.component";
import { RbFilelistComponent } from "app/rb-filelist/rb-filelist.component";
import { RbFilesetComponent } from "app/rb-fileset/rb-fileset.component";
import { RbFormComponent } from "app/rb-form/rb-form.component";
import { RbGanttComponent } from "app/rb-gantt/rb-gantt.component";
import { RbHsectionComponent } from "app/rb-hsection/rb-hsection.component";
import { RbHseparatorComponent } from "app/rb-hseparator/rb-hseparator.component";
import { RbLayoutComponent } from "app/rb-layout/rb-layout.component";
import { RbLinkComponent } from "app/rb-link/rb-link.component";
import { RbListComponent } from "app/rb-list/rb-list.component";
import { RbList4Component } from "app/rb-list4/rb-list4.component";
import { RbLogComponent } from "app/rb-log/rb-log.component";
import { RbMapComponent } from "app/rb-map/rb-map.component";
import { RbModalComponent } from "app/rb-modal/rb-modal.component";
import { RbProcessactionsComponent } from "app/rb-processactions/rb-processactions.component";
import { RbSearchComponent } from "app/rb-search/rb-search.component";
import { RbSpacerComponent } from "app/rb-spacer/rb-spacer.component";
import { RbTabSectionComponent } from "app/rb-tab-section/rb-tab-section.component";
import { RbTabComponent } from "app/rb-tab/rb-tab.component";
import { RbTableComponent } from "app/rb-table/rb-table.component";
import { RbTileComponent } from "app/rb-tile/rb-tile.component";
import { RbTimelineComponent } from "app/rb-timeline/rb-timeline.component";
import { RbVcollapseComponent } from "app/rb-vcollapse/rb-vcollapse.component";
import { componentRegistry } from 'app/rb-view-loader/rb-view-loader-registry';
import { LoadedView } from 'app/rb-view-loader/rb-view-loader.component';
import { RbVsectionComponent } from "app/rb-vsection/rb-vsection.component";
/*
export const componentRegistry = {
    "dataset": RbDatasetComponent,
    "datasetgroup": RbDatasetGroupComponent,
    "layout": RbLayoutComponent,
    "form": RbFormComponent,
    "hsection": RbHsectionComponent,
    "vsection": RbVsectionComponent,
    "tabsection": RbTabSectionComponent,
    "tab": RbTabComponent,
    "input": RbStringInputComponent,
    "numberinput":RbNumberInputComponent,
    "relatedinput": RbRelatedInputComponent,
    "list3": RbListComponent,
    "list4": RbList4Component,
    "button": RbButtonComponent,
    "search": RbSearchComponent,
    "actiongroup": RbActiongroupComponent,
    "addressinput": RbAddressInputComponent,
    "aggregateset": RbAggregatesetComponent,
    "choiceinput": RbChoiceInputComponent,
    "codeinput": RbCodeInputComponent,
    "currencyinput": RbCurrencyInputComponent,
    "durationinput": RbDurationInputComponent,
    "dynamicform": RbDynamicformComponent,
    "fileinput": RbFileInputComponent,
    "filedrop": RbFiledropComponent,
    "filelist": RbFilelistComponent,
    "fileset": RbFilesetComponent,
    "textarea": RbTextareaInputComponent,
    "datepicker": RbDatetimeInputComponent,
    "dateinput": RbTextareaInputComponent,
    "gantt": RbGanttComponent,
    "link": RbLinkComponent,
    "log": RbLogComponent,
    "map": RbMapComponent,
    "modal": RbModalComponent,
    "processactionsbutton": RbProcessactionsComponent,
    "switch": RbSwitchInputComponent,
    "table": RbTableComponent,
    "vcollapse": RbVcollapseComponent,
    "hseparator": RbHseparatorComponent,
    "spacer": RbSpacerComponent,
    "graph": RbDynamicGraphComponent,
    "calendar": RbCalendarComponent,
    "tile": RbTileComponent,
    "numbertiles": RbNumberTilesComponent,
    "timeline": RbTimelineComponent,
    "stars": RbStarsInputComponent,
    "percentinput": RbPercentInputComponent
  }*/

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
