import { SeriesConfig } from "app/abstract/rb-datacalc";
import { RbObject } from "app/datamodel";

export class TimelineSeriesConfig extends SeriesConfig {
    mainAttribute: string;
    mainExpression: Function;
    subAttribute: string;
    subExpression: Function;
    dateAttribute: string;
    level: number;
  
    constructor(json: any) {
        super(json);
        this.mainAttribute = json.mainattribute;
        this.mainExpression = json.mainexpression != null ? Function("object", "return (" + json.mainexpression + ");") : null;
        this.subAttribute = json.subattribute;
        this.subExpression = json.subexpression != null ? Function("object", "return (" + json.subexpression + ");") : null;
        this.dateAttribute = json.dateattribute;
        this.level = json.level || 0;
    }
  }

  export class TimelineEntry {
      date: Date;
      main: string;
      sub: string;
      level: number;
      showDatePart: boolean = true;
      showTimePart: boolean = true;
      showTopLine: boolean = true;
      showBottomLine: boolean = true;

      constructor(d: Date, m: string, s: string, l: number) {
          this.date = d;
          this.main = m;
          this.sub = s;
          this.level = l;
      }
  }