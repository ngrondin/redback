import { FilterAttributeConfig, SortAttributeConfig } from "./rb-filter-builder-configs";

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
      } else if(this.config.type == 'switch') {
        this.val1 = v == true ? true : false;
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
      } else if(this.config.type == 'switch') {
        return this.val1 == true ? true : false;
      }
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
  