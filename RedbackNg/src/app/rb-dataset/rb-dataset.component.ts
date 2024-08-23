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
  private _selectedObjects: RbObject[] = [];
  public totalCount: number = -1;
  public userSearch: string;
  public userFilter: any = null;
  public userSort: any = null;
  public mergedFilter: any;
  public resolvedFilter: any;
  public resolvedSort: any;
  public resolvedSearch: string;
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
      this.datasetgroup.register((this.id || this.name || this.objectname), this);
    }
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
      this.refreshData(true);
    }
  }

  onDataTargetEvent(dt: DataTarget) {
    console.log('dateset ' + this.id + ': ' + JSON.stringify(dt)); 
    this.userSort = dt.sort;
    this.userFilter = dt.filter;
    this.userSearch = dt.search;
    let refreshed = this.refreshData();
    if(!refreshed && dt.objectuid != null) {
      console.log('dateset ' + this.id + ' not refreshed, directly selecting'); 
      this.selectUid(dt.objectuid);
    }
  }

  public get list() : RbObject[] {
    return this._list;
  }

  public get relatedObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : null;
  }

  public get selectedObject(): RbObject {
    return this._selectedObjects.length == 1 ? this._selectedObjects[0] : null;
  }

  public get selectedObjects() : RbObject[] {
    return this._selectedObjects;
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

  public contains(obj: RbObject): boolean {
    return this._list.indexOf(obj) > -1;
  }

  public isObjectSelected(object: RbObject) : boolean {
    return this._selectedObjects.indexOf(object) != -1;
  }

  getObservable() : Observable<string>  {
    return new Observable<string>((observer) => {
      this.observers.push(observer);
    });
  }

  public refreshData(onlyIfFilterChanged = false) : boolean {
    if(this.canLoadData) {
      let prevFilter = this.resolvedFilter;
      let prevSearch = this.resolvedSearch;
      this.resolveFilterSort();
      let filterChanged = ValueComparator.notEqual(prevFilter, this.resolvedFilter);
      let searchChanged = ValueComparator.notEqual(prevSearch, this.resolvedSearch);
      if(filterChanged || searchChanged || !onlyIfFilterChanged) {
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

  private resolveFilterSort() {
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
    this.resolvedSort = this.userSort != null ? this.userSort : this.baseSort;
    this.resolvedSearch = this.userSearch;
  }

  public clear() {
    this.nextPage = 0;
    this.hasMorePages = true;
    this.totalCount = -1;
    for(let obj of this._list) {
      obj.removeSet(this);
    }
    this._list = [];
    this._selectedObjects = [];
    this.publishEvent('clear');
  }

  public fetchNextPage() {
    if(this.hasMorePages) {
      const sort = this.userSort != null ? this.userSort : this.dataTarget != null && this.dataTarget.sort != null ? this.dataTarget.sort : this.baseSort;
      const search = this.userSearch != null ? this.userSearch : this.dataTarget != null && this.dataTarget.search != null ? this.dataTarget.search : null;
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

  private setData(data: RbObject[]) {
    for(var i = 0; i < data.length; i++) {
      let obj: RbObject = data[i];
      if(!this.contains(obj)) {
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
    if(this._list.length == 0) {
      this._selectedObjects = [];
    } else if(this._list.length == 1) {
      this.select(this._list[0]);
    } else if(this._list.length > 1) {
      if(this.dataTarget != null && this.dataTarget.objectuid != null) {
        this.selectUid(this.dataTarget.objectuid);
      } else if(this.selectedObject != null && !this._list.includes(this.selectedObject)) {
        this._selectedObjects = [];
      }
    }
    if(this.nextPage == 1) { 
      if(this.fetchAll == false && this._list.length > 10) {
        this.apiService.countObjects(this.objectname, this.resolvedFilter, this.userSearch).subscribe(data => {
          this.totalCount = data.result
        });
      } else {
        this.totalCount = this._list.length;
      }
    }
  }
  
  private receiveNewlyCreatedData(object: RbObject) {
    if(object.objectname == this.objectname && this.isLoading == false && this._list.includes(object) == false && (this.userSearch == null || this.userSearch == '') && (this.fetchAll == true || this._list.length < this.pageSize)) {
      if(this.filterService.applies(this.resolvedFilter, object)) {
        this._list.push(object);
        object.addSet(this);
        this.publishEvent('load');
        if(this._list.length == 1) {
          this._selectedObjects = [this._list[0]];
        }  
      }
    }
  }

  public objectUpdated(object: RbObject) {
    let filterApplies = this.filterService.applies(this.resolvedFilter, object);
    if(object.deleted || !filterApplies) {
      this.remove(object);
    } else  {
      this.publishEvent('update');
    }
  }

  public selectUid(uid: string) {
    let obj = this._list.find(o => o.uid == uid);
    if(obj != null) this.select(obj);
    else console.log("Trying a select an object that is not in the dataset");
  }

  public select(object: RbObject) {
    this._selectedObjects = [object];
    if(this.dataTarget != null) {
      this.dataTarget.objectuid = object != null ? object.uid : null; //Null object means 'deselect everything'
    }
    this.publishEvent('select');
  }

  public addOneToSelection(object: RbObject) {
    if(this._selectedObjects.length == 0) {
      this.select(object);
    } else {
      this._selectedObjects.push(object);
      if(this.dataTarget != null) {
        this.dataTarget.objectuid = null;
      }
      this.publishEvent('select');
    }
  }

  public addRangeToSelection(object: RbObject) {
    if(this._selectedObjects.length == 0) {
      this.select(object);
    } else {
      let i1 = this._list.indexOf(this._selectedObjects[0]);
      let i2 = this._list.indexOf(object);
      let start = Math.min(i1, i2);
      let end = Math.max(i1, i2);
      this._selectedObjects = this._list.slice(start, end + 1);
      if(this.dataTarget != null) {
        this.dataTarget.objectuid = null;
      }
      this.publishEvent('select');
    }
  }

  public filterSort(event: any) : boolean {
    let fetched = false;
    let filterChange = 'filter' in event && ValueComparator.notEqual(event.filter, this.userFilter);
    let sortChange = 'sort' in event && ValueComparator.notEqual(event.sort, this.userSort);
    let searchChange = 'search' in event && event.search != this.userSearch;
    if(filterChange || sortChange || searchChange) {
      if(filterChange) this.userFilter = event.filter;
      if(sortChange) this.userSort = event.sort;
      if(searchChange) this.userSearch = event.search;
      fetched = this.refreshData();
      if(this.dataTarget != null && this.ignoretarget == false) {
        if(filterChange) this.dataTarget.filter = event.filter;
        if(searchChange) this.dataTarget.search = event.search;
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
    let index = this._list.indexOf(obj);
    if(index > -1) {
      this._list.splice(index);
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
    let index = this._list.indexOf(obj);
    if(index > -1) {
      this._list.splice(index, 1);
      obj.removeSet(this);
      this.publishEvent("removed");
    }
  }

  private publishEvent(event: string) {
    this.observers.forEach((observer) => {
      observer.next(event);
    }); 
    if(this.datasetgroup != null) {
      this.datasetgroup.groupMemberEvent((this.id || this.name || this.objectname), event);
    }
  }
}
