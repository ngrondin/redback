import { Component, OnInit, Input, SimpleChange, Output, EventEmitter } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { FilterBuilderConfig } from 'app/rb-filter-builder/rb-filter-builder.component';
import { DragService } from 'app/services/drag.service';
import { FilterService } from 'app/services/filter.service';
import { ModalService } from 'app/services/modal.service';
import { Subscription } from 'rxjs';

let ganttLaneHeight: number = 42;

class GanttLaneConfig {
  dataset: string;
  labelAttribute: string;
  iconAttribute: string;
  iconMap: any;
  modal: string;
  dragfilter: any;

  constructor(json: any) {
    this.dataset = json.dataset;
    this.labelAttribute = json.labelattribute;
    this.iconAttribute = json.iconattribute;
    this.iconMap = json.iconmap;
    this.modal = json.modal;
    this.dragfilter = json.dragfilter;
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
  height: number;
  spreads: GanttSpread[];
  object: RbObject;

  constructor(i: string, l: string, ic: string, o: RbObject) {
    this.id = i;
    this.label = l != null ? l : "";
    this.icon = ic;
    this.object = o;
    this.height = ganttLaneHeight;
  }

  setSpreads(s: GanttSpread[]) {
    this.spreads = s;
    let max: number = 0;
    for(let i = 0; i < this.spreads.length; i++) {
      if(this.spreads[i].sublane > max) {
        max = this.spreads[i].sublane;
      }
    }
    this.height = ganttLaneHeight * (max + 1);
  }
}

class GanttSpread {
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
    this.top = (this.sublane * ganttLaneHeight) + (ganttLaneHeight - this.height) / 2;
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
export class RbGanttComponent extends RbDataObserverComponent {
  @Input('lanes') lanes : any;
  @Input('series') series: any[];
  @Input('locktonow') locktonow: boolean = false;
  @Input('dofilter') dofilter: boolean = true;

  lanesConfig: GanttLaneConfig;
  seriesConfigs: GanttSeriesConfig[];
  _startDate: Date = new Date();
  spanMS: number;
  zoomMS: number;
  markIntervalMS: number;
  startMS: number;
  endMS: number;
  multiplier: number;
  widthPX: number;
  doDragFilter: boolean = false;
  scrollTop: number;
  scrollLeft: number;
  monthNames: String[] = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
  ganttData: GanttLane[];
  dayMarks: GanttMark[];
  hourMarks: GanttMark[];

  lastObjectCount: number = 0;
  lastObjectUpdate: number = 0;
  lastHash: string | Int32Array;

  recalcPlanned: boolean = false;
  lastRecalc: number = 0;
  public getSizeForObjectCallback: Function;
  dragSubscription: Subscription;
  
  constructor(
    private modalService: ModalService,
    private dragService: DragService,
    private filterService: FilterService
  ) {
    super();
  }

  dataObserverInit() {
    this.dragSubscription = this.dragService.getObservable().subscribe(event => this.onDragEvent(event));
    this.getSizeForObjectCallback = this.getSizeForObject.bind(this);
    this.spanMS = 259200000;
    this.zoomMS = 259200000;
    if(this.lanes != null) {
      this.lanesConfig = new GanttLaneConfig(this.lanes);
    }
    if(this.series != null) {
      this.seriesConfigs = [];
      for(let item of this.series) {
        this.seriesConfigs.push(new GanttSeriesConfig(item));
      }
    }
    if(this.dofilter && this.active) {
      this.filterDataset();
    }
  }

  dataObserverDestroy() {
    this.dragSubscription.unsubscribe();
  }

  onDatasetEvent(event: any) {
    if(this.haveListsChanged()) {
      this.redraw();
    }
  }

  onActivationEvent(event: any) {
    if(this.dofilter && this.active) {
      this.filterDataset();
    }
  }

  onDragEvent(event: any) {
    if(this.lanesConfig.dragfilter != null) {
      this.redraw();
    }
  }

  get selectedObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : this.datasetgroup != null ? this.datasetgroup.selectedObject : null;
  }

  get startDate(): Date {
    return this._startDate;
  }

  set startDate(dt: Date) {
    this._startDate = new Date(dt);
    if(this.dofilter) {
      this.filterDataset();
    } else {
      this.redraw();
    }
  }

  get laneHeight() : number {
    return ganttLaneHeight;
  }

  get isLoading() : boolean {
    return this.dataset != null ? this.dataset.isLoading : this.datasetgroup != null ? this.datasetgroup.isLoading : false;
  }

