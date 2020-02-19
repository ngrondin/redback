import { Directive, OnInit, Input, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { ObjectResp, RbObject } from '../datamodel';
import { DataService } from '../data.service';

@Directive({
  selector: 'rb-dataset',
  exportAs: 'dataset'
})
export class RbDatasetDirective implements OnChanges {

  @Input('object') objectname: string;
  @Input('relatedObject') relatedObject: RbObject;
  @Input('relatedFilter') relatedFilter: any;
  @Input('baseFilter') baseFilter: any;
  @Input('initialUserFilter') initialUserFilter: any;
  @Input('initialSearchString') initialSearchString: any;
  @Input('initialSelectedObject') initialSelectedObject: any;
  @Input('active') active: boolean;

  @Output('userFilterChanged') userFilterChanged: EventEmitter<any> = new EventEmitter();
  @Output('searchStringChanged') searchStringChanged: EventEmitter<any> = new EventEmitter();
  @Output('selectedObjectChanged') selectedObjectChanged: EventEmitter<any> = new EventEmitter();

  
  public list: RbObject[] = [];
  public selectedObject: RbObject;
  public searchString: string;
  public userFilter: any;
  public isLoading: boolean;
  public initiated: boolean = false;
  public firstLoad: boolean = true;

  constructor(
    private dataService: DataService
  ) {   }

  ngOnInit() {
    this.userFilter = this.initialUserFilter;
    this.searchString = this.initialSearchString;
    this.initiated = true;
    this.refreshData();
  }

  ngOnChanges(changes: SimpleChanges) {
    if("relatedObject" in changes || "active" in changes) {
      if(this.initiated) {
        if(this.active)
          this.refreshData();
      else
        this.list = [];
      }
    }
  }

  private mergeFilters() : any {
    let filter = {};
    if(this.baseFilter != null) {
      filter = this.mergeMaps(filter, this.baseFilter);
    }

    if(this.relatedFilter != null && this.relatedObject != null) {
      filter = this.mergeMaps(filter, this.relatedFilter);
    } 

    if(this.userFilter != null) {
      filter = this.mergeMaps(filter, this.userFilter);
    }
    
    filter = this.resolveMap(filter, this.relatedObject);

    return filter;
  }

  public refreshData() {
    this.list = [];
    this.selectedObject = null;
    if(this.relatedFilter == null || (this.relatedFilter != null && this.relatedObject != null)) {
      const filter = this.mergeFilters();
      this.dataService.listObjects(this.objectname, filter, this.searchString).subscribe(
        data => this.setData(data)
      );
      this.isLoading = true;
    }
  }

  private setData(data: RbObject[]) {
    this.list = data;
    this.isLoading = false;
    if(this.firstLoad && this.initialSelectedObject != null) {
      this.selectedObject = this.initialSelectedObject;
    }
    this.firstLoad = false;
    if(this.list.length == 1) {
      this.selectedObject = this.list[0];
    }
  }

  public select(item: RbObject) {
    this.selectedObject = item;
    this.selectedObjectChanged.emit(item);
  }

  public search(str: string) {
    this.searchString = str;
    this.refreshData();
    this.searchStringChanged.emit(str);
  }

  public filter(flt: any) {
    this.userFilter = flt;
    this.refreshData();
    this.userFilterChanged.emit(flt);
  } 

  public action(name: string, param: string) {
    if(name == 'create') {
      let data = this.mergeFilters();
      if(param != null) {
        data = this.mergeMaps(data, this.resolveMap(param, this.selectedObject))
      }
      this.dataService.createObject(this.objectname, data).subscribe(newObject => this.addObjectAndSelect(newObject));
    } else if(name == 'save') {
      
    } else if(this.selectedObject != null) {
      this.dataService.executeObObject(this.selectedObject, name, param);
    }
  }

  public addObjectAndSelect(obj: RbObject) {
    if(this.list.indexOf(obj) == -1) {
      this.list.push(obj);
      this.selectedObject = obj;
    }
  }


  private mergeMaps(map1: any, map2: any) : any {
    let map: any = {};
    for (const key in map1) {
      let value = map1[key];
      map[key] = value;
    }
    for (const key in map2) {
      let value = map2[key];
      map[key] = value;
    }
    return map;
  }

  private resolveMap(inMap: any, obj: RbObject) : any {
    let outMap: any = {};
    let relatedObject = this.relatedObject;
    let selectedObject = this.selectedObject;
    let uid = null;
    let varString = "";
    if(obj != null && typeof obj != 'undefined') {
      uid = obj.uid;
      for(const attr in obj.data) {
        let val = obj.data[attr];
        if(typeof val == 'object') {
          val = JSON.stringify(val);
        } 
        if(typeof val == 'string') {
          val = "'" + val.replace(/\'/g, "\\'").replace(/\"/g, "\\\"") + "'";
        } 
        varString = varString + "var " + attr + " = " + val + ";"
      }
    } 
    try {
      eval(varString);
    } catch(e) {}

    for (const key in inMap) {
      let value = inMap[key];
      if(typeof value == "string") {
        value = eval(value);
      } else if(typeof value == "object") {
        value = this.resolveMap(value, obj);
      }
      outMap[key] = value;
    }
    return outMap;
  }
}
