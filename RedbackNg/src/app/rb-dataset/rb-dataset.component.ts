import { Component, Input } from '@angular/core';
import { DataTarget, RbObject } from '../datamodel';
import { DataService } from '../services/data.service';
import { FilterService } from 'app/services/filter.service';
import { Observable, Subscription } from 'rxjs';
import { ApiService } from 'app/services/api.service';
import { Observer } from 'rxjs';
import { ValueComparator } from 'app/helpers';
import { RbSetComponent } from 'app/abstract/rb-set';
import { RbSearchTarget } from 'app/rb-search/rb-search-target';


@Component({
  selector: 'rb-dataset',
  templateUrl: './rb-dataset.component.html',
  styleUrls: ['./rb-dataset.component.css']
})
export class RbDatasetComponent extends RbSetComponent implements RbSearchTarget  {
  @Input('basesort') baseSort: any;
  @Input('name') name: string; //To be deprecated
  @Input('fetchall') fetchAll: boolean = false;
  @Input('addrelated') addrelated: boolean = true;
  @Input('addtoend') addtoend: boolean = false;


  public uid: string;
  private dataSubscription: Subscription;
  private _list: RbObject[] = [];
  private _selectedObject: RbObject;
  public totalCount: number = -1;
  public searchString: string;
  public userFilter: any = null;
  public userSort: any = null;
  public mergedFilter: any;
  public resolvedFilter: any;
  public nextPage: number;
  public pageSize: number;
  public hasMorePages: boolean = true;
  public _loading: boolean = false;
  private observers: Observer<string>[] = [];
  public refreshOnActivate: boolean = true;


  constructor(
    private dataService: DataService,
    private apiService: ApiService,
    private filterService: FilterService,
  ) {
    super();
    this.uid = "" + Math.floor(Math.random() * 10000);
  }

  setInit() {
    this.dataSubscription = this.dataService.getCreationObservable().subscribe(object => this.receiveNewlyCreatedData(object));
    this.pageSize = this.fetchAll == true ? 250 : 50;
    this._loading = false;
    this.hasMorePages = true;
    if(this.datasetgroup != null) {
      this.datasetgroup.register((this.id || this.name), this);
    }
    this.refreshData();
  }

  setDestroy() {
    this.clear();
    this.dataSubscription.unsubscribe();
  }

  onDatasetEvent(event: string) {
    if(event == 'select') {
      this.refreshData();
    } else if(event == 'load') {

    } else if(event == 'clear') {
      this.clear();
    } else if(event == 'update') {
      this.publishEvent('update');
      this.refreshData(true);
    }
  }

  onActivationEvent(state: any) {
    if(state == true && this.refreshOnActivate) {
      this.refreshData();
    }
  }

  onDataTargetEvent(dt: DataTarget) {
    if(this.dataTarget != null 
      && (this.dataTarget.filter != null || this.dataTarget.search != null)
      && (ValueComparator.notEqual(this.dataTarget.filter, this.userFilter) || (this.dataTarget.search != this.searchString))) {
        this.searchString = this.dataTarget.search || null;
        this.userFilter = this.dataTarget.filter || null;
        this.refreshData();
    }
  }

  public get list() : RbObject[] {
    return this._list;
  }

