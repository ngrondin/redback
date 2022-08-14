import { Component, OnInit, Output, EventEmitter, Inject } from '@angular/core';
import { CONTAINER_DATA } from 'app/tokens';
import { OverlayRef } from '@angular/cdk/overlay';
import { DataService } from 'app/services/data.service';
import { FilterService } from 'app/services/filter.service';
import { ValueComparator } from 'app/helpers';
import { FilterConfig, SortConfig, FilterAttributeConfig, SortAttributeConfig, FilterBuilderConfig } from './rb-filter-builder-configs';
import { FilterItemConstruct, SortItemConstruct } from './rb-filter-builder-constructs';



@Component({
  selector: 'rb-filter-builder',
  templateUrl: './rb-filter-builder.component.html',
  styleUrls: ['./rb-filter-builder.component.css']
})
export class RbFilterBuilderComponent implements OnInit {

  @Output() done: EventEmitter<any> = new EventEmitter();
  filterConfig: FilterConfig;
  filter: any;
  filterConstructs: FilterItemConstruct[];
  sortConfig: SortConfig;
  sort: any;
  sortConstructs: SortItemConstruct[];
  _attributeToAddToFilter: any;
  _attributeToAddToSort: any;
  changed: boolean = false;

  datechoice: any = [
    { value: "last15", display: "Last 15 Minutes"},
    { value: "lasthour", display: "Last Hour"},
    { value: "lastday", display: "Last Day"},
    { value: "since", display: "Since"},
    { value: "next15", display: "Next 15 Minutes"},
    { value: "nexthour", display: "Next Hour"},
    { value: "nextday", display: "Next Day"},
    { value: "until", display: "Until"},
    { value: "between", display: "Between"}
  ]

  constructor(
    @Inject(CONTAINER_DATA) public config: FilterBuilderConfig, 
    public overlayRef: OverlayRef,
    public dataService: DataService,
    private filterService: FilterService
  ) { 
    this.filter = this.config.initialFilter;
    if(this.config.filterConfig != null) {
      this.filterConfig = new FilterConfig(this.config.filterConfig);
    }
    this.sort = this.config.initialSort;
    if(this.config.sortConfig != null) {
      this.sortConfig = new SortConfig(this.config.sortConfig);
    }
  }

  ngOnInit() {
    this.filterConstructs = [];
    this.sortConstructs = [];
    if(this.filter == null) {
      this.filter = {};
    } else {
      for(let key in this.filter) {
        let fac: FilterAttributeConfig = this.filterConfig.getAttributeConfig(key);
        let fic = new FilterItemConstruct(fac, this.filter[key]);
        this.filterConstructs.push(fic);
        this.getAggregatesFor(fac);
      }
    }

    if(this.sort == null) {
      this.sort = {};
    } else {
      for(let key in this.sort) {
        let order: number = parseInt(key);
        let sortItem: any = this.sort[key];
        let sic = new SortItemConstruct(this.sortConfig.getAttributeConfig(sortItem.attribute), order, sortItem);
        this.sortConstructs.push(sic);
      }
    }

  }


  get availableAttributes() : FilterAttributeConfig[] {
    let ret : FilterAttributeConfig[] = [];
    for(let fac of this.filterConfig.attributes) {
      if(fac.appliesToObject == null || (fac.appliesToObject != null && fac.appliesToObject == this.config.objectname)) {
        ret.push(fac);
      }
    }
    return ret;
  }

  get attributeToAddToFilter() : any {
    return this._attributeToAddToFilter;
  }

  set attributeToAddToFilter(val: any) {
    this._attributeToAddToFilter = val;
    let fac: FilterAttributeConfig = val;
    let fic = new FilterItemConstruct(fac, null);
    this.filterConstructs.push(fic);
    this.getAggregatesFor(fac);
    this.changed = true;
    setTimeout(() => {this._attributeToAddToFilter = null}, 200);
  }

  get attributeToAddToSort() : any {
    return this._attributeToAddToSort;
  }

  set attributeToAddToSort(val: any) {
    this._attributeToAddToSort = val;
    let sac: SortAttributeConfig = val;
    let sic = new SortItemConstruct(sac, this.sortConstructs.length, {dir:1});
    this.sortConstructs.push(sic);
    this.changed = true;
    setTimeout(() => {this._attributeToAddToSort = null}, 200);
  }

  get empty() : boolean {
    return this.filterConstructs.length == 0 && this.sortConstructs.length == 0;
  }

  removeFilterItem(fic: FilterItemConstruct) {
    this.filterConstructs.splice(this.filterConstructs.indexOf(fic), 1);
    this.changed = true;
  }

  removeSortItem(sic: SortItemConstruct) {
    this.sortConstructs.splice(this.sortConstructs.indexOf(sic), 1);
    this.changed = true;
  }

  toggleDir(sic: SortItemConstruct) {
    sic.direction = (-1 * sic.direction );
    this.changed = true;
  }

  getAggregatesFor(fac: FilterAttributeConfig) {
    if(fac.type == 'multiselect') {
      let fltr = {};
      for (const key in this.filter) {
        if(key != fac.attribute) {
          fltr[key] = this.filter[key];
        }
      }
      fltr = this.filterService.resolveFilter(fltr, null, null, null);
      this.dataService.aggregateObjects(this.config.objectname, fltr, null, [fac.attribute], [{function:"count", name:"count"}], null, 0, 500).subscribe(list => {
        fac.options = list.map(agg => {return {
          name: agg.getDimension(fac.attribute + "." + fac.displayAttribute), 
          value: agg.getDimension(fac.attribute),
          count:agg.getMetric("count")
        }});
        fac.options.sort((a, b) => ValueComparator.valueCompare(a, b, 'name'));
      });
    }    
  }

  clickOk() {
    if(this.filterConstructs.length > 0) {
      this.filter = {};
      for(let fic of this.filterConstructs) {
        this.filter[fic.config.attribute] = fic.getFilterValue();
      }  
    } else {
      this.filter = null;
    }
    if(this.sortConstructs.length > 0) {
      this.sort = {};
      for(let sic of this.sortConstructs) {
        this.sort[sic.order] = sic.getSortValue();
      }  
    } else {
      this.sort = null;
    }
    this.done.emit({filter: this.filter, sort: this.sort});
  }

  clickClear() {
    this.done.emit({filter: null, sort: null});
  }

  clickCancel() {
    this.overlayRef.dispose();
  }

  dateOptionComparison(option: any, value: any) : boolean  {
    return JSON.stringify(option) == JSON.stringify(value);
  }


}
