import { Component, Input } from '@angular/core';
import { DataTarget, RbObject } from '../datamodel';
import { DataService } from '../services/data.service';
import { FilterService } from 'app/services/filter.service';
import { Observable, Subscription } from 'rxjs';
import { ApiService } from 'app/services/api.service';
import { ReportService } from 'app/services/report.service';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { Observer } from 'rxjs';
import { ModalService } from 'app/services/modal.service';
import { ValueComparator } from 'app/helpers';
import { ErrorService } from 'app/services/error.service';


@Component({
  selector: 'rb-dataset',
  templateUrl: './rb-dataset.component.html',
  styleUrls: ['./rb-dataset.component.css']
})
export class RbDatasetComponent extends RbContainerComponent  {
  @Input('object') object: string;
  @Input('basefilter') baseFilter: any;
  @Input('basesort') baseSort: any;
  @Input('fetchall') fetchAll: boolean = false;
  @Input('fetchonreset') fetchonreset: boolean = true;
  @Input('ignoretarget') ignoretarget: boolean = false;
  @Input('name') name: string;

  @Input('dataTarget') dataTarget: DataTarget;
  @Input('master') master: any;

  public id: String;
  private dataSubscription: Subscription;
  private _list: RbObject[] = [];
  private _selectedObject: RbObject;
  public totalCount: number = -1;
  public searchString: string;
  public userFilter: any = null;
  public userSort: any = null;
  public filter: any;
  public firstLoad: boolean = true;
  public nextPage: number;
  public pageSize: number;
  public fetchThreads: number = 0;
  private observers: Observer<string>[] = [];


  constructor(
    private dataService: DataService,
    private apiService: ApiService,
    private filterService: FilterService,
    private reportService: ReportService,
    private modalService: ModalService,
    private errorService: ErrorService
  ) {
    super();
  }

  containerInit() {
    this.id = "" + Math.floor(Math.random() * 10000);
    this.dataSubscription = this.dataService.getObjectCreateObservable().subscribe(object => this.receiveNewlyCreatedData(object));
    this.pageSize = this.fetchAll == true ? 250 : 50;
    this.fetchThreads = 0;
    if(this.datasetgroup != null) {
      this.datasetgroup.register(this.name, this);
    }
  }

  containerDestroy() {
    this.clearData();
    this.dataSubscription.unsubscribe();
  }

  onDatasetEvent(event: string) {
    if(this.active == true) {
      if(event == 'select') {
        this.refreshData();
      } else if(event == 'loaded') {

      } else if(event == 'cleared') {
        this.clearData();
      }
    }
  }

  onActivationEvent(state: any) {
    state == true ? this.refreshData() : this.clearData();
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
    return this.active && (this.master == null || (this.master != null && this.relatedObject != null));
  }

  getObservable() : Observable<string>  {
    return new Observable<string>((observer) => {
      this.observers.push(observer);
    });
  }

  public reset() {
    if(this.dataTarget != null && (this.dataTarget.filter != null || this.dataTarget.search != null)) {
      if(ValueComparator.notEqual(this.dataTarget.filter, this.userFilter) || (this.dataTarget.search != this.searchString)) {
        this.searchString = this.dataTarget.search || null;
        this.userFilter = this.dataTarget.filter || null;
        this.userSort = null;
        if(this.fetchonreset && this.active) this.refreshData();
        this.publishEvent('reset');
      }
    } else {
      this.searchString = null;
      this.userFilter = null;
      this.userSort = null;
    }
  }

  public refreshData() {
    if(this.canLoadData) {
      this.clearData();
      this.calcFilter();
      if(this.fetchAll) {
        this.fetchThreads = 2;
        setTimeout(() => this.fetchNextPage(), 1);
        setTimeout(() => this.fetchNextPage(), 200);
      } else {
        this.fetchThreads = 1;
        this.fetchNextPage();
      }
    }
  }

  public clearData() {
    this.nextPage = 0;
    this.totalCount = -1;
    for(let obj of this._list) {
      obj.removeSet(this);
    }
    this._list = [];
    this._selectedObject = null;
    this.publishEvent('cleared');
  }

  public fetchNextPage() {
    const sort = this.userSort != null ? this.userSort : this.dataTarget != null && this.dataTarget.sort != null ? this.dataTarget.sort : this.baseSort;
    const search = this.searchString;
    this.dataService.listObjects(this.object, this.filter, search, sort, this.nextPage, this.pageSize).subscribe({
      next: (data) => this.setData(data),
      error: (error) => {this.fetchThreads--;}
    });
    this.nextPage = this.nextPage + 1;
  }

  private calcFilter() {
    let filter = {};
    if(this.baseFilter != null) {
      filter = this.filterService.mergeFilters(filter, this.baseFilter);
    }
    if(this.master != null && this.relatedObject != null) {
      filter = this.filterService.mergeFilters(filter, this.master.relationship);
    } 
    /*if(this.dataTarget != null) {
      filter = this.filterService.mergeFilters(filter, this.dataTarget.filter);
    }*/ 
    if(this.userFilter != null) {
      filter = this.filterService.mergeFilters(filter, this.userFilter);
    }
    filter = this.filterService.resolveFilter(filter, this.relatedObject, this.selectedObject, this.relatedObject);
    this.filter = filter;
    this.dataService.subscribeObjectCreation(this.id, this.object, this.filter);
  }