  public get relatedObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : null;
  }

  public get selectedObject(): RbObject {
    return this._selectedObject;
  }

  public set selectedObject(obj: RbObject) {
    this.select(obj);
  }

  public get isLoading() : boolean {
    return this._loading;
  }

  public get canLoadData() : boolean {
    return this.active 
      && (this.master == null || (this.master != null && this.relatedObject != null))
      && (this.requiresuserfilter == false || this.userFilter != null)
      && !this._loading;
  }

  getObservable() : Observable<string>  {
    return new Observable<string>((observer) => {
      this.observers.push(observer);
    });
  }

  public refreshData(onlyIfFilterChanged = false) : boolean {
    if(this.canLoadData) {
      let filterChanged = this.calcFilter();
      if(onlyIfFilterChanged == false || (onlyIfFilterChanged == true && filterChanged == true)) {
        this.clear();
        this.fetchNextPage();
        this.refreshOnActivate = false;
        return true;  
      } else {
        return false;
      }
    } else {
      this.refreshOnActivate = true;
      return false;
    }
  }

  public clear() {
    this.nextPage = 0;
    this.hasMorePages = true;
    this.totalCount = -1;
    for(let obj of this._list) {
      obj.removeSet(this);
    }
    this._list = [];
    this._selectedObject = null;
    this.publishEvent('clear');
  }

  public fetchNextPage() {
    if(this.hasMorePages) {
      const sort = this.userSort != null ? this.userSort : this.dataTarget != null && this.dataTarget.sort != null ? this.dataTarget.sort : this.baseSort;
      const search = this.searchString;
      const addRel = this.fetchAll ? false : this.addrelated;
      let observable = null;
      if(this.fetchAll) {
        observable = this.dataService.fetchEntireList(this.objectname, this.resolvedFilter, search, sort);
      } else {
        observable = this.dataService.fetchList(this.objectname, this.resolvedFilter, search, sort, this.nextPage, this.pageSize, addRel);
        this.nextPage++;
      }
      observable.subscribe({
        next: (data) => {
          this.setData(data);
        },
        error: (error) => {
          this.loadComplete(false);
        },
        complete: () => {
          this.loadComplete(true);
        }
      });
      this._loading = true;
    }
  }

  private calcFilter() : boolean {
    let prevFilterStr = JSON.stringify(this.resolvedFilter);
    let filter = {};
    if(this.userFilter != null && this.userFilter["uid"] != null) {
      filter = this.userFilter;
    } else {
      if(this.baseFilter != null) {
        filter = this.baseFilter;
      }
      if(this.master != null && this.relatedObject != null) {
        filter = this.filterService.mergeFilters(filter, this.master.relationship);
      } 
      if(this.userFilter != null) {
        filter = this.filterService.mergeFilters(filter, this.userFilter);
      }  
    }
    this.mergedFilter = filter;
    this.resolvedFilter = this.filterService.resolveFilter(filter, this.relatedObject, this.selectedObject, this.relatedObject);
    this.dataService.subscribeToCreation(this.uid, this.objectname, this.resolvedFilter);
    return (JSON.stringify(this.resolvedFilter)) != prevFilterStr; //Has the filter changed?
  }

  private setData(data: RbObject[]) {
    for(var i = 0; i < data.length; i++) {
      let obj: RbObject = data[i];
      if(this._list.indexOf(obj) == -1) {
        this._list.push(obj);
        obj.addSet(this);
      }
    } 
    if(this.fetchAll == false && data.length < this.pageSize) {
      this.hasMorePages = false
    }
  }

  private loadComplete(success) {
    this._loading = false;
    if(this.fetchAll) {
      this.hasMorePages = false;
    }
    this.publishEvent('load');
    //this.firstLoad = false;
    if(this._list.length == 0) {
      this._selectedObject = null;
    } else if(this._list.length == 1) {
      this.select(this._list[0]);
    } else if(this._list.length > 1) {
      if(this.selectedObject != null && !this._list.includes(this.selectedObject)) {
        this._selectedObject = null;
      }
    }
    if(this.nextPage == 1) { 
      if(this.fetchAll == false && this._list.length > 10) {
        this.apiService.countObjects(this.objectname, this.resolvedFilter, this.searchString).subscribe(data => {this.totalCount = data.result});
      } else {
        this.totalCount = this._list.length;
      }
    }
  }
  
  private receiveNewlyCreatedData(object: RbObject) {
    if(object.objectname == this.objectname && this.isLoading == false && this._list.includes(object) == false && (this.searchString == null || this.searchString == '') && (this.fetchAll == true || this._list.length < this.pageSize)) {
      if(this.filterService.applies(this.resolvedFilter, object)) {
        this._list.push(object);
        object.addSet(this);
        this.publishEvent('load');
        if(this._list.length == 1) {
          this._selectedObject = this._list[0];
        }  
      }
    }
  }

  public objectUpdated(object: RbObject) {
    this.publishEvent('update');
  }

  public select(item: RbObject) {
    this._selectedObject = item;
    if(this.dataTarget != null) {
      this.dataTarget.selectedObject = item;
    }
    this.publishEvent('select');
  }

  public filterSort(event: any) : boolean {
    let fetched = false;
    if(('filter' in event && ValueComparator.notEqual(event.filter, this.userFilter))
     || ('sort' in event && ValueComparator.notEqual(event.sort, this.userSort))
     || ('search' in event && event.search != this.searchString)) {
      if('filter' in event) this.userFilter = event.filter;
      if('sort' in event) this.userSort = event.sort;
      if('search' in event) this.searchString = event.search;
      fetched = this.refreshData();
      if(this.dataTarget != null && this.ignoretarget == false) {
        this.dataTarget.filter = event.filter;
      }
    }
    return fetched;
  } 

  public getBaseSearchFilter(): any {
    return this.baseFilter;
  }

  public create() {
    this.dataService.create(this.objectname, null, this.resolvedFilter).subscribe(newObject => this.addObjectAndSelect(newObject));
  }

  public delete(obj: RbObject) {
    this.dataService.delete(obj).subscribe(result => this.remove(obj));
  }

  public addObjectAndSelect(obj: RbObject) {
    if(this._list.indexOf(obj) > -1) {
      this._list.splice(this._list.indexOf(obj));
    }
    if(this.addtoend == true) {
      this._list.push(obj);
    } else {
      this._list.unshift(obj);
    }
    obj.addSet(this);
    this.publishEvent("load");
    this.select(obj);
  }

  public removeSelected() {
    this.remove(this.selectedObject);
    this.selectedObject = null;
  }

  public remove(obj: RbObject) {
    if(this._list.indexOf(obj) > -1) {
      this._list.splice(this._list.indexOf(obj), 1);
      obj.removeSet(this);
      this.publishEvent("removed");
    }
  }

  private publishEvent(event: string) {
    this.observers.forEach((observer) => {
      observer.next(event);
    }); 
    if(this.datasetgroup != null) {
      this.datasetgroup.groupMemberEvent((this.id || this.name), event);
    }
  }
}
