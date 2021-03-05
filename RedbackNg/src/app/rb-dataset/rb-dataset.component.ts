import { Component, Input } from '@angular/core';
import { DataTarget, RbObject } from '../datamodel';
import { DataService } from '../services/data.service';
import { MapService } from 'app/services/map.service';
import { Observable, Subscription } from 'rxjs';
import { ApiService } from 'app/services/api.service';
import { ReportService } from 'app/services/report.service';
import { ToastrService } from 'ngx-toastr';
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
  @Input('name') name: string;

  @Input('dataTarget') dataTarget: DataTarget;
  @Input('master') master: any;

  public id: String;
  private dataSubscription: Subscription;
  private _list: RbObject[] = [];
  private _selectedObject: RbObject;
  public totalCount: number = -1;
  public searchString: string;
  public userFilter: any;
  public userSort: any;
  public isLoading: boolean;
  public firstLoad: boolean = true;
  public nextPage: number;
  public pageSize: number;
  public fetchThreads: number;
  private observers: Observer<string>[] = [];


  constructor(
    private dataService: DataService,
    private apiService: ApiService,
    private mapService: MapService,
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
      if(event == 'select' || event == 'loaded') {
        this.refreshData();
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

  getObservable() : Observable<string>  {
    return new Observable<string>((observer) => {
      this.observers.push(observer);
    });
  }

  public reset() {
    if(this.dataTarget != null && (this.dataTarget.filter != null || this.dataTarget.search != null)) {
      if(ValueComparator.notEqual(this.dataTarget.filter, this.userFilter) || (this.dataTarget.search != this.searchString)) {
        this.searchString = this.dataTarget.search;
        this.userFilter = this.dataTarget.filter;
        this.userSort = null;
        this.refreshData();
        this.publishEvent('reset');
      }
    } else {
      this.searchString = null;
      this.userFilter = null;
      this.userSort = null;
    }
  }

  public refreshData() {
    if(this.active) {
      this.clearData();
      if(this.fetchAll) {
        setTimeout(() => this.fetchNextPage(), 1);
        setTimeout(() => this.fetchNextPage(), 500);
      } else {
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
    if(this.master == null || (this.master != null && this.relatedObject != null)) {
      const filter = this.mergeFilters();
      const sort = this.userSort != null ? this.userSort : this.dataTarget != null && this.dataTarget.sort != null ? this.dataTarget.sort : this.baseSort;
      const search = this.searchString;
      this.dataService.listObjects(this.object, filter, search, sort, this.nextPage, this.pageSize).subscribe({
        next: (data) => this.setData(data),
        error: (error) => {this.isLoading = false;}
      });
      this.nextPage = this.nextPage + 1;
      this.fetchThreads = this.fetchThreads + 1;
      this.dataService.subscribeObjectCreation(this.id, this.object, filter);
      this.isLoading = true;
    }
  }

  private mergeFilters() : any {
    let filter = {};
    if(this.baseFilter != null) {
      filter = this.mapService.mergeMaps(filter, this.baseFilter);
    }
    if(this.master != null && this.relatedObject != null) {
      filter = this.mapService.mergeMaps(filter, this.master.relationship);
    } 
    if(this.dataTarget != null) {
      filter = this.mapService.mergeMaps(filter, this.dataTarget.filter);
    } 
    if(this.userFilter != null) {
      filter = this.mapService.mergeMaps(filter, this.userFilter);
    }
    filter = this.mapService.resolveMap(filter, this.relatedObject, this.selectedObject, this.relatedObject);
    return filter;
  }

  private setData(data: RbObject[]) {
    for(var i = 0; i < data.length; i++) {
      let obj: RbObject = data[i];
      if(this._list.indexOf(obj) == -1) {
        this._list.push(obj);
        obj.addSet(this);
      }
    } 
    this.fetchThreads = this.fetchThreads - 1;
    if(this.fetchAll == true && data.length == this.pageSize) {
      this.fetchNextPage();
    } else if(this.fetchThreads == 0) {
      this.isLoading = false;
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
          const filter = this.mergeFilters();
          this.apiService.aggregateObjects(this.object, filter, this.searchString, [], [{function:'count', name:'count'}]).subscribe(data => {this.totalCount = data.list != null && data.list.length > 0 ? data.list[0].metrics.count : -1});
        } else {
          this.totalCount = this._list.length;
        }
      }
      this.publishEvent('loaded');
    }
  }
  
  private receiveNewlyCreatedData(object: RbObject) {
    if(object.objectname == this.object && this.isLoading == false && this._list.includes(object) == false && (this.searchString == null || this.searchString == '') && (this.fetchAll == true || this._list.length < this.pageSize)) {
      this._list.push(object);
      object.addSet(this);
      this.publishEvent('loaded');
      if(this._list.length == 1) {
        this._selectedObject = this._list[0];
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
      let data = this.mergeFilters();
      if(param != null) {
        data = this.mapService.mergeMaps(data, this.mapService.resolveMap(param, this.selectedObject, this.selectedObject, this.relatedObject))
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
      const filter = this.mergeFilters();
      this.dataService.exportObjects(this.object, filter, this.searchString);
    } else if(_name == 'report') {
      if(this.selectedObject != null) {
        this.reportService.launchReport(param, null, {"uid": this.selectedObject.uid});
      }
    } else if(_name == 'reportall') {
      const filter = this.mergeFilters();
      this.reportService.launchReport(param, null, filter);
    } else if(_name == 'reportlist') {
      const allFilter = this.mergeFilters();
      const selectedFilter = this.selectedObject != null ? {"uid": this.selectedObject.uid} : null;
      this.reportService.popupReportList(param, selectedFilter, allFilter);
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
    } else if(_name == 'executeglobal') {
      let funcParam = {
        "filter": this.mergeFilters(),
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
