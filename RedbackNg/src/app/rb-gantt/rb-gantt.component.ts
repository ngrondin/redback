import { Component, OnInit, Input, SimpleChange, Output, EventEmitter, ViewChild, ViewContainerRef, Injector } from '@angular/core';
import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { RbObject } from 'app/datamodel';
import { BuildService } from 'app/services/build.service';
import { DragService } from 'app/services/drag.service';
import { FilterService } from 'app/services/filter.service';
import { ModalService } from 'app/services/modal.service';
import { UserprefService } from 'app/services/userpref.service';
import { Subscription } from 'rxjs';
import { GanttLane, GanttLaneConfig, GanttMark, GanttSeriesConfig, GanttSpread } from './rb-gantt-models';
import { RbScrollComponent } from 'app/rb-scroll/rb-scroll.component';
import { DataService } from 'app/services/data.service';
import { LogService } from 'app/services/log.service';


@Component({
  selector: 'rb-gantt',
  templateUrl: './rb-gantt.component.html',
  styleUrls: ['./rb-gantt.component.css']
})
export class RbGanttComponent extends RbDataCalcComponent<GanttSeriesConfig> {
  @Input('lanes') lanes : any;
  @Input('toolbar') toolbarConfig : any;
  @Input('layers') layers: any[];
  @Input('locktonow') locktonow: boolean = false;
  @ViewChild('customtoolbar', { read: ViewContainerRef, static: true }) toolbar: ViewContainerRef;
  @ViewChild('mainscroll') mainscroll: RbScrollComponent;
  
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
  dayNames: String[] = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
  ganttData: GanttLane[];
  dayMarks: GanttMark[];
  hourMarks: GanttMark[];

  refocus: boolean = false;
  blockNextRefocus: boolean = false;

  public getSizeForObjectCallback: Function;
  public droppedOutCallback: Function;
  dragSubscription: Subscription;
  laneFilterObject: RbObject;
  
  constructor(
    private modalService: ModalService,
    private dragService: DragService,
    private filterService: FilterService,
    private userprefService: UserprefService,
    private buildService: BuildService,
    private dataService: DataService,
    private logService: LogService
  ) {
    super();
  }

  dataCalcInit() {
    this.dragSubscription = this.dragService.getObservable().subscribe(event => this.onDragEvent(event));
    this.getSizeForObjectCallback = this.getSizeForObject.bind(this);
    this.droppedOutCallback = this.droppedOut.bind(this);
    this.spanMS = 259200000;
    this.zoomMS = 259200000;
    if(this.lanes != null) {
      this.lanesConfig = new GanttLaneConfig(this.lanes, this.userPref);
    }
    if(this.toolbarConfig != null) {
      for(var item of this.toolbarConfig) {
        var context: any = {};
        this.buildService.buildConfigRecursive(this.toolbar, item, context);
      }
    }
  }

  dataCalcDestroy() {

  }

  createSeriesConfig(json: any): GanttSeriesConfig {
    return new GanttSeriesConfig(json, this.userPref);
  }

  onActivationEvent(event: any) {
    if(this.active) {
      this.scrollTop = 0;
      this.scrollLeft = 0;
      let target = null;
      for(let cfg of this.seriesConfigs) {
        let dataset = this.datasetgroup != null ? this.datasetgroup.datasets[cfg.dataset] : this.dataset;
        if(dataset.dataTarget != null && dataset.dataTarget.select != null) {
          target = {
            cfg: cfg, 
            objectname: dataset.objectname, 
            filter: dataset.dataTarget.select, 
            object: dataset.selectedObject
          }
        }
      }
      if(target != null && target.filter != null && target.object == null) {
        this.logService.info("Gantt actiavate with target that is not in the dataset");
        this.dataService.fetchFirst(target.objectname, target.filter, null, null).subscribe((obj) => {
          if(obj != null) {
            let ms = (new Date(obj.get(target.cfg.startAttribute))).getTime();
            this.startDate = new Date(ms - (3*60*60*1000));
            super.onActivationEvent(event);  
          }
        })
      } else {
        super.onActivationEvent(event);
      }
    } else {
      super.onActivationEvent(event);
    }
  }

  onDatasetEvent(event: any) {
    if(event.endsWith('_select') && this.blockNextRefocus == false) {
      this.refocus = true;
    }
    super.onDatasetEvent(event);
  }

  onDragEvent(event: any) {
    if(this.lanesConfig.dragfilter != null && this.doDragFilter) {
      if(event.type == 'start') {
        this.laneFilterObject = event.data;
      } else if(event.type == 'end' && this.laneFilterObject != null) {
        this.laneFilterObject = null;
      }
    }
    this.redraw();
  }

