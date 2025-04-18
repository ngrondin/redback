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
import { UserprefService } from 'app/services/userpref.service';


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
  public defaultUserFilter: any = null;
  public defaultUserSort: any = null;
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
    private userprefService: UserprefService
  ) {
    super();
    this.uid = "" + Math.floor(Math.random() * 10000);
  }

  setInit() {
    this.dataSubscription = this.dataService.getObjectUpdateObservable().subscribe(object => this.receiveUpdatedObject(object));
    this.pageSize = this.fetchAll == true ? 250 : 50;
    this._loading = false;
    this.hasMorePages = true;
    if(this.datasetgroup != null) {
      this.datasetgroup.register((this.id || this.name || this.objectname), this);
    }
    let pref = this.id != null ? this.userprefService.getCurrentViewUISwitch('dataset', this.id) : null;
    if(pref != null) {
      let def = pref.saved.find(s => s.default == true);
      if(def != null) {
        this.defaultUserFilter = this.filterService.removePrefixDollarSign(def.filter);
        this.defaultUserSort = def.sort;
        if(this.userFilter == null) this.userFilter = this.defaultUserFilter;
        if(this.userSort == null) this.userSort = this.defaultUserSort;
      }
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
    } else if(event == 'globalupdate') {
      this.refreshData();
    }
  }

  onActivationEvent(state: any) {
    if(state == true && this.refreshOnActivate) {
      this.refreshData(true);
    }
  }

  onDataTargetEvent(dt: DataTarget) {
    let fetched = this.filterSort({
      filter: dt.filter ?? this.defaultUserFilter, 
      sort: dt.sort ?? this.defaultUserSort, 
      search: dt.search
    });
    if(!fetched && dt.select != null) {
      this.selectByFilter(dt.select); //Dataset not refreshed, directly selecting the object (assuming it is already in the list)
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

  public get hasUserFilter(): boolean {
    return this.userFilter != null && Object.keys(this.userFilter).length > 0;
  }

  public get canLoadData() : boolean {
    return this.initiated
      && this.active 
      && (this.master == null || (this.master != null && this.relatedObject != null))
      && (this.requiresuserfilter == false || this.hasUserFilter)
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
      let prevSort = this.resolvedSort;
      this.resolveFilterSort();
      let filterChanged = ValueComparator.notEqual(prevFilter, this.resolvedFilter);
      let searchChanged = ValueComparator.notEqual(prevSearch, this.resolvedSearch);
      let sortChanged = ValueComparator.notEqual(prevSort, this.resolvedSort);
      if(filterChanged || searchChanged || sortChanged || !onlyIfFilterChanged) {
        this.clear(false);
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

  public clear(resetResolved = true) {
    this.nextPage = 0;
    this.hasMorePages = true;
    this.totalCount = -1;
    for(let obj of this._list) {
      obj.removeSet(this);
    }
    this._list = [];
    this._selectedObjects = [];
    if(resetResolved) {
      this.resolvedFilter = null;
      this.resolvedSort = null;
      this.resolvedSearch = null;  
    }
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
      if(this.dataTarget != null && this.dataTarget.select != null) {
        this.selectByFilter(this.dataTarget.select);
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
  
  private receiveUpdatedObject(object: RbObject) {
    if(object.objectname == this.objectname && object.deleted == false && this._list.includes(object) == false && this.isLoading == false && (this.userSearch == null || this.userSearch == '') && (this.fetchAll == true || this._list.length < this.pageSize)) {
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
    } 
    this.publishEvent('update');
  }

  public selectByFilter(filter: any) {
    this._selectedObjects = [];
    for(var o of this._list) {
      if(this.filterService.applies(filter, o)) this.addOneToSelection(o);
    }
  }

  public select(object: RbObject) {
    this._selectedObjects = [object];
    if(this.dataTarget != null && object != null) {
      this.dataTarget.select = {uid: object.uid};
    }
    this.publishEvent('select');
  }

  public addOneToSelection(object: RbObject) {
    if(this._selectedObjects.length == 0) {
      this.select(object);
    } else {
      this._selectedObjects.push(object);
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
      this.publishEvent('select');
    }
  }

  public filterSort(event: any) : boolean {
    let fetched = false;
    let newFilter = event.filter;// ?? this.defaultUserFilter;
    let newSort = event.sort;// ?? this.defaultUserSort;
    let newSearch = event.search;
    let filterChange = 'filter' in event && ValueComparator.notEqual(newFilter, this.userFilter);
    let sortChange = 'sort' in event && ValueComparator.notEqual(newSort, this.userSort);
    let searchChange = 'search' in event && newSearch != this.userSearch;
    if(filterChange || sortChange || searchChange) {
      if(filterChange) this.userFilter = newFilter;
      if(sortChange) this.userSort = newSort;
      if(searchChange) this.userSearch = newSearch;
      fetched = this.refreshData();
      if(this.dataTarget != null && this.ignoretarget == false) {
        if(filterChange) this.dataTarget.filter = newFilter;
        if(searchChange) this.dataTarget.search = newSearch;
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
