import { Component, Input } from '@angular/core';
import { DataTarget, RbObject } from '../datamodel';
import { DataService } from '../services/data.service';
import { FilterService } from 'app/services/filter.service';
import { Observable, Subscription } from 'rxjs';
import { ApiService } from 'app/services/api.service';
import { ReportService } from 'app/services/report.service';
import { Observer } from 'rxjs';
import { ModalService } from 'app/services/modal.service';
import { ObserverProxy, ValueComparator } from 'app/helpers';
import { ErrorService } from 'app/services/error.service';
import { RbSetComponent } from 'app/abstract/rb-set';


@Component({
  selector: 'rb-dataset',
  templateUrl: './rb-dataset.component.html',
  styleUrls: ['./rb-dataset.component.css']
})
export class RbDatasetComponent extends RbSetComponent  {
  @Input('basesort') baseSort: any;
  @Input('name') name: string;
  @Input('fetchall') fetchAll: boolean = false;
  @Input('addrelated') addrelated: boolean = true;


  public id: string;
  private dataSubscription: Subscription;
  private _list: RbObject[] = [];
  private _selectedObject: RbObject;
  public totalCount: number = -1;
  public searchString: string;
  public userFilter: any = null;
  public userSort: any = null;
  public mergedFilter: any;
  public resolvedFilter: any;
  public firstLoad: boolean = true;
  public nextPage: number;
  public pageSize: number;
  public hasMorePages: boolean = true;
  public fetchThreads: number = 0;
  private observers: Observer<string>[] = [];
  public refreshOnActivate: boolean = true;


  constructor(
    private dataService: DataService,
    private apiService: ApiService,
    private filterService: FilterService,
  ) {
    super();
    this.id = "" + Math.floor(Math.random() * 10000);
  }

  setInit() {
    this.dataSubscription = this.dataService.getObjectCreateObservable().subscribe(object => this.receiveNewlyCreatedData(object));
    this.pageSize = this.fetchAll == true ? 250 : 50;
    this.fetchThreads = 0;
    this.hasMorePages = true;
    if(this.datasetgroup != null) {
      this.datasetgroup.register(this.name, this);
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
    return this.fetchThreads > 0;
  }

  public get canLoadData() : boolean {
    return this.active 
      && (this.master == null || (this.master != null && this.relatedObject != null))
      && (this.requiresuserfilter == false || this.userFilter != null);
  }

  getObservable() : Observable<string>  {
    return new Observable<string>((observer) => {
      this.observers.push(observer);
    });
  }

  public refreshData() {
    if(this.canLoadData) {
      this.clear();
      this.calcFilter();
      if(this.fetchAll) {
        setTimeout(() => this.fetchNextPage(), 1);
        setTimeout(() => this.fetchNextPage(), 200);
      } else {
        this.fetchNextPage();
      }
      this.refreshOnActivate = false;
    } else {
      this.refreshOnActivate = true;
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
      this.dataService.listObjects(this.object, this.resolvedFilter, search, sort, this.nextPage, this.pageSize, this.addrelated).subscribe({
        next: (data) => {
          this.fetchThreads--;
          this.setData(data);
        },
        error: (error) => {
          this.fetchThreads--;
        }
      });
      this.fetchThreads++;
      this.nextPage++;
    }
  }

  private calcFilter() {
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
    this.dataService.subscribeObjectCreation(this.id, this.object, this.resolvedFilter);
  }

  private setData(data: RbObject[]) {
    for(var i = 0; i < data.length; i++) {
      let obj: RbObject = data[i];
      if(this._list.indexOf(obj) == -1) {
        this._list.push(obj);
        obj.addSet(this);
      }
    } 
    if(data.length == this.pageSize) {
      if(this.fetchAll == true) {
        this.fetchNextPage();
      }
    } else {
      this.hasMorePages = false
    }
    if(this.fetchThreads == 0) {
      this.firstLoad = false;
      //console.log("dataset " + this.object + " finished loading (" + this._list.length + ")");
      this.publishEvent('load');
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
          this.apiService.aggregateObjects(this.object, this.resolvedFilter, this.searchString, [], [{function:'count', name:'count'}], null).subscribe(data => {this.totalCount = data.list != null && data.list.length > 0 ? data.list[0].metrics.count : -1});
        } else {
          this.totalCount = this._list.length;
        }
      }
    }
  }
  
  private receiveNewlyCreatedData(object: RbObject) {
    if(object.objectname == this.object && this.isLoading == false && this._list.includes(object) == false && (this.searchString == null || this.searchString == '') && (this.fetchAll == true || this._list.length < this.pageSize)) {
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

  public search(str: string) {
    if(str != this.searchString) {
      this.searchString = str;
      this.refreshData();
      if(this.dataTarget != null) {
        this.dataTarget.search = str;
      }
    }
  }

  public filterSort(event: any) {
    if(ValueComparator.notEqual(event.filter, this.userFilter) || ValueComparator.notEqual(event.sort, this.userSort)) {
      this.userFilter = event.filter;
      this.userSort = event.sort;
      this.refreshData();
      if(this.dataTarget != null && this.ignoretarget == false) {
        this.dataTarget.filter = event.filter;
      }
    }
  } 

  public create() {
    this.dataService.createObject(this.object, null, this.resolvedFilter).subscribe(newObject => this.addObjectAndSelect(newObject));
  }

  public delete(obj: RbObject) {
    this.dataService.deleteObject(obj).subscribe(result => this.remove(obj));
  }

  public addObjectAndSelect(obj: RbObject) {
    if(this._list.indexOf(obj) > -1) {
      this._list.splice(this._list.indexOf(obj));
    }
    this._list.unshift(obj);
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
      this.datasetgroup.groupMemberEvent(this.name, event);
    }
  }
}
