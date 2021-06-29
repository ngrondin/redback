import { SeriesConfig } from "app/abstract/rb-datacalc";
import { RbObject } from "app/datamodel";


export class GanttLaneConfig {
    dataset: string;
    labelAttribute: string;
    iconAttribute: string;
    iconMap: any;
    modal: string;
    dragfilter: any;
  
    constructor(json: any, userpref: any) {
      this.dataset = json.dataset;
      this.labelAttribute = json.labelattribute;
      this.iconAttribute = json.iconattribute;
      this.iconMap = json.iconmap;
      this.modal = json.modal;
      this.dragfilter = json.dragfilter;
    }
  }
  
  export class GanttSeriesConfig extends SeriesConfig {
    dataset: string;
    startAttribute: string;
    durationAttribute: string;
    endAttribute: string;
    labelAttribute: string;
    laneAttribute: string;
    laneLabelAttribute: string;
    laneIconAttribute: string;
    laneIconMap: any;
    colorAttribute: string;
    colorMap: any;
    isBackground: boolean;
    modal: string;
    canEdit: boolean;
  
    constructor(json: any, userpref: any) {
      super(json);
      let subpref = userpref != null && userpref.series != null ? userpref.series[json.dataset] : null;
      this.startAttribute = json.startattribute;
      this.durationAttribute = json.durationattribute;
      this.endAttribute = json.endattribute;
      this.labelAttribute = subpref != null && subpref.labelattribute != null ? subpref.labelattribute : json.labelattribute;
      this.laneAttribute = json.laneattribute;
      this.laneLabelAttribute = json.lanelabelattribute;
      this.laneIconAttribute = json.laneliconattribute;
      this.laneIconMap = json.laneiconmap;
      this.isBackground = json.isbackground;
      this.canEdit = json.canedit;
      this.colorAttribute = json.colorattribute;
      this.colorMap = json.colormap;
      this.modal = json.modal;
    }
  }
  
  export class GanttLane {
    static ganttLaneHeight: number = 42;
    id: string;
    label: string;
    icon: string;
    height: number;
    spreads: GanttSpread[];
    object: RbObject;
  
    constructor(i: string, l: string, ic: string, o: RbObject) {
      this.id = i;
      this.label = l != null ? l : "";
      this.icon = ic;
      this.object = o;
      this.height = GanttLane.ganttLaneHeight;
    }
  
    setSpreads(s: GanttSpread[]) {
      this.spreads = s;
      let max: number = 0;
      for(let i = 0; i < this.spreads.length; i++) {
        if(this.spreads[i].sublane > max) {
          max = this.spreads[i].sublane;
        }
      }
      this.height = GanttLane.ganttLaneHeight * (max + 1);
    }
  }
  
  export class GanttSpread {
    id: string;
    label: string;
    start: number;
    width: number;
    top: number;
    height: number;
    sublane: number;
    lane: string;
    color: string;
    canEdit: Boolean;
    object: RbObject;
    config: GanttSeriesConfig;
  
    constructor(i: string, l: string, s: number, w: number, h: number, ln: string, c: string, ce: Boolean, o: RbObject, cfg: GanttSeriesConfig) {
      this.id = i;
      this.label = l;
      this.start = s;
      this.width = w;
      this.height = h;
      this.sublane = 0;
      this.lane = ln;
      this.color = c;
      this.canEdit = ce;
      this.object = o;
      this.config = cfg;
      this.setSubLane(0);
    }
  
    setSubLane(sl: number) {
      this.sublane = sl;
      this.top = (this.sublane * GanttLane.ganttLaneHeight) + (GanttLane.ganttLaneHeight - this.height) / 2;
    }
  }
  
  export class GanttMark {
    px: number;
    label: string;
  
    constructor(p: number, l: string) {
      this.px = p;
      this.label = l;
    }
  }