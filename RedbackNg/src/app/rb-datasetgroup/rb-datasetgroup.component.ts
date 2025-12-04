import { Component, Input, OnInit } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { Observable, Subscription } from 'rxjs';
import { Observer } from 'rxjs';
import { RbSearchTarget } from 'app/rb-search/rb-search-target';
import { FilterService } from 'app/services/filter.service';

export type DatasetMap = {
  [key: string]: RbDatasetComponent;
};

export type DatasetListMap = {
  [key: string]: RbObject[];
};

@Component({
  selector: 'rb-datasetgroup',
  templateUrl: './rb-datasetgroup.component.html',
  styleUrls: ['./rb-datasetgroup.component.css']
})
export class RbDatasetGroupComponent extends RbContainerComponent implements RbSearchTarget {
  datasets: DatasetMap = {};
  _selectedObject: RbObject;
  private observers: Observer<any>[] = [];
  
  constructor(
    public filterService: FilterService
  ) {
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

  public get lists() : DatasetListMap {
    let l: DatasetListMap = {};
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
  

  public get isLoading(): boolean {
    let l: boolean = false;
    for(let key of Object.keys(this.datasets)) {
      if(this.datasets[key].isLoading) {
        l = true;
      }
    }
    return l;
  }

  getObservable() : Observable<string>  {
    return new Observable<string>((observer) => {
      this.observers.push(observer);
    });
  }

  public register(id: string, dataset: RbDatasetComponent) {
    this.datasets[id] = dataset;
  }

  public refreshAllData() {
    for(let id of Object.keys(this.datasets)) {
      let ds: RbDatasetComponent = this.datasets[id];
      ds.refreshData();
    }
  }

  public select(obj: RbObject) {
    this._selectedObject = obj;
    for(let id of Object.keys(this.datasets)) {
      let ds: RbDatasetComponent = this.datasets[id];
      if(ds.objectname == obj.objectname && ds.list.indexOf(obj) > -1) {
        ds.select(obj);
      }
    }
  }

  public filterSort(event: any) : boolean {
    let fetched = true;
    for(let id of Object.keys(this.datasets)) {
      let ds: RbDatasetComponent = this.datasets[id];
      fetched = fetched && ds.filterSort(event);
    }
    return fetched;
  }

  public getBaseSearchFilter(): any {
    return null;
  }

  public groupMemberEvent(event: any) {
    event.datasetgroup = this;
    this.forwardEvent(event);
  }

  public forwardEvent(event: any) {
    this.observers.forEach((observer) => {
      observer.next(event);
    });     
  }

  public getFirstDataset() : RbDatasetComponent {
    let ids = Object.keys(this.datasets);
    if(ids.length > 0) {
      return this.datasets[ids[0]];
    } else {
      return null;
    }
  }

  //Getters used mainly for SearchTarget
  public getId(): string {
    throw this.id;
  }

  public getSearchTargetType(): string {
    return "datasetgroup";
  }

  public getObjectName(): string {
    return null;
  }

  public getBaseFilter(): any {
    return null;
  }

  public getUserFilter() {
    let filter = {};
    for(var dsId of Object.keys(this.datasets)) {
      filter = this.filterService.mergeFilters(filter, this.datasets[dsId].userFilter);
    }
    return filter;
  }

  public getUserSort() {
    return null;
  }
}
