import { SeriesConfig } from "app/abstract/rb-datacalc";
import { RbObject, RELATED_LOADING } from "app/datamodel";
import { ColorConfig, ColorTool, Evaluator, LinkConfig, VAEConfig } from "app/helpers";
import { RbDatasetComponent } from "app/rb-dataset/rb-dataset.component";

  export class GanttLaneConfig {
    dataset: string;
    linkAttributes: string[];
    labelAttribute: string;
    labelExpression: string;
    subAttribute: string;
    imageAttribute: string;
    iconAttribute: string;
    iconMap: any;
    orderAttribute: string;
    modal: string;
    link?: LinkConfig;
    dragfilter: any;
    editable: boolean;

    constructor(json: any, userpref: any) {
      this.dataset = json.dataset;
      this.linkAttributes = json.linkattributes ?? ["uid"];
      this.labelAttribute = json.labelattribute;
      this.labelExpression = json.labelexpression;
      this.subAttribute = json.subattribute;
      this.imageAttribute = json.imageattribute;
      this.iconAttribute = json.iconattribute;
      this.iconMap = json.iconmap;
      this.orderAttribute = json.orderattribute;
      this.modal = json.modal;
      this.link = json.link != null ? new LinkConfig(json.link) : undefined;
      this.dragfilter = json.dragfilter;
      this.editable = json.editable ?? false;
    }
  }

  export abstract class GanttTimeBasedConfig extends SeriesConfig {
    start: VAEConfig;
    duration?: VAEConfig;
    end?: VAEConfig;

    constructor(json: any) {
      super(json);
      this.start = json.startattribute != null ? new VAEConfig({attribute: json.startattribute}) : new VAEConfig(json.start);
      this.duration = json.durationattribute  != null ? new VAEConfig({attribute: json.durationattribute}) : json.duration != null ? new VAEConfig(json.duration) : undefined;
      this.end = json.endattribute  != null ? new VAEConfig({attribute: json.endattribute}) : json.end != null ? new VAEConfig(json.end) : undefined;
    }

    get timeFromAttributes() : boolean {
      return this.start != null && this.start.attribute != null && ((this.duration != null && this.duration.attribute != null) || (this.end != null && this.end.attribute != null));
    }

    getObjectStartEndDur(obj: RbObject): [number, number, number] {
      let startMS: number = (new Date(this.start.getValue(obj))).getTime();
      let durationMS: number|null = null;
      let endMS: number|null = null;
      if(this.duration != null) {
          durationMS = parseInt(this.duration.getValue(obj));
          endMS = startMS + durationMS;
      } else if(this.end != null) {
          endMS = (new Date(this.end.getValue(obj))).getTime();
          durationMS = endMS - startMS;
      } else {
          durationMS = 3600000;
          endMS = startMS + durationMS;
      }
      if(durationMS == null || isNaN(durationMS)) durationMS = 3600000;
      return [startMS, endMS, durationMS];
    }
  }

export class GanttSeriesConfig extends GanttTimeBasedConfig {
    id: string | null;
    laneAttributes: string[];
    laneForeignAttributes: string[];
    labelAttribute: string | null;
    labelExpression: string | null;
    labelAlts: any[] | null;
    centerLabel: boolean;
    labelColor: string | null;
    color: ColorConfig | null;
    selectedBorderColor: string | null;
    indicatorAttribute: string | null;
    indicatorExpression: string | null;
    dependencyAttribute: string | null;
    groupOf: string | null;
    groupAttribute: string | null;
    isBackground: boolean;
    canEdit: boolean;
    isGhost: boolean;
    modal: string | null;
    link: LinkConfig | null;
    click: Function | null;
    applyLaneFilter: boolean;
    applyDateFilter: boolean;
    show: Function | null;

