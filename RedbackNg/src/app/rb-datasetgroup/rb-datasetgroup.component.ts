import { Component, Input, OnInit } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { Observable, Subscription } from 'rxjs';
import { Observer } from 'rxjs';

@Component({
  selector: 'rb-datasetgroup',
  templateUrl: './rb-datasetgroup.component.html',
  styleUrls: ['./rb-datasetgroup.component.css']
})
export class RbDatasetGroupComponent extends RbContainerComponent {
  datasets: any = {};
  _selectedObject: RbObject;
  private observers: Observer<string>[] = [];
  
  constructor() {
    super();
  }
  
  containerInit() {
  }

  containerDestroy() {
  }

  onDatasetEvent(event: any) {
  }

  onActivationEvent(state: any) {
  }

  public get lists() : any {
    let l = {};
    for(let key of Object.keys(this.datasets)) {
      l[key] = this.datasets[key].list;
    }
    return l;
  }

  public get selectedObject(): RbObject {
    return this._selectedObject;
  }

  public set selectedObject(obj: RbObject) {
    this.select(obj);
  }
  
  getObservable() : Observable<string>  {
    return new Observable<string>((observer) => {
      this.observers.push(observer);
    });
  }

  public register(name: string, dataset: RbDatasetComponent) {
    this.datasets[name] = dataset;
  }

  setActive(state: boolean) {
    this.active = state;
  }

  public select(obj: RbObject) {
    this._selectedObject = obj;
    for(let name of Object.keys(this.datasets)) {
      let ds: RbDatasetComponent = this.datasets[name];
      if(ds.object == obj.objectname) {
        ds.select(obj);
      } else {
        ds.select(null);
      }
    }
    this.publishEvent('groupselect');
  }

  public groupMemberEvent(name: string, event: string) {
    this.publishEvent('group_' + name + "_" + event);
  }

  public publishEvent(event: string) {
    this.observers.forEach((observer) => {
      observer.next(event);
    });     
  }

  public get isLoading(): boolean {
    let l: boolean = false;
    for(let key of Object.keys(this.datasets)) {
      if(this.datasets[key].isLoading) {
        l = true;
      }
    }
    return l;
  }
}
