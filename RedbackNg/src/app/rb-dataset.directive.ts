import { Directive, OnInit, Input } from '@angular/core';
import { ObjectResp, RbObject } from './datamodel';
import { DataService } from './data.service';

@Directive({
  selector: 'rb-dataset'
})
export class RbDatasetDirective {

  @Input('object') objectname: string;
  @Input('baseFilter') baseFilter: string;

  list: RbObject[];
  selectedObject: RbObject[];

  constructor(
    private dataService: DataService
  ) {   }

  ngOnInit() {
    this.dataService.listObjects("workorder", {status: {$nin: ["closed", "cancel"]}}).subscribe(data => alert(data));
  }
}
