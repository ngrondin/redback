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
    laneAttribute: string;
    labelAttribute: string;
    preLabelAttribute: string;
    postLabelAttribute: string;
    prioritizePrePostLabel: boolean;
    labelColor: string;
    color: string;
    colorAttribute: string;
    colorMap: any;
    isBackground: boolean;
    modal: string;
    canEdit: boolean;
    show: Function;
  
    constructor(json: any, userpref: any) {
      super(json);
      let subpref = userpref != null && userpref.series != null ? userpref.series[json.dataset] : null;
      this.startAttribute = json.startattribute;
      this.durationAttribute = json.durationattribute;
      this.endAttribute = json.endattribute;
      this.laneAttribute = json.laneattribute;
      this.labelAttribute = subpref != null && subpref.labelattribute != null ? subpref.labelattribute : json.labelattribute;
      this.preLabelAttribute = subpref != null && subpref.prelabelattribute != null ? subpref.prelabelattribute : json.prelabelattribute;
      this.postLabelAttribute = subpref != null && subpref.postlabelattribute != null ? subpref.postlabelattribute : json.postlabelattribute;
      this.prioritizePrePostLabel = json.prioritizeprepostlabel ?? false;
      this.labelColor = json.labelcolor;
      this.isBackground = json.isbackground;
      this.canEdit = json.canedit;
      this.color = json.color;
      this.colorAttribute = json.colorattribute;
      this.colorMap = json.colormap;
      this.modal = json.modal;
      this.show = json.show != null ? Function("dataset", "relatedObject", "return (" + json.show + ")") : null;
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
    prelabel: string;
    postlabel: string;
    start: number;
    width: number;
    top: number;
    height: number;
    sublane: number;
    lane: string;
    color: string;
    labelcolor: string;
    canEdit: boolean;
    selected: boolean
    object: RbObject;
    config: GanttSeriesConfig;
  
    constructor(i: string, l: string, prel: string, postl: string, s: number, w: number, h: number, ln: string, c: string, lc: string, ce: boolean, sel: boolean, o: RbObject, cfg: GanttSeriesConfig) {
      this.id = i;
      this.label = l;
      this.prelabel = prel;
      this.postlabel = postl;
      this.start = s;
      this.width = w;
      this.height = h;
      this.sublane = 0;
      this.lane = ln;
      this.color = c;
      this.labelcolor = lc;
      this.canEdit = ce;
      this.selected = sel;
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