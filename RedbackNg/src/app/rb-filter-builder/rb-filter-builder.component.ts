import { Component, OnInit, Output, EventEmitter, Inject } from '@angular/core';
import { CONTAINER_DATA } from 'app/tokens';
import { DateTimePopupConfig } from 'app/rb-popup-datetime/rb-popup-datetime.component';
import { OverlayRef } from '@angular/cdk/overlay';

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
      if(val != null && val.startsWith('*') && val.endsWith('*')) {
        return val.substring(1, val.length - 1);
      } else {
        return val;
      }
    }
    return this.filter[att];
  }

  setFilterValue(att: string, val: string) {
    let type = this.getAttributeType(att);
    if(type == 'string') {
      this.filter[att] = '*' + val + '*'; 
    }
  }

  clickOk() {
    this.done.emit(this.filter);
  }

}
