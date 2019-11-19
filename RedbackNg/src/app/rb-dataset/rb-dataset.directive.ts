import { Directive, OnInit, Input } from '@angular/core';
import { ObjectResp, RbObject } from '../datamodel';
import { DataService } from '../data.service';

@Directive({
  selector: 'rb-dataset',
  exportAs: 'dataset',
})
export class RbDatasetDirective {

  @Input('object') objectname: string;
  @Input('baseFilter') baseFilter: string;

  public list: RbObject[] = [];
  public selectedObject: RbObject;

  constructor(
    private dataService: DataService
  ) {   }

  ngOnInit() {
    this.dataService.listObjects("workorder", {status: {$nin: ["closed", "cancel"]}}).subscribe(data => this.list = data);
  }

  public select(item: RbObject) {
    this.selectedObject = item;
  }
}
