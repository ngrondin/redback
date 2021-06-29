import { RbDataCalcComponent, SeriesConfig } from "app/abstract/rb-datacalc";
import { RbObject } from "app/datamodel";

export class CalendarSeriesConfig extends SeriesConfig {
    dataset: string;
    dateAttribute: string;
    durationAttribute: string;
    labelAttribute: string;
    colorAttribute: string;
    colorMap: any;
    color: string;
    linkAttribute: string;
    linkView: string;
    modal: string;
    canEdit: boolean;
    active: boolean = true;
  
    constructor(json: any, userpref: any) {
      super(json);
      let subpref = userpref != null && userpref.series != null ? userpref.series[json.dataset] : null;
      this.dateAttribute = json.dateattribute;
      this.durationAttribute = json.durationattribute;
      this.labelAttribute = subpref != null && subpref.labelattribute != null ? subpref.labelattribute : json.labelattribute;
      this.colorAttribute = json.colorattribute;
      this.colorMap = json.colormap;
      this.color = json.color;
      this.linkAttribute = json.linkattribute;
      this.linkView = json.linkview;
      this.modal = json.modal;
    }
}
  
export class CalendarEntry {
    id: string;
    label: string;
    date: Date;
    color: string;
    object: RbObject;
    config: CalendarSeriesConfig;
  
    constructor(i: string, l: string, d: Date, c: string, o: RbObject, cfg: CalendarSeriesConfig) {
      this.id = i;
      this.label = l;
      this.date = d;
      this.color = c;
      this.object = o;
      this.config = cfg;
    }
  
}