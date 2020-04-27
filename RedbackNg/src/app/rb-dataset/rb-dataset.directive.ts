import { Directive, OnInit, Input, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { ObjectResp, RbObject } from '../datamodel';
import { DataService } from '../data.service';
import { MapService } from 'app/map.service';

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

  @Input('userFilter') inputUserFilter: any;
  @Input('searchString') inputSearchString: any;
  @Input('selectedObject') inputSelectedObject: any;

  @Output('initiated') initated: EventEmitter<any> = new EventEmitter();
  @Output('userFilterChange') userFilterChange: EventEmitter<any> = new EventEmitter();
  @Output('searchStringChange') searchStringChange: EventEmitter<any> = new EventEmitter();
  @Output('selectedObjectChange') selectedObjectChange: EventEmitter<any> = new EventEmitter();

  
  public list: RbObject[] = [];
  public _selectedObject: RbObject;
  public searchString: string;
  public userFilter: any;
  public isLoading: boolean;
  public initiated: boolean = false;
  public firstLoad: boolean = true;

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
    this.initiated = true;
    this.refreshData();
    this.initated.emit(this);
  }

  public refreshData() {
    this.list = [];
    if(this.relatedFilter == null || (this.relatedFilter != null && this.relatedObject != null)) {
      const filter = this.mergeFilters();
      this.dataService.listObjects(this.objectname, filter, this.searchString).subscribe(
        data => this.setData(data)
      );
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
    this.list = data;
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
    if(name == 'create' || name == 'createInMemory') {
      let data = this.mergeFilters();
      if(param != null) {
        data = this.mapService.mergeMaps(data, this.mapService.resolveMap(param, this.selectedObject, this.selectedObject, this.relatedObject))
      }
      if(name == 'create') {
        this.dataService.createObject(this.objectname, null, data).subscribe(newObject => this.addObjectAndSelect(newObject));
      } else if(name == 'createInMemory') {
        this.dataService.createObjectInMemory(this.objectname, null, data).subscribe(newObject => this.addObjectAndSelect(newObject));
      }
    } else if(name == 'save') {
      
    } else if(name == 'executeAll') {
      let delay: number = 0;
      this.list.forEach((object) => {
        setTimeout(() => {
          this.dataService.executeObObject(object, param, null)
        }, delay);
        delay += 200;
      });
    } else if(this.selectedObject != null) {
      this.dataService.executeObObject(this.selectedObject, name, param);
    }
  }

  public addObjectAndSelect(obj: RbObject) {
    if(this.list.indexOf(obj) == -1) {
      this.list.unshift(obj);
      this.select(obj);
    }
  }



}