    constructor(json: any, userpref: any) {
      super(json);
      let subpref = userpref != null && userpref.series != null ? userpref.series[json.dataset] : ["uid"];
      this.id = json.id;
      this.laneAttributes = json.laneattribute != null ? [json.laneattribute] : json.laneattributes != null ? json.laneattributes : ["uid"];
      this.laneForeignAttributes = json.laneforeignattribute != null ? [json.laneforeignattribute] : json.laneforeignattributes != null ? json.laneforeignattributes : undefined;
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
      } else {
        this.color = null;
      }
      this.selectedBorderColor = json.selectedbordercolor;
      this.indicatorAttribute = json.indicatorattribute;
      this.indicatorExpression = json.indicatorexpression;
      this.dependencyAttribute = json.dependencyattribute;
      this.groupOf = json.groupof;
      this.groupAttribute = json.groupattribute;
      this.modal = json.modal;
      this.link = json.link != null ? new LinkConfig(json.link) : null;
      this.click = json.clickscript != null ? Function("event", json.clickscript) : null;
      this.applyLaneFilter = json.applylanefilter != null ? json.applylanefilter : true;
      this.applyDateFilter = json.applydatefilter != null ? json.applydatefilter : true;
      this.show = json.show != null ? Function("dataset", "relatedObject", "return (" + json.show + ")") : null;
    }
  }

  export class GanttOverlayConfig extends GanttTimeBasedConfig {
    label: string | null;
    labelAttribute: string | null;
    color: ColorConfig | null;
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
      } else {
        this.color = null;
      }
      this.applyDateFilter = json.applydatefilter != null ? json.applydatefilter : true;
    }
  }

  export class GanttLane {
    //linkValues: string[];
    label: string | null = null;
    sub: string | null = null;
    image: string | null = null;
    icon: string | null = null;
    spreadHeight: number;
    spreadMargin: number;
    height: number;
    spreads: GanttSpread[] = [];
    object: RbObject;
    config: GanttLaneConfig;

    constructor(obj: RbObject, cfg: GanttLaneConfig, sh: number, sm: number) {
      this.object = obj;
      this.config = cfg;
      this.spreadHeight = sh;
      this.spreadMargin = sm;
      this.height = sh + (2*sm);
      if(cfg.labelAttribute != null) {
        this.label = obj.get(cfg.labelAttribute);
      } else if(cfg.labelExpression != null) {
        this.label = Evaluator.eval(cfg.labelExpression, obj);
      }
      if(this.config.iconAttribute != null) {
        this.icon = obj.get(this.config.iconAttribute);
        if(this.config.iconMap != null && this.icon != null) {
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
      //this.linkValues = this.config.linkAttributes.map(la => this.object.get(la));
    }

    setSpreads(s: GanttSpread[]) {
      this.spreads = s;
      let max: number = this.image != null ? 1 : 0;
      for(let i = 0; i < this.spreads.length; i++) {
        if(this.spreads[i].sublane > max) {
          max = this.spreads[i].sublane;
        }
      }
      this.height = ((max + 1) * (this.spreadHeight + this.spreadMargin)) + this.spreadMargin;
    }

    backgroundSpreads() {
      return this.spreads.filter(s => s.background == true);
    }

    foregroundSpreads() {
      return this.spreads.filter(s => s.background == false);
    }

    getLinkValuesForSeries(cfg: GanttSeriesConfig) : string[] {
      let linkAttributes = cfg.laneForeignAttributes || this.config.linkAttributes;
      let linkValues = linkAttributes.map(la => this.object.get(la));
      return linkValues;
    }
  }

  export class GanttSpread {
    id: string | null = null; //currently only used for groupings
    label: string;
    start: number;
    end: number;
    width: number;
    height: number;
    margin: number;
    offsetTop: number;
    sublane: number;
    color: string;
    labelcolor: string;
    indicator: boolean = false;
    dragging: boolean = false;
    tip: string | null = null;
    dependencies: GanttDependency[] = [];
    background: boolean = false;
    ghost: boolean = false;
    centerLabel: boolean = false;
    selectedBorderColor: string | null;
    hardLaneValues: string[];
    object: RbObject | null;
    dataset: RbDatasetComponent | null;
    config: GanttSeriesConfig;
    groupKey: string | null = null;
    groupOf: string | null = null;

    constructor(label: string, start: number, width: number, height: number, margin: number, offsettop: number, sublane: number, object: RbObject|null, dataset: RbDatasetComponent|null, cfg: GanttSeriesConfig) {
      this.label = label;
      this.start = start;
      this.width = width;
      this.end = start + width;
      this.height = height;
      this.margin = margin;
      this.offsetTop = offsettop;
      this.sublane = sublane;
      this.object = object;
      this.dataset = dataset;
      this.config = cfg;
      if (this.config != null) {
        this.background = this.config.isBackground;
        this.ghost = this.config.isGhost;
        this.centerLabel = this.config.centerLabel;
        this.selectedBorderColor = this.config.selectedBorderColor;
        this.groupOf = this.config.groupOf;
      }
      this.color = (this.background ? 'white' : 'var(--primary-light-color)');
      this.labelcolor = "#333";
    }

    get laneValues(): string[] | null {
      return this.config != null && this.config.laneAttributes != null && this.object != null ? this.config.laneAttributes.map(la => this.object!.get(la)) : this.hardLaneValues;
    }

    get laneTop(): number {
      return (this.sublane * this.height) + (this.background == false ? (this.sublane + 1) * this.margin : 0);
    }

    get canEdit(): boolean {
      return this.object != null && this.config != null ? this.config.canEdit && ((this.config.start.attribute != null && this.object.canEdit(this.config.start.attribute)) || (this.config.laneAttributes != null && this.config.laneAttributes.reduce((acc, la) => acc && this.object.canEdit(la), true))) : false;
    }

    get selected(): boolean {
      return this.background == false && this.dataset != null && this.object != null && this.dataset.isObjectSelected(this.object);
    }
  }

  export class GanttOverlayLane {
    id: string|null;
    label: string;
    height: number;
    spreads: GanttOverlaySpread[] = [];

    constructor(i: string|null, l: string, h: number) {
      this.id = i;
      this.label = l != null ? l : "";
      this.height = h;
    }

    setSpreads(s: GanttOverlaySpread[]) {
      this.spreads = s;
    }
  }

  export class GanttOverlaySpread {
    id: string|null;
    start: number;
    width: number;
    color: string;
    object: RbObject;
    config: GanttOverlayConfig;

    constructor(i: string|null, s: number, w: number, c: string, o: RbObject, cfg: GanttOverlayConfig) {
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
    dayLabel: string|null;
    timeLabel: string|null;
    type: GanttMarkType;

    constructor(p: number, dl: string|null, tl: string|null, t: GanttMarkType) {
      this.px = p;
      this.dayLabel = dl;
      this.timeLabel = tl;
      this.type = t;
    }
  }

  export enum GanttDependencyType {
    SS, FS, SF, DU
  }
  export class GanttDependency {
    spread: GanttSpread;
    type: GanttDependencyType;

    constructor(s: GanttSpread, t: GanttDependencyType) {
      this.spread = s;
      this.type = t;
    }

  }
