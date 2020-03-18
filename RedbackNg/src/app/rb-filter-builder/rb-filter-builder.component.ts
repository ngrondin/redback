import { Component, OnInit, Output, EventEmitter, Inject } from '@angular/core';
import { CONTAINER_DATA } from 'app/tokens';
import { DateTimePopupConfig } from 'app/rb-popup-datetime/rb-popup-datetime.component';
import { OverlayRef, validateHorizontalPosition } from '@angular/cdk/overlay';
import { MatSelect } from '@angular/material/select';

export class FilterBuilderConfig {
  filterConfig: any;
  initialFilter: any;
  sortConfig: any;
  initialSort: any;
}


export class FilterConfig {
  public attributes: FilterAttributeConfig[];

  constructor(json: any) {
    this.attributes = [];
    if(typeof json.attributes != 'undefined') {
      for(let ac of json.attributes) {
        this.attributes.push(new FilterAttributeConfig(ac));
      }
    }
  }
  
  getAttributeConfig(name: string) : FilterAttributeConfig {
    for(let a of this.attributes) {
      if(a.attribute == name) {
        return a;
      }
    }
    return null;
  }
}


export class FilterAttributeConfig {
  public attribute: string;
  public label: string;
  public type: string;

  constructor(json: any) {
    this.attribute = json.attribute;
    this.label = json.label;
    this.type = json.type;
  }
}


export class FilterItemConstruct {
  public config: FilterAttributeConfig;
  public val1: string;
  public val2: string;
  public val3: string;

  constructor(c: FilterAttributeConfig, v: any) {
    this.config = c;
    if(v != null) {
      if(this.config.type == 'string') {
        if(v != null && v.startsWith("'*") && v.endsWith("*'")) {
          this.val1 = v.substring(2, v.length - 2);; 
        } else {
          this.val1 = v;
        }
      } else if(this.config.type == 'date') {
        if(typeof v == 'object') {
          if(v.hasOwnProperty('$gt') && v.hasOwnProperty('$lt')) {
            this.val1 = 'between';
            let s2 = v['$gt'];
            let s3 = v['$gt'];
            this.val2 = s2.substring(1, s2.length - 1);
            this.val3 = s3.substring(1, s3.length - 1);
          } else if(v.hasOwnProperty('$gt')) {
            let s = v['$gt'];
            if(s == "(new Date((new Date()).getTime() - 900000)).toISOString()") {
              this.val1 = 'last15';
            } else if(s == "(new Date((new Date()).getTime() - 3600000)).toISOString()") {
              this.val1 = 'lasthour';
            } else if(s == "(new Date((new Date()).getTime() - 86400000)).toISOString()") {
              this.val1 = 'lastday';
            } else {
              this.val1 = 'since';
              this.val2 = s.substring(1, s.length - 1);
            }
          }
        }
      }
    }
  }

  public getFilterValue() : any {
    if(this.config.type == 'string') {
      return "'*" + this.val1 + "*'"; 
    } else if(this.config.type == 'date') {
      if(this.val1 == 'last15') {
        return {"$gt":"(new Date((new Date()).getTime() - 900000)).toISOString()"};
      } else if(this.val1 == 'lasthour') {
        return {"$gt":"(new Date((new Date()).getTime() - 3600000)).toISOString()"};
      } else if(this.val1 == 'lastday') {
        return {"$gt":"(new Date((new Date()).getTime() - 86400000)).toISOString()"};
      } else if(this.val1 == 'since') {
        return {"$gt":"'" + this.val2 + "'"};
      } else if(this.val1 == 'between') {
        return {"$gt":"'" + this.val2 + "'", "$lt":"'" + this.val3 + "'"};
      }
    }
  }
}




export class SortConfig {
  public attributes: SortAttributeConfig[];

  constructor(json: any) {
    this.attributes = [];
    if(typeof json.attributes != 'undefined') {
      for(let ac of json.attributes) {
        this.attributes.push(new SortAttributeConfig(ac));
      }
    }
  }
  
  getAttributeConfig(name: string) : SortAttributeConfig {
    for(let a of this.attributes) {
      if(a.attribute == name) {
        return a;
      }
    }
    return null;
  }
}


export class SortAttributeConfig {
  public attribute: string;
  public label: string;
  public type: string;

  constructor(json: any) {
    this.attribute = json.attribute;
    this.label = json.label;
    this.type = json.type;
  }
}


export class SortItemConstruct {
  public config: SortAttributeConfig;
  public direction: number;
  public order: number;

  constructor(c: SortAttributeConfig, v: any) {
    this.config = c;
    if(v != null) {
      this.direction = v.dir;
      this.order = v.order;
    } 
  }

  public getSortValue() : any {
    return {dir: this.direction, order: this.order};
  }
}



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

  datechoice: any = [
    { value: "last15", display: "Last 15 Minutes"},
    { value: "lasthour", display: "Last Hour"},
    { value: "lastday", display: "Last Day"},
    { value: "since", display: "Since"},
    { value: "between", display: "Between"}
  ]

  constructor(
    @Inject(CONTAINER_DATA) public config: FilterBuilderConfig, 
    public overlayRef: OverlayRef,
  ) { 
    this.filter = this.config.initialFilter;
    if(this.config.filterConfig != null) {
      this.filterConfig = new FilterConfig(this.config.filterConfig);
    }
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
        let fic = new FilterItemConstruct(this.filterConfig.getAttributeConfig(key), this.filter[key]);
        this.filterConstructs.push(fic);
      }
    }

    if(this.sort == null) {
      this.sort = {};
    } else {
      for(let key in this.sort) {
        let sic = new SortItemConstruct(this.sortConfig.getAttributeConfig(key), this.sort[key]);
        this.sortConstructs.push(sic);
      }
    }

  }

  addAttributeToFilter(event: any) {
    let fac: FilterAttributeConfig = event.value;
    let src: MatSelect = event.source;
    let fic = new FilterItemConstruct(fac, null);
    this.filterConstructs.push(fic);
    src.value = null;
  }

  addAttributeToSort(event: any) {
    let sac: SortAttributeConfig = event.value;
    let src: MatSelect = event.source;
    let sic = new SortItemConstruct(sac, {order:this.sortConstructs.length, dir:1});
    this.sortConstructs.push(sic);
    src.value = null;
  }

  removeFilterItem(fic: FilterItemConstruct) {
    this.filterConstructs.splice(this.filterConstructs.indexOf(fic), 1);
  }

  removeSortItem(sic: SortItemConstruct) {
    this.sortConstructs.splice(this.sortConstructs.indexOf(sic), 1);
  }

  toggleDir(sic: SortItemConstruct) {
    sic.direction = (-1 * sic.direction );
  }

  clickOk() {
    this.filter = {};
    for(let fic of this.filterConstructs) {
      this.filter[fic.config.attribute] = fic.getFilterValue();
    }
    this.sort = {};
    for(let sic of this.sortConstructs) {
      this.sort[sic.config.attribute] = sic.getSortValue();
    }
    this.done.emit({filter: this.filter, sort: this.sort});
  }

  dateOptionComparison(option: any, value: any) : boolean  {
    return JSON.stringify(option) == JSON.stringify(value);
  }
}
