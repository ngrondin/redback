import { FilterAttributeConfig, SortAttributeConfig } from "./rb-filter-builder-configs";

const dateCriteriaStartMinus = "(new Date((new Date()).getTime() - ";
const dateCriteriaStartPlus = "(new Date((new Date()).getTime() + ";
const dateCriteriaEnd = ")).toISOString()";
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
          let gt = 0, lt = 0;
          if(v.hasOwnProperty('$gt')) {
            if(this.isRelativeDate(v['$gt'])) {
              this.val2 = this.getValueOfRelativeDate(v['$gt']);
              gt = 2;
            } else {
              this.val2 = v['$gt'].substring(1, v['$gt'].length - 1);
              gt = 1;
            }
          }
          if(v.hasOwnProperty('$lt')) {
            if(this.isRelativeDate(v['$lt'])) {
              this.val3 = this.getValueOfRelativeDate(v['$lt']);
              lt = 2;
            } else {
              this.val3 = v['$lt'].substring(1, v['$lt'].length - 1);
              lt = 1;
            }
          }
          if(gt > 0 && lt > 0) {
            if(gt == 2 && lt == 2) {
              this.val1 = 'rollwindow';
            } else if(gt == 1 && lt == 1) {
              this.val1 = 'between';
            }
          } else if(gt == 2) {
            if(this.val2 == "3600000") {
              this.val1 = 'lasthour';
            } else if(this.val2 == "86400000") {
              this.val1 = 'lastday';
            } else {
              this.val1 = 'sincelast';
            }
          } else if(gt == 1) {
            this.val1 = 'since';
          } else if(lt == 2) {
            if(this.val2 == "3600000") {
              this.val1 = 'nexthour';
            } else if(this.val2 == "86400000") {
              this.val1 = 'nextday';
            } else {
              this.val1 = 'untilnext';
            }
          } else if(lt == 1) {
            this.val1 = 'until';
          }
        }
      } else if(this.config.type == 'multiselect') {
        if(v != null && typeof v == 'object' && v.hasOwnProperty("$in")) {
          this.val1 = v["$in"].map(item => item == "null" ? null : item.substring(1, item.length - 1));
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
        if(this.val1 == 'lasthour') {
          return {"$gt": dateCriteriaStartMinus + 3600000 + dateCriteriaEnd};
        } else if(this.val1 == 'lastday') {
          return {"$gt": dateCriteriaStartMinus + 86400000 + dateCriteriaEnd};
        } else if(this.val1 == 'sincelast') {
          return {"$gt": dateCriteriaStartMinus + this.val2 + dateCriteriaEnd};
        } else if(this.val1 == 'since') {
          return {"$gt":"'" + this.val2 + "'"};
        } else if(this.val1 == 'nexthour') {
          return {"$lt": dateCriteriaStartPlus + 3600000 + dateCriteriaEnd};
        } else if(this.val1 == 'nextday') {
          return {"$lt": dateCriteriaStartPlus + 86400000 + dateCriteriaEnd};
        } else if(this.val1 == 'untilnext') {
          return {"$lt": dateCriteriaStartPlus + this.val3 + dateCriteriaEnd};
        } else if(this.val1 == 'until') {
          return {"$lt":"'" + this.val3 + "'"};
        } else if(this.val1 == 'between') {
          return {"$gt":"'" + this.val2 + "'", "$lt":"'" + this.val3 + "'"};
        } else if(this.val1 == 'rollwindow') {
          return {"$gt": dateCriteriaStartMinus + this.val2 + dateCriteriaEnd, "$lt": dateCriteriaStartPlus + this.val3 + dateCriteriaEnd};
        }
      } else if(this.config.type == 'multiselect') {
        return {"$in": this.val1.map(item => item != null ? "'" + item + "'" : "null")};
      } else if(this.config.type == 'switch') {
        return this.val1 == true ? true : false;
      }
    }

    private isRelativeDate(str: string): boolean {
      return (str.startsWith(dateCriteriaStartPlus) || str.startsWith(dateCriteriaStartMinus)) && str.endsWith(dateCriteriaEnd);
    }

    private getValueOfRelativeDate(str: string) : string {
      let part = str.substring(dateCriteriaStartMinus.length, str.length - dateCriteriaEnd.length);
      return part.trim();
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
  

  export class SavedEntry {
    public name: string;
    public filter: any;
    public sort: any;
    public default: boolean;

    constructor(n: string, f: any, s: any, d: boolean) {
      this.name = n;
      this.filter = f;
      this.sort = s;
      this.default = d;
    }
  }