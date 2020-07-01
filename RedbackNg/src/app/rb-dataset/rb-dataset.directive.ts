import { Directive, OnInit, Input, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { ObjectResp, RbObject } from '../datamodel';
import { DataService } from '../data.service';
import { MapService } from 'app/map.service';
import { Observable, Subscription } from 'rxjs';

@Directive({
  selector: 'rb-dataset',
  exportAs: 'dataset'
})
export class RbDatasetDirective implements OnChanges {

  @Input('active') active: boolean;
  @Input('object') objectname: string;
  @Input('relatedObject') relatedObject: RbObject;
  @Input('relatedFilter') relatedFilter: any;
  @Input('baseFilter') baseFilter: any;
  @Input('fetchAll') fetchAll: boolean = false;

  @Input('userFilter') inputUserFilter: any;
  @Input('searchString') inputSearchString: any;
  @Input('selectedObject') inputSelectedObject: any;

  @Output('initiated') initated: EventEmitter<any> = new EventEmitter();
  @Output('userFilterChange') userFilterChange: EventEmitter<any> = new EventEmitter();
  @Output('searchStringChange') searchStringChange: EventEmitter<any> = new EventEmitter();
  @Output('selectedObjectChange') selectedObjectChange: EventEmitter<any> = new EventEmitter();

  public id: String;
  public dataSubscription: Subscription;
  public list: RbObject[] = [];
  public _selectedObject: RbObject;
  public searchString: string;
  public userFilter: any;
  public isLoading: boolean;
  public initiated: boolean = false;
  public firstLoad: boolean = true;
  public page: number;

  constructor(
    private dataService: DataService,
    private mapService: MapService
  ) {   }

  ngOnChanges(changes: SimpleChanges) {
    let doRefresh: boolean = false;
    if("relatedObject" in changes || "active" in changes) {
      doRefresh = true;
    }
    if("inputUserFilter" in changes && this.userFilter != this.inputUserFilter) {
      this.userFilter = this.inputUserFilter;
      doRefresh = true;
    }
    if("inputSearchString" in changes && this.searchString != this.inputSearchString) {
      this.searchString = this.inputSearchString;
      doRefresh = true;
    }
    if("inputSelectedObject" in changes && this.selectedObject != this.inputSelectedObject) {
      this._selectedObject = this.inputSelectedObject;
      doRefresh = true;
    }
    if(doRefresh && this.initiated && this.active) {
      this.refreshData();
    }
  }

  ngOnInit() {
    this.id = "" + Math.floor(Math.random() * 10000);
    this.dataSubscription = this.dataService.getObjectCreateObservable().subscribe(object => this.receiveNewlyCreatedData(object));
    this.initiated = true;
    this.refreshData();
    this.initated.emit(this);
  }

  ngOnDestroy() {
    this.dataSubscription.unsubscribe();
  }

  public refreshData() {
    this.page = 0;
    this.list = [];
    this.fetchPage(this.page);
  }

  public fetchPage(page: number) {
    if(this.relatedFilter == null || (this.relatedFilter != null && this.relatedObject != null)) {
      const filter = this.mergeFilters();
      this.dataService.listObjects(this.objectname, filter, this.searchString, this.page).subscribe(
        data => this.setData(data)
      );
      this.dataService.subscribeObjectCreation(this.id, this.objectname, filter);
      this.isLoading = true;
    }
  }

  private mergeFilters() : any {
    let filter = {};
    if(this.baseFilter != null) {
      filter = this.mapService.mergeMaps(filter, this.baseFilter);
    }

    if(this.relatedFilter != null && this.relatedObject != null) {
      filter = this.mapService.mergeMaps(filter, this.relatedFilter);
    } 

    if(this.userFilter != null) {
      filter = this.mapService.mergeMaps(filter, this.userFilter);
    }
    
    filter = this.mapService.resolveMap(filter, this.relatedObject, this.selectedObject, this.relatedObject);

    return filter;
  }

  private setData(data: RbObject[]) {
    this.list = this.list.concat(data);
    if(this.fetchAll && data.length == 50) {
      this.page = this.page + 1;
      this.fetchPage(this.page);
    } else {
      this.isLoading = false;
      this.firstLoad = false;
      if(this.list.length == 0) {
        this._selectedObject = null;
      } else if(this.list.length == 1) {
        this._selectedObject = this.list[0];
      } else if(this.list.length > 1) {
        if(this.selectedObject != null && !this.list.includes(this.selectedObject)) {
          this._selectedObject = null;
        }
      }
    }
  }
  
  private receiveNewlyCreatedData(object: RbObject) {
    if(object.objectname == this.objectname && this.list.includes(object) == false && (this.searchString == null || this.searchString == '') && this.list.length < 50) {
      this.list.push(object);
    }
  }

  public get selectedObject(): RbObject {
    return this._selectedObject;
  }

  public set selectedObject(obj: RbObject) {
    this.select(obj);
  }

  public select(item: RbObject) {
    this._selectedObject = item;
    this.selectedObjectChange.emit(item);
  }

  public search(str: string) {
    this.searchString = str;
    this.refreshData();
    this.searchStringChange.emit(str);
  }

  public filter(flt: any) {
    this.userFilter = flt;
    this.refreshData();
    this.userFilterChange.emit(flt);
  } 

  public action(name: string, param: string) {
    let _name: string = name.toLowerCase();
    if(_name == 'create' || _name == 'createinmemory') {
      let data = this.mergeFilters();
      if(param != null) {
        data = this.mapService.mergeMaps(data, this.mapService.resolveMap(param, this.selectedObject, this.selectedObject, this.relatedObject))
      }
      if(_name == 'create') {
        this.dataService.createObject(this.objectname, null, data).subscribe(newObject => this.addObjectAndSelect(newObject));
      } else if(_name == 'createinmemory') {
        this.dataService.createObjectInMemory(this.objectname, null, data).subscribe(newObject => this.addObjectAndSelect(newObject));
      }
    } else if(_name == 'save') {
      
    } else if(_name == 'executeall') {
      let delay: number = 0;
      this.list.forEach((object) => {
        setTimeout(() => {
          this.dataService.executeObject(object, param, null)
        }, delay);
        delay += 200;
      });
    } else if(_name == 'executeglobal') {
      this.dataService.executeGlobal(param, null);
    } else if(this.selectedObject != null) {
      this.dataService.executeObject(this.selectedObject, name, param);
    }
  }

  public addObjectAndSelect(obj: RbObject) {
    if(this.list.indexOf(obj) == -1) {
      this.list.unshift(obj);
      this.select(obj);
    }
  }



}
