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
  @Input('active') active: boolean;

  public list: RbObject[] = [];
  public selectedObject: RbObject;
  public searchString: string;
  public userFilter: any;
  public isLoading: boolean;

  constructor(
    private dataService: DataService
  ) {   }

  ngOnInit() {
    this.refreshData();
  }

  ngOnChanges(changes: SimpleChanges) {
    if(this.active)
      this.refreshData();
    else
      this.list = [];
  }

  private mergeFilters() : any {
    let filter = {};
    if(this.baseFilter != null) {
      for (const key in this.baseFilter) {
        let value = this.baseFilter[key];
        filter[key] = value;
      }
    }

    if(this.relatedFilter != null) {
      if(this.relatedObject != null) {
        for (const key in this.relatedFilter) {
          let value = this.relatedFilter[key];
          if(typeof value == "string"  &&  value.startsWith("[") &&  value.endsWith("]")) {
            let attr = value.substring(1, value.length - 1);
            if(attr == 'uid')
              value = this.relatedObject.uid;
            else
              value = this.relatedObject.data[attr];
          }
          filter[key] = value;
        }
      }
    } 

    if(this.userFilter != null) {
      for (const key in this.userFilter) {
        let value = this.userFilter[key];
        filter[key] = value;
      }
    }
    
    return filter;
  }

  public refreshData() {
    this.list = [];
    this.selectedObject = null;
    if(this.relatedFilter == null || (this.relatedFilter != null && this.relatedObject != null)) {
      const filter = this.mergeFilters();
      this.dataService.listObjects(this.objectname, filter, this.searchString).subscribe(data => this.setData(data));
      this.isLoading = true;
    }
  }

  private setData(data: RbObject[]) {
    this.list = data;
    this.isLoading = false;
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
      this.dataService.createObject(this.objectname, this.mergeFilters()).subscribe(newObject => this.addObjectAndSelect(newObject));
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
}
