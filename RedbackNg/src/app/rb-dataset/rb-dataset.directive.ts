import { Directive, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
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
  @Input('active') active: boolean;

  public list: RbObject[] = [];
  public selectedObject: RbObject;
  public searchString: string;
  public userFilter: any;
  public isLoading: boolean;
  public initiated: boolean = false;

  constructor(
    private dataService: DataService
  ) {   }

  ngOnInit() {
    this.refreshData();
    this.initiated = true;
  }

  ngOnChanges(changes: SimpleChanges) {
    if("initialUserFilter" in changes) {
      this.userFilter = this.initialUserFilter;
    }

    if(this.initiated) {
      if(this.active)
        this.refreshData();
    else
      this.list = [];
    }
  }

  private mergeFilters() : any {
    let filter = {};
    if(this.baseFilter != null) {
      filter = this.mergeMaps(filter, this.baseFilter);
    }

    if(this.relatedFilter != null) {
      if(this.relatedObject != null) {
        filter = this.mergeMaps(filter, this.resolveMap(this.relatedFilter, this.relatedObject));
      }
    } 

    if(this.userFilter != null) {
      filter = this.mergeMaps(filter, this.userFilter);
    }
    
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
    if(this.list.length == 1) {
      this.selectedObject = this.list[0];
    }
  }

  public select(item: RbObject) {
    this.selectedObject = item;
  }

  public search(str: string) {
    this.searchString = str;
    this.refreshData();
  }

  public filter(flt: any) {
    this.userFilter = flt;
    this.refreshData();
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
    for (const key in inMap) {
      let value = inMap[key];
      if(typeof value == "string"  &&  value.startsWith("[") &&  value.endsWith("]")) {
        let attr = value.substring(1, value.length - 1);
        if(attr == 'uid')
          value = obj.uid;
        else
          value = obj.data[attr];
      }
      outMap[key] = value;
    }
    return outMap;
  }
}