  private setData(data: RbObject[]) {
    for(var i = 0; i < data.length; i++) {
      let obj: RbObject = data[i];
      if(this._list.indexOf(obj) == -1) {
        this._list.push(obj);
        obj.addSet(this);
      }
    } 
    if(data.length == this.pageSize && this.fetchAll == true) {
      this.fetchNextPage();
    } else {
      this.fetchThreads--;
    }
    if(this.fetchThreads == 0) {
      console.log("dataset " + this.object + " finished loading");
      this.firstLoad = false;
      if(this._list.length == 0) {
        this._selectedObject = null;
      } else if(this._list.length == 1) {
        this._selectedObject = this._list[0];
      } else if(this._list.length > 1) {
        if(this.selectedObject != null && !this._list.includes(this.selectedObject)) {
          this._selectedObject = null;
        }
      }
      if(this.nextPage == 1) { 
        if(this.fetchAll == false && this._list.length > 10) {
          this.apiService.aggregateObjects(this.object, this.filter, this.searchString, [], [{function:'count', name:'count'}]).subscribe(data => {this.totalCount = data.list != null && data.list.length > 0 ? data.list[0].metrics.count : -1});
        } else {
          this.totalCount = this._list.length;
        }
      }
      this.publishEvent('loaded');
    }
  }
  
  private receiveNewlyCreatedData(object: RbObject) {
    if(object.objectname == this.object && this.isLoading == false && this._list.includes(object) == false && (this.searchString == null || this.searchString == '') && (this.fetchAll == true || this._list.length < this.pageSize)) {
      if(this.filterService.applies(this.filter, object)) {
        this._list.push(object);
        object.addSet(this);
        this.publishEvent('loaded');
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
    this.searchString = str;
    this.refreshData();
    if(this.dataTarget != null) {
      this.dataTarget.search = str;
    }
  }

  public filterSort(event: any) {
    this.userFilter = event.filter;
    this.userSort = event.sort;
    this.refreshData();
    if(this.dataTarget != null) {
      this.dataTarget.filter = event.filter;
    }
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
    }
  }

  public publishEvent(event: string) {
    this.observers.forEach((observer) => {
      observer.next(event);
    }); 
    if(this.datasetgroup != null) {
      this.datasetgroup.groupMemberEvent(this.name, event);
    }
  }

  public action(name: string, param: string) {
    let _name: string = name.toLowerCase();
    if(_name == 'create' || _name == 'createinmemory') {
      let data = this.filter;
      if(param != null) {
        data = this.filterService.mergeFilters(this.filter, this.filterService.resolveFilter(param, this.selectedObject, this.selectedObject, this.relatedObject))
      }
      if(_name == 'create') {
        this.dataService.createObject(this.object, null, data).subscribe(newObject => this.addObjectAndSelect(newObject));
      } else if(_name == 'createinmemory') {
        this.dataService.createObjectInMemory(this.object, null, data).subscribe(newObject => this.addObjectAndSelect(newObject));
      }
    } else if(_name == 'delete') {
      if(this.selectedObject != null) {
        this.dataService.deleteObject(this.selectedObject).subscribe(result => this.removeSelected());
      }
    } else if(_name == 'save') {
      
    } else if(_name == 'exportall') {
      this.dataService.exportObjects(this.object, this.filter, this.searchString);
    } else if(_name == 'report') {
      if(this.selectedObject != null) {
        this.reportService.launchReport(param, null, {"uid": this.selectedObject.uid});
      }
    } else if(_name == 'reportall') {
      this.reportService.launchReport(param, null, this.filter);
    } else if(_name == 'reportlist') {
      const selectedFilter = this.selectedObject != null ? {"uid": this.selectedObject.uid} : null;
      this.reportService.popupReportList(param, selectedFilter, this.filter);
    } else if(_name == 'execute') {
      this.dataService.executeObject(this.selectedObject, param, null);
    } else if(_name == 'executeall') {
      let delay: number = 0;
      this._list.forEach((object) => {
        setTimeout(() => {
          this.dataService.executeObject(object, param, null)
        }, delay);
        delay += 50;
      });
    } else if(_name == 'executemaster') {
      if(this.relatedObject != null) {
        this.dataService.executeObject(this.relatedObject, param, null);
      }
    } else if(_name == 'executeglobal') {
      let funcParam = {
        "filter": this.filter,
        "selecteduid": (this.selectedObject != null ? this.selectedObject.uid : null)
      }
      this.dataService.executeGlobal(param, funcParam);
    } else if(_name == 'executedomain') {
      if(this.selectedObject != null) {
        this.apiService.executeDomain(param, this.selectedObject.domain, {"uid": this.selectedObject.uid}).subscribe(
          json => {},
          error => this.errorService.receiveHttpError(error)
        );
      }
    } else if(_name == 'modal') {
      this.modalService.open(param);
    } else if(this.selectedObject != null) {
      this.dataService.executeObject(this.selectedObject, name, param);
    }
  }



}