  haveListsChanged(): Boolean {
    let str: string = "";
    let cnt = 0;
    let lu = 0;
    if(this.list != null) {
      for(let obj of this.list) {
        cnt = cnt + 1;
        let u = obj.lastUpdated;
        if(u > lu) {
          lu = u;
        }
      }
    }
    if(this.lists != null) {
      for(let ser in this.lists) {
        for(let obj of this.lists[ser]) {
          cnt = cnt + 1;
          let u = obj.lastUpdated;
          if(u > lu) {
            lu = u;
          }
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
    if(this.dofilter) {
      this.filterDataset();
    } else {
      this.redraw();
    }
  }

  refresh() {
    if(this.dataset != null) {
      this.dataset.refreshData();
    }
    if(this.datasetgroup != null) {
      this.datasetgroup.refreshAllData();
    }
  }

  toggleDragFilter() {
    this.doDragFilter = !this.doDragFilter;
  }

  filterDataset() {
    let startDate = this.startDate;
    let endDate = new Date(this.startDate.getTime() + this.spanMS);
    for(let cfg of this.seriesConfigs) {
      let filter = {};
      filter[cfg.startAttribute] = {
        $gt: "'" + startDate.toISOString() + "'",
        $lt: "'" + endDate.toISOString() + "'"
      }
      if(cfg.endAttribute != null) {
        let sFilter = filter;
        let eFilter = {}
        eFilter[cfg.endAttribute] = {
          $gt: "'" + startDate.toISOString() + "'",
          $lt: "'" + endDate.toISOString() + "'"          
        };
        filter = {
          $or:[sFilter, eFilter]
        }
      }
      if(this.datasetgroup != null) {
        this.datasetgroup.datasets[cfg.dataset].filterSort({filter: filter});
      } else {
        this.dataset.filterSort({filter: filter});
      }
    }
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
    this.startMS = this.startDate != null ? this.startDate.getTime() : (new Date()).getTime();
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
    let laneFilter: any = null;
    if(this.doDragFilter && this.dragService.object != null && this.lanesConfig.dragfilter != null) {
      laneFilter =  this.filterService.resolveFilter(this.lanesConfig.dragfilter, this.dragService.object, null, null);
    };
    let list: RbObject[] = this.lists != null ? this.lists[this.lanesConfig.dataset] : this.list;
    for(let obj of list) {
      let show = true;
      if(laneFilter != null) {
        if(!this.filterService.applies(laneFilter, obj)) show = false;
      }
      if(show) {
        let label = obj.get(this.lanesConfig.labelAttribute);
        let icon = obj.get(this.lanesConfig.iconAttribute);
        if(this.lanesConfig.iconMap != null) {
          icon = this.lanesConfig.iconMap[icon];
        }
        let lane = new GanttLane(obj.uid, label, icon, obj);
        let spreads: GanttSpread[] = this.getSpreads(obj.uid);
        lane.setSpreads(spreads);
        lanes.push(lane);
      }
    }
    if(lanes.length > 0) {
      lanes.sort((a, b) => (a != null && b != null ? a.label.localeCompare(b.label) : 0));
    }
    return lanes;
  }

  private getSpreads(laneId: string) : GanttSpread[] {
    let spreads : GanttSpread[] = [];
    for(let cfg of this.seriesConfigs) {
      let list: RbObject[] = this.lists != null ? this.lists[cfg.dataset] : this.list;
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
              let height = cfg.isBackground ? ganttLaneHeight : 28;
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
                spreads.push(new GanttSpread(obj.uid, label, startPX, widthPX, height, laneId, color, canEdit, obj, cfg));
              }
            }
          }
        }
      }
    }
    this.adjustSpreadTops(spreads);
    return spreads;
  }

  private adjustSpreadTops(spreads: GanttSpread[]) {
    for(var i = 0; i < spreads.length; i++) {
      let s: GanttSpread = spreads[i];
      if(!s.config.isBackground) {
        let hasOverlap: boolean;
        do {
          hasOverlap = false;
          for(var j = 0; j < spreads.length && j < i; j++) {
            let os: GanttSpread = spreads[j];
            if(!os.config.isBackground && s.start < os.start + os.width && os.start < s.start + s.width && s.sublane == os.sublane) hasOverlap = true;
          }
          if(hasOverlap) s.setSubLane(s.sublane + 1);
        } while(hasOverlap);
      }
    }
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
    if(this.lists != null) {
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
    } else if(this.list != null) {
      return this.seriesConfigs[0];
    }
    return null;
  }

  public select(object: RbObject) {
    if(this.dataset != null) {
      this.dataset.select(object);
    } else if(this.datasetgroup != null) {
      this.datasetgroup.select(object);
    }
  }

  public clickSpread(spread: GanttSpread) {
    this.select(spread.object);
    if(spread.config.modal != null) {
      this.modalService.open(spread.config.modal);
    }
  }

  public clickLane(lane: GanttLane) {
    this.select(lane.object);
    if(this.lanesConfig.modal != null) {
      this.modalService.open(this.lanesConfig.modal);
    }
  }

  public dropped(event: any, lane: GanttLane, ignoreTime: boolean = false) {
    let update: any = {};
    let object: RbObject = event.object;
    let config: GanttSeriesConfig = this.getSeriesConfigForObject(object);

    if(ignoreTime == false) {
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
    }

    let previousLaneId = object.get(config.laneAttribute);
    if(previousLaneId != lane.id) {
      update[config.laneAttribute] = lane.id;
    }
    if(Object.keys(update).length > 0) {
      event.object.setValues(update);
      this.redraw();
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
