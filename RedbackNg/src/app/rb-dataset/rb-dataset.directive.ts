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
  public searchFilter: any;
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

  public refreshData() {
    this.list = [];
    this.selectedObject = null;
    let filter = null;
    if(this.baseFilter != null)
      filter = this.baseFilter;
    else
      filter = {};
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
      } else {
        filter = null;
      }
    } 

    if(this.searchFilter != null) {
      for (const key in this.searchFilter) {
        let value = this.searchFilter[key];
        filter[key] = value;
      }
    }

    if(filter != null) {
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
    this.searchFilter = flt;
    this.refreshData();
  } 
}
