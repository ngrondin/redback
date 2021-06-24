import { SeriesConfig } from "app/abstract/rb-datacalc";

export class TimelineSeriesConfig extends SeriesConfig {
    mainAttribute: string;
    subAttribute: string;
    dateAttribute: string;
  
    constructor(json: any) {
        super(json);
        this.mainAttribute = json.mainattribute;
        this.subAttribute = json.subattribute;
        this.dateAttribute = json.dateattribute;
    }
  }

  export class TimelineEntry {
      date: Date;
      main: string;
      sub: string;
      showDatePart: boolean = true;
      showTopLine: boolean = true;
      showBottomLine: boolean = true;

      constructor(d: Date, m: string, s: string) {
          this.date = d;
          this.main = m;
          this.sub = s;
      }
  }