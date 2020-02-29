import { Component, OnInit, Output, EventEmitter, Inject } from '@angular/core';
import { CONTAINER_DATA } from 'app/tokens';
import { DateTimePopupConfig } from 'app/rb-popup-datetime/rb-popup-datetime.component';
import { OverlayRef, validateHorizontalPosition } from '@angular/cdk/overlay';

export class FilterBuilderConfig {
  filterConfig: any;
  initialFilter: any;
}


export class FilterConfig {
  public attributes: FilterAttributeConfig[];

  constructor(json: any) {
    this.attributes = [];
    for(let ac of json.attributes) {
      this.attributes.push(new FilterAttributeConfig(ac));
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




@Component({
  selector: 'rb-filter-builder',
  templateUrl: './rb-filter-builder.component.html',
  styleUrls: ['./rb-filter-builder.component.css']
})
export class RbFilterBuilderComponent implements OnInit {

  @Output() done: EventEmitter<any> = new EventEmitter();
  selectedAttribute: any;
  filterConfig: FilterConfig;
  filter: any;
  filterConstructs: FilterItemConstruct[];

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
    this.filterConfig = new FilterConfig(this.config.filterConfig);
  }

  ngOnInit() {
    this.filterConstructs = [];
    if(this.filter == null) {
      this.filter = {};
    } else {
      for(let key in this.filter) {
        let fic = new FilterItemConstruct(this.filterConfig.getAttributeConfig(key), this.filter[key]);
        this.filterConstructs.push(fic);
      }
    }
  }

  addSelectedAttribute() {
    let fic = new FilterItemConstruct(this.filterConfig.getAttributeConfig(this.selectedAttribute.attribute), null);
    this.filterConstructs.push(fic);
    this.selectedAttribute = null;
}

  removeFilterItem(fic: FilterItemConstruct) {
    this.filterConstructs.splice(this.filterConstructs.indexOf(fic), 1);
  }

  clickOk() {
    this.filter = {};
    for(let fic of this.filterConstructs) {
      this.filter[fic.config.attribute] = fic.getFilterValue();
    }
    this.done.emit(this.filter);
  }

  dateOptionComparison(option: any, value: any) : boolean  {
    return JSON.stringify(option) == JSON.stringify(value);
  }
}
