import { RbSearchTarget } from "app/rb-search/rb-search-target";

  export class FilterBuilderConfig {
    filterConfig: any;
    sortConfig: any;
    searchTarget: RbSearchTarget
  }
  
  
  export class FilterConfig {
    public attributes: FilterAttributeConfig[];
    public useBaseFilter: boolean;
  
    constructor(json: any) {
      this.attributes = [];
      if(typeof json.attributes != 'undefined') {
        for(let ac of json.attributes) {
          this.attributes.push(new FilterAttributeConfig(ac));
        }
      }
      this.useBaseFilter = json.usebasefilter
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
    public objectName: string;
    public valueAttribute: string;
    public filter: any;
    public options: any[] = [];
  
    constructor(json: any) {
      this.attribute = json.attribute;
      this.label = json.label;
      this.type = json.type;
      this.displayAttribute = json.displayattribute;
      this.objectName = json.object;
      this.valueAttribute = json.valueattribute;
      this.filter = json.filter;
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
  