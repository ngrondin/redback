import { SeriesConfig } from "app/abstract/rb-datacalc";
import { RbObject } from "app/datamodel";
import { LinkConfig } from "app/helpers";

export class TimelineSeriesConfig extends SeriesConfig {
    mainAttribute: string;
    mainExpression: Function;
    subAttribute: string;
    subExpression: Function;
    dateAttribute: string;
    icon: string;
    level: number;
    link: LinkConfig;
    modal: string;
  
    constructor(json: any) {
        super(json);
        this.mainAttribute = json.mainattribute;
        this.mainExpression = json.mainexpression != null ? Function("object", "return (" + json.mainexpression + ");") : null;
        this.subAttribute = json.subattribute;
        this.subExpression = json.subexpression != null ? Function("object", "return (" + json.subexpression + ");") : null;
        this.dateAttribute = json.dateattribute;
        this.icon = json.icon;
        this.level = json.level || 0;
        this.link = json.link != null ? new LinkConfig(json.link) : null;
        this.modal = json.modal;
    }
  }

  export class TimelineEntry {
      object: RbObject;
      date: Date;
      main: string;
      sub: string;
      icon: string;
      level: number;
      link: LinkConfig;
      modal: string;
      showDatePart: boolean = true;
      showTimePart: boolean = true;
      showTopLine: boolean = true;
      showBottomLine: boolean = true;

      constructor(o: RbObject, l: number, d: Date, m: string, s?: string, i?: string, lk?: LinkConfig, md?: string) {
          this.object = o;
          this.date = d;
          this.main = m;
          this.sub = s;
          this.icon = i;
          this.level = l;
          this.link = lk;
          this.modal = md;
      }

      get isClickable(): boolean {
        return this.link != null || this.modal != null;
      }
  }