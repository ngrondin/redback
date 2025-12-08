import { SeriesConfig } from "app/abstract/rb-datacalc";
import { RbObject } from "app/datamodel";
import { ColorConfig, LinkConfig } from "app/helpers";

//export const GanttLaneHeight: number = 2.47; //VW, was in PX 42;
export const GanttSpreadHeight: number = 1.64; //VW   
export const GanttSpreadMargin: number = 0.41; //VW   

export class GanttLaneConfig {
    dataset: string;
    labelAttribute: string;
    subAttribute: string;
    imageAttribute: string;
    iconAttribute: string;
    iconMap: any;
    modal: string;
    link: LinkConfig;
    dragfilter: any;
  
    constructor(json: any, userpref: any) {
      this.dataset = json.dataset;
      this.labelAttribute = json.labelattribute;
      this.subAttribute = json.subattribute;
      this.imageAttribute = json.imageattribute;
      this.iconAttribute = json.iconattribute;
      this.iconMap = json.iconmap;
      this.modal = json.modal;
      this.link = json.link != null ? new LinkConfig(json.link) : null;
      this.dragfilter = json.dragfilter;
    }
  }
  
  export class GanttSeriesConfig extends SeriesConfig {
    dataset: string;
    startAttribute: string;
    durationAttribute: string;
    endAttribute: string;
    laneAttribute: string;
    labelAttribute: string;
    labelExpression: string;
    labelAlts: any[];
    centerLabel: boolean;
    labelColor: string;
    color: ColorConfig;
    colorAttribute: string;
    colorMap: any;
    colorExpression: string;
    indicatorAttribute: string;
    indicatorExpression: string;    
    isBackground: boolean;
    modal: string;
    link: LinkConfig;
    canEdit: boolean;
    applyLaneFilter: boolean;
    applyDateFilter: boolean;
    show: Function;
  
    constructor(json: any, userpref: any) {
      super(json);
      let subpref = userpref != null && userpref.series != null ? userpref.series[json.dataset] : null;
      this.startAttribute = json.startattribute;
      this.durationAttribute = json.durationattribute;
      this.endAttribute = json.endattribute;
      this.laneAttribute = json.laneattribute;
      this.labelAlts = json.labelalts;
      this.labelAttribute = subpref != null && subpref.labelattribute != null ? subpref.labelattribute : json.labelattribute;
      this.labelExpression = json.labelexpression;
      this.centerLabel = json.centerlabel ?? false;
      this.labelColor = json.labelcolor;
      this.isBackground = json.isbackground;
      this.canEdit = json.canedit;
      if(json.color != null && typeof json.color === 'object') {
        this.color = new ColorConfig(json.color);
      } else if(json.color != null || json.colorattribute != null || json.colormap != null || json.colorexpression != null) {
        this.color = new ColorConfig({color: json.color, attribute: json.colorattribute, map: json.colormap, expression: json.colorexpression});
      }
      this.indicatorAttribute = json.indicatorattribute;
      this.indicatorExpression = json.indicatorexpression;
      this.modal = json.modal;
      this.link = json.link != null ? new LinkConfig(json.link) : null;
      this.applyLaneFilter = json.applylanefilter != null ? json.applylanefilter : true;
      this.applyDateFilter = json.applydatefilter != null ? json.applydatefilter : true;
      this.show = json.show != null ? Function("dataset", "relatedObject", "return (" + json.show + ")") : null;
    }
  }
  
  export class GanttOverlayConfig {
    dataset: string;
    startAttribute: string;
    durationAttribute: string;
    endAttribute: string;
    label: string;
    labelAttribute: string;
    color: ColorConfig;
  
    constructor(json: any, userpref: any) {
      let subpref = userpref != null && userpref.series != null ? userpref.series[json.dataset] : null;
      this.dataset = json.dataset;
      this.startAttribute = json.startattribute;
      this.durationAttribute = json.durationattribute;
      this.endAttribute = json.endattribute;
      this.label = json.label;
      this.labelAttribute = subpref != null && subpref.labelattribute != null ? subpref.labelattribute : json.labelattribute;
      if(json.color != null && typeof json.color === 'object') {
        this.color = new ColorConfig(json.color);
      } else if(json.color != null || json.colorattribute != null || json.colormap != null || json.colorexpression != null) {
        this.color = new ColorConfig({color: json.color, attribute: json.colorattribute, map: json.colormap, expression: json.colorexpression});
      }
    }
  }

  export class GanttLane {
    id: string;
    label: string;
    sub: string;
    image: string;
    icon: string;
    height: number;
    spreads: GanttSpread[];
    object: RbObject;
  
    constructor(i: string, l: string, s: string, im: string, ic: string, o: RbObject) {
      this.id = i;
      this.label = l != null ? l : "";
      this.sub = s;
      this.image = im;
      this.icon = ic;
      this.object = o;
      this.height = GanttSpreadHeight + (2*GanttSpreadMargin);
    }
  
    setSpreads(s: GanttSpread[]) {
      this.spreads = s;
      let max: number = this.image != null ? 1 : 0;
      for(let i = 0; i < this.spreads.length; i++) {
        if(this.spreads[i].sublane > max) {
          max = this.spreads[i].sublane;
        }
      }
      this.height = ((max + 1) * (GanttSpreadHeight + GanttSpreadMargin)) + GanttSpreadMargin;
    }

    backgroundSpreads() {
      return this.spreads.filter(s => s.config.isBackground == true);
    }

    foregroundSpreads() {
      return this.spreads.filter(s => s.config.isBackground == false && s.dragging == false);
    }
  }
  
  export class GanttSpread {
    id: string; //currently only used for groupings
    lane: string;
    label: string;
    start: number;
    width: number;
    offsetTop: number;
    laneTop: number;
    height: number;
    sublane: number;
    color: string;
    labelcolor: string;
    canEdit: boolean;
    selected: boolean;
    indicator: boolean;
    dragging: boolean;
    object: RbObject;
    config: GanttSeriesConfig;
  
    constructor(ln: string, l: string, s: number, w: number, ost: number, sl: number, c: string, lc: string, ce: boolean, sel: boolean, ind: boolean, drg: boolean, o: RbObject, cfg: GanttSeriesConfig) {
      this.lane = ln;
      this.label = l;
      this.start = s;
      this.width = w;
      this.offsetTop = ost;
      this.sublane = sl;
      this.laneTop = GanttSpreadMargin + (this.sublane * (GanttSpreadHeight + GanttSpreadMargin));
      this.color = c;
      this.labelcolor = lc;
      this.canEdit = ce;
      this.selected = sel;
      this.indicator = ind;
      this.dragging = drg;
      this.object = o;
      this.config = cfg;
      this.height = GanttSpreadHeight;
    }
  }

  export class GanttOverlayLane {
    id: string;
    label: string;
    height: number;
    spreads: GanttOverlaySpread[];
  
    constructor(i: string, l: string) {
      this.id = i;
      this.label = l != null ? l : "";
      this.height = GanttSpreadHeight;
    }

    setSpreads(s: GanttOverlaySpread[]) {
      this.spreads = s;
    }
  }

  export class GanttOverlaySpread {
    id: string;
    start: number;
    width: number;
    color: string;
    object: RbObject;
    config: GanttOverlayConfig;
  
    constructor(i: string, s: number, w: number, c: string, o: RbObject, cfg: GanttOverlayConfig) {
      this.id = i;
      this.start = s;
      this.width = w;
      this.color = c;
      this.object = o;
      this.config = cfg;
    }
  }

  export enum GanttMarkType {
    Day, Major, Minor
  }
  export class GanttMark {
    px: number;
    dayLabel: string;
    timeLabel: string;
    type: GanttMarkType;
  
    constructor(p: number, dl: string, tl: string, t: GanttMarkType) {
      this.px = p;
      this.dayLabel = dl;
      this.timeLabel = tl;
      this.type = t;
    }
  }