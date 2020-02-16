import { Component, OnInit, Output, EventEmitter, Inject } from '@angular/core';
import { CONTAINER_DATA } from 'app/tokens';
import { DateTimePopupConfig } from 'app/rb-popup-datetime/rb-popup-datetime.component';
import { OverlayRef, validateHorizontalPosition } from '@angular/cdk/overlay';

export class FilterBuilderConfig {
  filterConfig: any;
  initialFilter: any;
}

@Component({
  selector: 'rb-filter-builder',
  templateUrl: './rb-filter-builder.component.html',
  styleUrls: ['./rb-filter-builder.component.css']
})
export class RbFilterBuilderComponent implements OnInit {

  @Output() done: EventEmitter<any> = new EventEmitter();
  selectedAttribute: any;
  filter: any;

  datechoice: any = [
    { value: {$gt: "(new Date()).toISOString()"}, display: "Last 15 Minutes"},
    { value: {$gt: "(new Date((new Date()).getTime() - 3600000)).toISOString()"}, display: "Last Hour"}
  ]

  constructor(
    @Inject(CONTAINER_DATA) public config: FilterBuilderConfig, 
    public overlayRef: OverlayRef,
  ) { }

  ngOnInit() {
    this.filter = this.config.initialFilter;
    if(this.filter == null)
      this.filter = {};
  }

  addSelectedAttribute() {
    this.filter[this.selectedAttribute.attribute] = null;
    this.selectedAttribute = null;
  }

  removeAttribute(att: string) {
    delete this.filter[att];
  }

  getAttributeLabel(att: string) : string {
    if(this.config.filterConfig != null) {
      for(let a of this.config.filterConfig.attributes) {
        if(a.attribute == att) {
          return a.label;
        }
      }
    }
    return "Not found";
  }

  getAttributeType(att: string) : string {
    if(this.config.filterConfig != null) {
      for(let a of this.config.filterConfig.attributes) {
        if(a.attribute == att) {
          return a.type;
        }
      }
    }
    return "string";
  }

  getFilterValue(att: string) : string {
    let type = this.getAttributeType(att);
    if(type == 'string') {
      let val : string = this.filter[att];
      if(val != null && val.startsWith("'*") && val.endsWith("*'")) {
        return val.substring(2, val.length - 2);
      } else {
        return val;
      } 
    } else if(type == 'date') {
      return this.filter[att];
    }
    return this.filter[att];
  }

  setFilterValue(att: string, val: any) {
    let type = this.getAttributeType(att);
    if(type == 'string') {
      this.filter[att] = "'*" + val + "*'"; 
    } else if(type == 'date') {
      this.filter[att] = val;
    }
  }

  clickOk() {
    this.done.emit(this.filter);
  }

  dateOptionComparison(option: any, value: any) : boolean  {
    return JSON.stringify(option) == JSON.stringify(value);
  }
}
