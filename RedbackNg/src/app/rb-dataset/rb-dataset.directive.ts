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
  //public nullSelectedObject: RbObject;

  constructor(
    private dataService: DataService
  ) {   }

  ngOnInit() {
    //this.nullSelectedObject = new RbObject({uid:-1, objectname:null, domain:null, data:{}, related:{}, validation:{}}, null);
    //this.selectedObject = this.nullSelectedObject;
    this.getData();
  }

  ngOnChanges(changes: SimpleChanges) {
    if(this.active)
      this.getData();
    else
      this.list = [];
  }

  public getData() {
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
    if(filter != null)
      this.dataService.listObjects(this.objectname, filter).subscribe(data => this.list = data);
  }

  public select(item: RbObject) {
    this.selectedObject = item;
  }
}