  get userPref() : any {
    return this.id != null ? this.userprefService.getCurrentViewUISwitch("gantt", this.id) : null;
  }

  get selectedObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : this.datasetgroup != null ? this.datasetgroup.selectedObject : null;
  }

  get startDate(): Date {
    return this._startDate;
  }

  set startDate(dt: Date) {
    this._startDate = new Date(dt);
    this.updateData(true);
  }

  get laneHeight() : number {
    return GanttLane.ganttLaneHeight;
  }

  get isLoading() : boolean {
    return this.dataset != null ? this.dataset.isLoading : this.datasetgroup != null ? this.datasetgroup.isLoading : false;
  }

  setZoom(ms: number) {
    this.zoomMS = ms;
    this.updateData(false);
  }

  setSpan(ms: number) {
    this.spanMS = ms;
    this.updateData(true);
  }

  toggleDragFilter() {
    this.doDragFilter = !this.doDragFilter;
  }

  getFilterSortForSeries(cfg: GanttSeriesConfig) : any {
    let startDate = this.startDate;
    let endDate = new Date(this.startDate.getTime() + this.spanMS);
    let filter = {};
    if(cfg.endAttribute != null) {
      filter[cfg.startAttribute] = {
        $lt: "'" + endDate.toISOString() + "'"
      }
      filter[cfg.endAttribute] = {
        $gt: "'" + startDate.toISOString() + "'",
      }
    } else {
      filter[cfg.startAttribute] = {
        $gt: "'" + startDate.toISOString() + "'",
        $lt: "'" + endDate.toISOString() + "'"
      }
    }
    return {filter:filter};
  }

  calc() {
    this.calcParams();
    this.ganttData = this.getLanes();
    this.dayMarks = this.getDayMarks();
    this.hourMarks = this.getHourMarks();
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
    if(this.doDragFilter && this.laneFilterObject != null && this.lanesConfig.dragfilter != null) {
      laneFilter =  this.filterService.resolveFilter(this.lanesConfig.dragfilter, this.laneFilterObject, null, null);
    };
    let list: RbObject[] = this.lists != null ? this.lists[this.lanesConfig.dataset] : this.list;
    for(let obj of list) {
      let show = true;
      if(laneFilter != null) {
        if(!this.filterService.applies(laneFilter, obj)) show = false;
      }
      if(show) {
        let label = obj.get(this.lanesConfig.labelAttribute);
        let image = null;
        let icon = null;
        if(this.lanesConfig.iconAttribute != null) {
          icon = obj.get(this.lanesConfig.iconAttribute);
          if(this.lanesConfig.iconMap != null) {
            icon = this.lanesConfig.iconMap[icon];
          }  
        }
        if(this.lanesConfig.imageAttribute != null) {
          let fileVal = obj.get(this.lanesConfig.imageAttribute);
          if(fileVal != null && fileVal.thumbnail != null) {
            image = "url(\'" + fileVal.thumbnail + "\')"
          }
        }
        let lane = new GanttLane(obj.uid, label, image, icon, obj);
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
      let dataset = this.datasetgroup != null ? this.datasetgroup.datasets[cfg.dataset] : this.dataset;
      let show = cfg.show == null || (cfg.show != null && cfg.show(null, dataset, dataset.relatedObject));
      if(show) {
        let list: RbObject[] = dataset.list;
        for(var i in list) {
          let obj = list[i];
          if(obj.get(cfg.laneAttribute) == laneId && obj != this.dragService.data) {
            let startMS = (new Date(obj.get(cfg.startAttribute))).getTime();
            let startPX: number = Math.round((startMS - this.startMS) * this.multiplier);
            if(startPX < this.widthPX) {
              let durationMS = this.getObjectDuration(cfg, obj);
              if(startMS + durationMS > this.endMS) {
                durationMS = this.endMS - startMS;
              }
              let endMS = startMS + durationMS;
              let endPX = Math.round((endMS - this.startMS) * this.multiplier);
              let widthPX = endPX - startPX;
              if(startPX > -widthPX) {
                if(startPX < 0) {
                  widthPX = widthPX + startPX;
                  startPX = 0;
                }
                let height = cfg.isBackground ? GanttLane.ganttLaneHeight : 28;
                let label = obj.get(cfg.labelAttribute); 
                let prelabel = cfg.preLabelAttribute != null ? obj.get(cfg.preLabelAttribute) : null;
                let postlabel = cfg.postLabelAttribute != null ? obj.get(cfg.postLabelAttribute) : null;
                let color = cfg.isBackground ? 'white' : 'var(--primary-light-color)';
                if(cfg.color != null) {
                  color = cfg.color;
                } else if(cfg.colorAttribute != null) {
                  let colorValue = obj.get(cfg.colorAttribute);
                  if(colorValue != null) {
                    if(cfg.colorMap != null) {
                      if(cfg.colorMap[colorValue] != null) {
                        color = cfg.colorMap[colorValue];
                      }
                    } else {
                      color = colorValue;
                    }  
                  }
                }
                let labelcolor = "#333";
                if(cfg.labelColor != null) {
                  labelcolor = cfg.labelColor;
                }
                let canEdit: boolean = cfg.canEdit && (obj.canEdit(cfg.startAttribute) || obj.canEdit(cfg.laneAttribute));
                let selected: boolean = false;
                if(cfg.isBackground == false && dataset.selectedObject == obj) {
                  selected = true;
                  if(this.refocus) {
                    this.mainscroll.scrollToHPos(Math.max(0, startPX - 30));
                    this.refocus = false;
                    this.blockNextRefocus = false;
                  }
                }
                if(color != null) {
                  spreads.push(new GanttSpread(obj.uid, label, prelabel, postlabel, startPX, widthPX, height, laneId, color, labelcolor, canEdit, selected, obj, cfg));
                }
              }
            }
          }
        }
      }
    }
    this.adjustSpreadTops(spreads);
    return spreads;
  }

  private getObjectDuration(cfg: GanttSeriesConfig, obj: RbObject): number {
    let durationMS = null;
    if(cfg.durationAttribute != null) {
      durationMS = parseInt(obj.get(cfg.durationAttribute));
    } else if(cfg.endAttribute != null) {
      durationMS = (new Date(obj.get(cfg.endAttribute))).getTime() - (new Date(obj.get(cfg.startAttribute))).getTime();
    } 
    if(durationMS == null || isNaN(durationMS)) durationMS = 3600000;
    return durationMS;
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
            if(!os.config.isBackground && s.start < os.start + os.width && os.start < s.start + s.width && s.sublane == os.sublane) {
              hasOverlap = true;
            }
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
      let mark = new GanttMark(pos, this.dayNames[curDate.getDay()] + ", " + curDate.getDate() + " " + this.monthNames[curDate.getMonth()] + " " + curDate.getFullYear());
      marks.push(mark);
      cur = cur + 86400000;
    }
    return marks;
  }


  public select(object: RbObject) {
    if(this.dataset != null) {
      this.dataset.select(object);
    } else if(this.datasetgroup != null) {
      this.datasetgroup.select(object);
    }
  }

  public clickSpread(spread: GanttSpread) {
    this.blockNextRefocus = true;
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

  public droppedOn(event: any, lane: GanttLane, ignoreTime: boolean = false) {
    let update: any = {};
    let related: any = {}
    let object: RbObject = event.data;
    let config: GanttSeriesConfig = this.getSeriesConfigForObject(object);

    if(ignoreTime == false) {
      let previousStart = object.get(config.startAttribute);
      let previousDuration = this.getObjectDuration(config, object);
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
          let newEnd = (new Date(newStartMS + previousDuration)).toISOString();
          update[config.endAttribute] = newEnd;
        } 
      }
    }

    let previousLaneId = object.get(config.laneAttribute);
    if(previousLaneId != lane.id) {
      update[config.laneAttribute] = lane.id;
      related[config.laneAttribute] = lane.object;
    }
    
    this.blockNextRefocus = true;
    if(Object.keys(update).length > 0) {
      object.setValuesAndRelated(update, related);
    }

    if(this.datasetgroup.datasets[config.dataset].list.indexOf(object) == -1) {
      this.datasetgroup.datasets[config.dataset].addObjectAndSelect(object);
    }
    //this.calc();
  }

  public droppedOut(event: any) {
    let object: RbObject = event.data;
    let config: GanttSeriesConfig = this.getSeriesConfigForObject(object);
    let update: any = {};
    update[config.startAttribute] = null;
    update[config.laneAttribute] = null;
    object.setValues(update);
  }

  public scroll(event) {
    this.scrollLeft = event.target.scrollLeft;
    this.scrollTop = event.target.scrollTop;
  }

  public getSizeForObject(obj: RbObject) : any {
    let cfg: GanttSeriesConfig = this.getSeriesConfigForObject(obj);
    let durationMS = this.getObjectDuration(cfg, obj);
    return {
      x: Math.round(durationMS * this.multiplier),
      y: 28
    };
  }
}
