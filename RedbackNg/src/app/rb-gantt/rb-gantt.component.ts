import { Component, OnInit, Input, SimpleChange, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { ObserveOnMessage } from 'rxjs/internal/operators/observeOn';
import { RbSearchComponent } from 'app/rb-search/rb-search.component';
import { trigger } from '@angular/animations';
import { MATERIAL_SANITY_CHECKS } from '@angular/material';
import { Md5 } from 'ts-md5/dist/md5';

class GanttLaneConfig {
  dataset: string;
  labelAttribute: string;
  iconAttribute: string;
  iconMap: any;
  modal: string;

  constructor(json: any) {
    this.dataset = json.dataset;
    this.labelAttribute = json.labelattribute;
    this.iconAttribute = json.iconattribute;
    this.iconMap = json.iconmap;
    this.modal = json.modal;
  }
}

class GanttSeriesConfig {
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

  constructor(json: any) {
    this.dataset = json.dataset;
    this.startAttribute = json.startattribute;
    this.durationAttribute = json.durationattribute;
    this.endAttribute = json.endattribute;
    this.labelAttribute = json.labelattribute;
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

class GanttLane {
  id: string;
  label: string;
  icon: string;
  spreads: GanttSpread[];
  object: RbObject;

  constructor(i: string, l: string, ic: string, o: RbObject, s: GanttSpread[]) {
    this.id = i;
    this.label = l != null ? l : "";
    this.icon = ic;
    this.spreads = s;
    this.object = o;
  }
}

class GanttSpread {
  id: string;
  label: string;
  start: number;
  width: number;
  lane: string;
  color: string;
  canEdit: Boolean;
  object: RbObject;
  config: GanttSeriesConfig;

  constructor(i: string, l: string, s: number, w: number, ln: string, c: string, ce: Boolean, o: RbObject, cfg: GanttSeriesConfig) {
    this.id = i;
    this.label = l;
    this.start = s;
    this.width = w;
    this.lane = ln;
    this.color = c;
    this.canEdit = ce;
    this.object = o;
    this.config = cfg;
  }
}

class GanttMark {
  px: number;
  label: string;

  constructor(p: number, l: string) {
    this.px = p;
    this.label = l;
  }
}


@Component({
  selector: 'rb-gantt',
  templateUrl: './rb-gantt.component.html',
  styleUrls: ['./rb-gantt.component.css']
})
export class RbGanttComponent implements OnInit {
  @Input('lists') lists : any;
  @Input('lanes') lanes : any;
  @Input('series') series: any[];
  @Input('selectedObject') selectedObject: RbObject;
  @Output() selectedObjectChange: EventEmitter<any> = new EventEmitter();
  @Output('openModal') openModal: EventEmitter<any> = new EventEmitter();

  lanesConfig: GanttLaneConfig;
  seriesConfigs: GanttSeriesConfig[];
  spanMS: number;
  zoomMS: number;
  markIntervalMS: number;
  startMS: number;
  endMS: number;
  multiplier: number;
  widthPX: number;
  scrollTop: number;
  scrollLeft: number;
  monthNames: String[] = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
  ganttData: any[];
  dayMarks: GanttMark[];
  hourMarks: GanttMark[];

  lastObjectCount: number = 0;
  lastObjectUpdate: number = 0;
  lastHash: string | Int32Array;

  recalcPlanned: boolean = false;
  lastRecalc: number = 0;
  public getSizeForObjectCallback: Function;
  
  constructor() { }


  ngOnChanges(changes : SimpleChange) {
    if('lanes' in changes && this.lanes != null) {
      this.lanesConfig = new GanttLaneConfig(this.lanes);
    }
    if('series' in changes && this.series != null) {
      this.seriesConfigs = [];
      for(let item of this.series) {
        this.seriesConfigs.push(new GanttSeriesConfig(item));
      }
    }
    if('lists' in changes && this.lists != null) {
      if(this.haveListsChanged()) {
        this.redraw();
      }
    }
  }

  ngOnInit() {
    this.getSizeForObjectCallback = this.getSizeForObject.bind(this);
    this.spanMS = 259200000;
    this.zoomMS = 259200000;
    this.redraw();
  }

  haveListsChanged(): Boolean {
    let str: string = "";
    let cnt = 0;
    let lu = 0;
    for(let ser in this.lists) {
      for(let obj of this.lists[ser]) {
        cnt = cnt + 1;
        let u = obj.lastUpdated;
        if(u > lu) {
          lu = u;
        }
      }
    }
    if(cnt != this.lastObjectCount || lu > this.lastObjectUpdate) {
      this.lastObjectCount = cnt;
      this.lastObjectUpdate = lu;
      return true;
    } else { 
      return false;
    }
  }

  setZoom(ms: number) {
    this.zoomMS = ms;
    this.redraw();
  }

  setSpan(ms: number) {
    this.spanMS = ms;
    this.redraw();
  }

  redraw() {
    if(this.recalcPlanned == false) {
      let now = (new Date()).getTime();
      let timeSinceLastRecalc = now - this.lastRecalc;
      if(timeSinceLastRecalc > 250) {
        this.calcAll();
      } else {
        this.recalcPlanned = true;
        setTimeout(() => this.calcAll(), (250 - timeSinceLastRecalc));
      }
    } 
  }

  calcAll() {
    this.calcParams();
    this.ganttData = this.getLanes();
    this.dayMarks = this.getDayMarks();
    this.hourMarks = this.getHourMarks();
    this.lastRecalc = (new Date()).getTime();
    this.recalcPlanned = false;
  }

  private calcParams() {
    if(this.spanMS < this.zoomMS) {
      this.spanMS = this.zoomMS;
    }
    let now = new Date();
    this.startMS = now.getTime();
    this.endMS = this.startMS + this.spanMS;
    this.multiplier = window.innerWidth / this.zoomMS;
    this.widthPX = this.spanMS * this.multiplier;
    this.markIntervalMS = 3600000;
    while(this.markIntervalMS * this.multiplier < 40) {
      this.markIntervalMS *= 2;
    }
  }

  private getLanes() {
    let lanes : GanttLane[] = [];
    let list: RbObject[] = this.lists[this.lanesConfig.dataset];
    for(let obj of list) {
      let label = obj.get(this.lanesConfig.labelAttribute);
      let icon = obj.get(this.lanesConfig.iconAttribute);
      if(this.lanesConfig.iconMap != null) {
        icon = this.lanesConfig.iconMap[icon];
      }
      let lane = new GanttLane(obj.uid, label, icon, obj, this.getSpreads(obj.uid))
      lanes.push(lane);
    }
    if(lanes.length > 0) {
      lanes.sort((a, b) => (a != null && b != null ? a.label.localeCompare(b.label) : 0));
    }
    return lanes;
  }

  private getSpreads(laneId: string) : GanttSpread[] {
    let spreads : GanttSpread[] = [];
    for(let cfg of this.seriesConfigs) {
      let list: RbObject[] = this.lists[cfg.dataset];
      for(var i in list) {
        let obj = list[i];
        if(obj.get(cfg.laneAttribute) == laneId) {
          let startMS = (new Date(obj.get(cfg.startAttribute))).getTime();
          let startPX: number = Math.round((startMS - this.startMS) * this.multiplier);
          if(startPX < this.widthPX) {
            let durationMS;
            if(cfg.durationAttribute != null) {
              durationMS = parseInt(obj.get(cfg.durationAttribute));
            } else if(cfg.endAttribute != null) {
              durationMS = (new Date(obj.get(cfg.endAttribute))).getTime() - startMS;
            } else {
              durationMS = 3600000;
            }
            if(startMS + durationMS > this.endMS) {
              durationMS = this.endMS - startMS;
            }
            let widthPX = Math.round(durationMS * this.multiplier);
            if(startPX > -widthPX) {
              if(startPX < 0) {
                widthPX = widthPX + startPX;
                startPX = 0;
              }
              let label = cfg.isBackground ? "" : obj.get(cfg.labelAttribute);
              let color = 'white';
              if(cfg.colorAttribute != null) {
                if(cfg.colorMap != null) {
                  color = cfg.colorMap[obj.get(cfg.colorAttribute)];
                } else {
                  color = obj.get(cfg.colorAttribute);
                }
              }
              let canEdit: Boolean = cfg.canEdit && (obj.canEdit(cfg.startAttribute) || obj.canEdit(cfg.laneAttribute));
              if(color != null) {
                spreads.push(new GanttSpread(obj.uid, label, startPX, widthPX, laneId, color, canEdit, obj, cfg));
              }
            }
          }
        }
      }
    }
    return spreads;
  }

  private getHourMarks() : GanttMark[] {
    let marks: GanttMark[] = [];
    let lastMidnight = (new Date(this.startMS));
    lastMidnight.setHours(0);
    lastMidnight.setMinutes(0);
    lastMidnight.setSeconds(0);
    lastMidnight.setMilliseconds(0);
    let cur = lastMidnight.getTime();
    while(cur < this.endMS) {
      let curDate: Date = new Date(cur);
      let pos = Math.round((cur - this.startMS) * this.multiplier);
      if(pos > 0) {
        let mark = new GanttMark(pos, curDate.getHours() + ":00");
        marks.push(mark);
      }
      cur = cur + this.markIntervalMS;
    }
    return marks;
  }

  private getDayMarks() : GanttMark[] {
    let marks: GanttMark[] = [];
    let lastMidnight = (new Date(this.startMS));
    lastMidnight.setHours(0);
    lastMidnight.setMinutes(0);
    lastMidnight.setSeconds(0);
    lastMidnight.setMilliseconds(0);
    let cur = lastMidnight.getTime();
    while(cur < this.endMS) {
      let curDate: Date = new Date(cur);
      let pos = Math.round((cur - this.startMS) * this.multiplier);
      let mark = new GanttMark(pos, curDate.getDate() + " " + this.monthNames[curDate.getMonth()] + " " + curDate.getFullYear());
      marks.push(mark);
      cur = cur + 86400000;
    }
    return marks;
  }

  public getSeriesConfigForObject(object: RbObject) : GanttSeriesConfig {
    for(let key in this.lists) {
      for(let obj of this.lists[key]) {
        if(obj == object) {
          for(let sc of this.seriesConfigs) {
            if(sc.dataset == key) {
              return sc;
            }
          }
        }
      }
    }
    return null;
  }

  public clickSpread(spread: GanttSpread) {
    this.selectedObjectChange.emit(spread.object);
    if(spread.config.modal != null) {
      this.openModal.emit(spread.config.modal);
    }
  }

  public clickLane(lane: GanttLane) {
    this.selectedObjectChange.emit(lane.object);
    if(this.lanesConfig.modal != null) {
      this.openModal.emit(this.lanesConfig.modal);
    }
  }

  public dropped(event, lane: GanttLane) {
    let update: any = {};
    let object: RbObject = event.object;
    let config: GanttSeriesConfig = this.getSeriesConfigForObject(object);
    let previousStart = object.get(config.startAttribute);
    let tgt = event.mouseEvent.target;
    let left = event.mouseEvent.offsetX - event.offset.x;
    while(tgt.className.indexOf("rb-gantt-lane") == -1) {
      left = left + tgt.offsetLeft;
      tgt = tgt.offsetParent;
    }
    let newStartMS = this.startMS + (left / this.multiplier);
    let newStart = (new Date(newStartMS)).toISOString();
    if(previousStart != newStart) {
      update[config.startAttribute] = newStart;
      if(config.durationAttribute == null && config.endAttribute != null) {
        let previousStartMS = (new Date(previousStart)).getTime();
        let previousEndMS = (new Date(event.object.get(config.endAttribute))).getTime();
        let durationMS = previousEndMS - previousStartMS;
        let newEnd = (new Date(newStartMS + durationMS)).toISOString();
        update[config.endAttribute] = newEnd;
      } 
    }

    let previousLane = object.get(config.laneAttribute);
    let newLane = lane.id;
    if(previousLane != newLane) {
      update[config.laneAttribute] = newLane;
    }
    if(Object.keys(update).length > 0) {
      event.object.setValues(update);
    }
  }

  public scroll(event) {
    this.scrollLeft = event.target.scrollLeft;
    this.scrollTop = event.target.scrollTop;
  }

  public getSizeForObject(object: RbObject) : any {
    let cfg: GanttSeriesConfig = this.getSeriesConfigForObject(object);
    return {
      x: Math.round(object.get(cfg.durationAttribute) * this.multiplier),
      y: 28
    };
  }
}
