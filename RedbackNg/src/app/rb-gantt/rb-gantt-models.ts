import { SeriesConfig } from "app/abstract/rb-datacalc";
import { RbObject } from "app/datamodel";
import { ColorConfig, Evaluator, LinkConfig, VAEConfig } from "app/helpers";
import { RbDatasetComponent } from "app/rb-dataset/rb-dataset.component";

//export const GanttLaneHeight: number = 2.47; //VW, was in PX 42;
//export const GanttSpreadHeight: number = 1.64; //VW   
//export const GanttSpreadMargin: number = 0.41; //VW   

  export class GanttLaneConfig {
    dataset: string;
    linkAttributes: string[];
    labelAttribute: string;
    labelExpression: string;
    subAttribute: string;
    imageAttribute: string;
    iconAttribute: string;
    iconMap: any;
    modal: string;
    link: LinkConfig;
    dragfilter: any;
  
    constructor(json: any, userpref: any) {
      this.dataset = json.dataset;
      this.linkAttributes = json.linkattributes ?? ["uid"];
      this.labelAttribute = json.labelattribute;
      this.labelExpression = json.labelexpression;
      this.subAttribute = json.subattribute;
      this.imageAttribute = json.imageattribute;
      this.iconAttribute = json.iconattribute;
      this.iconMap = json.iconmap;
      this.modal = json.modal;
      this.link = json.link != null ? new LinkConfig(json.link) : null;
      this.dragfilter = json.dragfilter;
    }
  }

  export abstract class GanttTimeBasedConfig extends SeriesConfig {
    start: VAEConfig;
    duration: VAEConfig;
    end: VAEConfig;

    constructor(json: any) {
      super(json);
      this.start = json.startattribute != null ? new VAEConfig({attribute: json.startattribute}) : new VAEConfig(json.start);
      this.duration = json.durationattribute  != null ? new VAEConfig({attribute: json.durationattribute}) : json.duration != null ? new VAEConfig(json.duration) : null;
      this.end = json.endattribute  != null ? new VAEConfig({attribute: json.endattribute}) : json.end != null ? new VAEConfig(json.end) : null;
    }

    get timeFromAttributes() : boolean {
      return this.start != null && this.start.attribute != null && ((this.duration != null && this.duration.attribute != null) || (this.end != null && this.end.attribute != null));
    }
  }
  
  export class GanttSeriesConfig extends GanttTimeBasedConfig {
    laneAttributes: string[];
    labelAttribute: string;
    labelExpression: string;
    labelAlts: any[];
    centerLabel: boolean;
    labelColor: string;
    color: ColorConfig;
    indicatorAttribute: string;
    indicatorExpression: string;    
    isBackground: boolean;
    canEdit: boolean;
    isGhost: boolean;
    modal: string;
    link: LinkConfig;
    applyLaneFilter: boolean;
    applyDateFilter: boolean;
    show: Function;
  
    constructor(json: any, userpref: any) {
      super(json);
      let subpref = userpref != null && userpref.series != null ? userpref.series[json.dataset] : null;
      this.laneAttributes = json.laneattribute != null ? [json.laneattribute] : json.laneattributes != null ? json.laneattributes : null;
      this.labelAlts = json.labelalts;
      this.labelAttribute = subpref != null && subpref.labelattribute != null ? subpref.labelattribute : json.labelattribute;
      this.labelExpression = json.labelexpression;
      this.centerLabel = json.centerlabel ?? false;
      this.labelColor = json.labelcolor;
      this.isBackground = json.isbackground ?? false;
      this.canEdit = json.canedit ?? true;
      this.isGhost = json.ghost ?? false;
      if(json.color != null && typeof json.color === 'object') {
        this.color = new ColorConfig(json.color);
      } else if(json.color != null || json.colorattribute != null || json.colormap != null || json.colorexpression != null) {
        this.color = new ColorConfig({value: json.color, attribute: json.colorattribute, map: json.colormap, expression: json.colorexpression});
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
  
  export class GanttOverlayConfig extends GanttTimeBasedConfig {
    label: string;
    labelAttribute: string;
    color: ColorConfig;
    applyDateFilter: boolean;
  
    constructor(json: any, userpref: any) {
      super(json);
      let subpref = userpref != null && userpref.series != null ? userpref.series[json.dataset] : null;
      this.dataset = json.dataset;
      this.label = json.label;
      this.labelAttribute = subpref != null && subpref.labelattribute != null ? subpref.labelattribute : json.labelattribute;
      if(json.color != null && typeof json.color === 'object') {
        this.color = new ColorConfig(json.color);
      } else if(json.color != null || json.colorattribute != null || json.colormap != null || json.colorexpression != null) {
        this.color = new ColorConfig({color: json.color, attribute: json.colorattribute, map: json.colormap, expression: json.colorexpression});
      }
      this.applyDateFilter = json.applydatefilter != null ? json.applydatefilter : true;
    }
  }

  export class GanttLane {
    linkValues: string[];
    label: string;
    sub: string;
    image: string;
    icon: string;
    unitHeight: number;
    height: number;
    spreads: GanttSpread[];
    object: RbObject;
    config: GanttLaneConfig;

    constructor(obj: RbObject, cfg: GanttLaneConfig, uh: number) {
      this.object = obj;
      this.config = cfg;
      this.unitHeight = uh;
      this.height = this.unitHeight;
      //let label = null;
      if(cfg.labelAttribute != null) {
        this.label = obj.get(cfg.labelAttribute); 
      } else if(cfg.labelExpression != null) {
        this.label = Evaluator.eval(cfg.labelExpression, obj, null, null);
      }
      if(this.config.iconAttribute != null) {
        this.icon = obj.get(this.config.iconAttribute);
        if(this.config.iconMap != null) {
          this.icon = this.config.iconMap[this.icon];
        }  
      }
      if(this.config.imageAttribute != null) {
        let fileVal = obj.get(this.config.imageAttribute);
        if(fileVal != null && fileVal.thumbnail != null) {
          this.image = "url(\'" + fileVal.thumbnail + "\')"
        }
      }
      if(this.config.subAttribute != null) {
        this.sub = obj.get(this.config.subAttribute);
      }   
      this.linkValues = this.config.linkAttributes.map(la => this.object.get(la));
    }
  
    setSpreads(s: GanttSpread[]) {
      this.spreads = s;
      let max: number = this.image != null ? 1 : 0;
      for(let i = 0; i < this.spreads.length; i++) {
        if(this.spreads[i].sublane > max) {
          max = this.spreads[i].sublane;
        }
      }
      this.height = (max + 1) * this.unitHeight;
      //this.height = ((max + 1) * (GanttSpreadHeight + GanttSpreadMargin)) + GanttSpreadMargin;
    }

    backgroundSpreads() {
      return this.spreads.filter(s => s.config.isBackground == true);
    }

    foregroundSpreads() {
      return this.spreads.filter(s => s.config.isBackground == false/* && s.dragging == false*/);
    }
  }
  
  export class GanttSpread {
    id: string; //currently only used for groupings
    label: string;
    start: number;
    end: number;
    width: number;
    height: number;
    margin: number;
    offsetTop: number;
    laneTop: number;
    sublane: number;
    color: string;
    labelcolor: string;
    canEdit: boolean;
    indicator: boolean;
    dragging: boolean;
    tip: string;
    object: RbObject;
    dataset: RbDatasetComponent;
    config: GanttSeriesConfig;
  
    constructor(l: string, s: number, w: number, h: number, m: number, ost: number, sl: number, c: string, lc: string, o: RbObject, ds: RbDatasetComponent, cfg: GanttSeriesConfig) {
      this.label = l;
      this.start = s;
      this.width = w;
      this.end = s + w;
      this.height = h;
      this.margin = m;
      this.offsetTop = ost;
      this.sublane = sl;
      this.laneTop = (this.sublane * h) + (cfg.isBackground == false ? (this.sublane + 1) * m : 0);
      this.color = c;
      this.labelcolor = lc;
      this.canEdit = o != null ? cfg.canEdit && (cfg.start.attribute != null && o.canEdit(cfg.start.attribute) || cfg.laneAttributes.reduce((acc, la) => acc && o.canEdit(la), true)) : false;
      this.indicator = false;
      this.dragging = false;
      this.tip = null;
      this.object = o;
      this.dataset = ds;
      this.config = cfg;
    }

    get laneValues(): string[] {
      return this.config.laneAttributes.map(la => this.object.get(la));
    }

    get ghost(): boolean {
      return this.config.isGhost;
    }

    get selected(): boolean {
      return this.config.isBackground == false && this.dataset.isObjectSelected(this.object);
    }
  }

  export class GanttOverlayLane {
    id: string;
    label: string;
    height: number;
    spreads: GanttOverlaySpread[];
  
    constructor(i: string, l: string, h: number) {
      this.id = i;
      this.label = l != null ? l : "";
      this.height = h;
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
    Day, Major, Minor, Now
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