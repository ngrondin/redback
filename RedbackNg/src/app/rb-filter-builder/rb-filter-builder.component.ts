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

  clickOk() {
    this.done.emit(this.filter);
  }

}
