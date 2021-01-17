import { Component, OnInit, Output, EventEmitter, Inject } from '@angular/core';
import { CONTAINER_DATA } from 'app/tokens';
import { DateTimePopupConfig } from 'app/popups/rb-popup-datetime/rb-popup-datetime.component';
import { OverlayRef, validateHorizontalPosition } from '@angular/cdk/overlay';
import { MatSelect } from '@angular/material/select';
import { DataService } from 'app/services/data.service';
import { RbAggregate } from 'app/datamodel';
import { MapService } from 'app/services/map.service';
import { ValueComparator } from 'app/helpers';

export class FilterBuilderConfig {
  filterConfig: any;
  initialFilter: any;
  sortConfig: any;
  initialSort: any;
  objectname: string;
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
  public displayAttribute: string;
  public options: any[] = [];

  constructor(json: any) {
    this.attribute = json.attribute;
    this.label = json.label;
    this.type = json.type;
    this.displayAttribute = json.displayattribute;
  }
}


export class FilterItemConstruct {
  public config: FilterAttributeConfig;
  public val1: any;
  public val2: string;
  public val3: string;

  constructor(c: FilterAttributeConfig, v: any) {
    this.config = c;
    if(this.config.type == 'string') {
      if(v != null) {
        if(v == 'null') {
          this.val1 = null;
        } else if(v.startsWith("'*") && v.endsWith("*'")) {
          this.val1 = v.substring(2, v.length - 2);
        } else {
          this.val1 = v;
        }
      }
    } else if(this.config.type == 'date') {
      if(v != null && typeof v == 'object') {
        if(v.hasOwnProperty('$gt') && v.hasOwnProperty('$lt')) {
          this.val1 = 'between';
          let s2 = v['$gt'];
          let s3 = v['$lt'];
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
        } else if(v.hasOwnProperty('$lt')) {
          let s = v['$lt'];
          if(s == "(new Date((new Date()).getTime() + 900000)).toISOString()") {
            this.val1 = 'next15';
          } else if(s == "(new Date((new Date()).getTime() + 3600000)).toISOString()") {
            this.val1 = 'nexthour';
          } else if(s == "(new Date((new Date()).getTime() + 86400000)).toISOString()") {
            this.val1 = 'nextday';
          } else {
            this.val1 = 'until';
            this.val2 = s.substring(1, s.length - 1);
          }
        }
      }
    } else if(this.config.type == 'multiselect') {
      if(v != null && typeof v == 'object' && v.hasOwnProperty("$in")) {
        this.val1 = v["$in"].map(item => item.substring(1, item.length - 1));
      } else {
        this.val1 = [];
      }
    }
  }

  public getFilterValue() : any {
    if(this.config.type == 'string') {
      if(this.val1 == null) {
        return "null";
      } else {
        return "'*" + this.val1 + "*'"; 
      }
    } else if(this.config.type == 'date') {
      if(this.val1 == 'last15') {
        return {"$gt":"(new Date((new Date()).getTime() - 900000)).toISOString()"};
      } else if(this.val1 == 'lasthour') {
        return {"$gt":"(new Date((new Date()).getTime() - 3600000)).toISOString()"};
      } else if(this.val1 == 'lastday') {
        return {"$gt":"(new Date((new Date()).getTime() - 86400000)).toISOString()"};
      } else if(this.val1 == 'since') {
        return {"$gt":"'" + this.val2 + "'"};
      } else if(this.val1 == 'next15') {
        return {"$lt":"(new Date((new Date()).getTime() + 900000)).toISOString()"};
      } else if(this.val1 == 'nexthour') {
        return {"$lt":"(new Date((new Date()).getTime() + 3600000)).toISOString()"};
      } else if(this.val1 == 'nextday') {
        return {"$lt":"(new Date((new Date()).getTime() + 86400000)).toISOString()"};
      } else if(this.val1 == 'until') {
        return {"$lt":"'" + this.val2 + "'"};
      } else if(this.val1 == 'between') {
        return {"$gt":"'" + this.val2 + "'", "$lt":"'" + this.val3 + "'"};
      }
    } else if(this.config.type == 'multiselect') {
      return {"$in": this.val1.map(item => "'" + item + "'")};
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

  constructor(json: any) {
    this.attribute = json.attribute;
    this.label = json.label;
  }
}


export class SortItemConstruct {
  public config: SortAttributeConfig;
  public direction: number;
  public order: number;

  constructor(c: SortAttributeConfig, o: number, v: any) {
    this.config = c;
    this.order = o;
    if(v != null) {
      this.direction = v.dir;
    } 
  }

  public getSortValue() : any {
    return {attribute: this.config.attribute, dir: this.direction};
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
    private mapService: MapService
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

  addAttributeToFilter(event: any) {
    let fac: FilterAttributeConfig = event.value;
    let src: MatSelect = event.source;
    let fic = new FilterItemConstruct(fac, null);
    this.filterConstructs.push(fic);
    src.value = null;
    this.getAggregatesFor(fac);
  }

  addAttributeToSort(event: any) {
    let sac: SortAttributeConfig = event.value;
    let src: MatSelect = event.source;
    let sic = new SortItemConstruct(sac, this.sortConstructs.length, {dir:1});
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

  getAggregatesFor(fac: FilterAttributeConfig) {
    if(fac.type == 'multiselect') {
      let fltr = {};
      for (const key in this.filter) {
        if(key != fac.attribute) {
          fltr[key] = this.filter[key];
        }
      }
      fltr = this.mapService.resolveMap(fltr, null, null, null);
      this.dataService.aggregateObjects(this.config.objectname, fltr, null, [fac.attribute], [{function:"count", name:"count"}]).subscribe(list => {
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
    this.filter = {};
    for(let fic of this.filterConstructs) {
      this.filter[fic.config.attribute] = fic.getFilterValue();
    }
    this.sort = {};
    for(let sic of this.sortConstructs) {
      this.sort[sic.order] = sic.getSortValue();
    }
    this.done.emit({filter: this.filter, sort: this.sort});
  }

  dateOptionComparison(option: any, value: any) : boolean  {
    return JSON.stringify(option) == JSON.stringify(value);
  }


}
