import { Component, OnInit, Input, SimpleChange, Output, EventEmitter, ViewChild, ViewContainerRef, Injector } from '@angular/core';
import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { RbObject } from 'app/datamodel';
import { BuildService } from 'app/services/build.service';
import { DragService } from 'app/services/drag.service';
import { FilterService } from 'app/services/filter.service';
import { ModalService } from 'app/services/modal.service';
import { UserprefService } from 'app/services/userpref.service';
import { Subscription } from 'rxjs';
import { GanttLane, GanttLaneConfig, GanttMark, GanttMarkType, GanttOverlayConfig, GanttOverlayLane, GanttOverlaySpread, GanttSeriesConfig, GanttSpread, GanttSpreadHeight } from './rb-gantt-models';
import { RbScrollComponent } from 'app/rb-scroll/rb-scroll.component';
import { DataService } from 'app/services/data.service';
import { LogService } from 'app/services/log.service';
import { Evaluator } from 'app/helpers';
import { AppInjector } from 'app/app.module';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';


@Component({
  selector: 'rb-gantt',
  templateUrl: './rb-gantt.component.html',
  styleUrls: ['./rb-gantt.component.css']
})
export class RbGanttComponent extends RbDataCalcComponent<GanttSeriesConfig> {
  @Input('lanes') lanes : any;
  @Input('overlays') overlays: any[];
  @Input('layers') layers: any[];
  @Input('toolbar') toolbarConfig : any;
  @Input('locktonow') locktonow: boolean = false;
  @Input('allowoverlapgroup') allowoverlapgroup: boolean = false;
  @Input('headerwidth') _headerwidth: number = 17;
  @ViewChild('customtoolbar', { read: ViewContainerRef, static: true }) toolbar: ViewContainerRef;
  @ViewChild('mainscroll') mainscroll: RbScrollComponent;
  
  lanesConfig: GanttLaneConfig;
  seriesConfigs: GanttSeriesConfig[];
  overlayConfigs: GanttOverlayConfig[];
  _startDate: Date = new Date();
  spanMS: number;
  zoomMS: number;
  markMajorIntervalMS: number;
  markMinorIntervalMS: number;
  startMS: number;
  endMS: number;
  pxPerMS: number;
  widthPX: number;
  heightVW: number;
  doDragFilter: boolean = false;
  groupOverlaps: boolean = false;
  scrollTop: number;
  scrollLeft: number;
  monthNames: String[] = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
  dayNames: String[] = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
  _zooms: any[] = [{label:"12 Hours", val:43200000}, {label:"1 Day", val:86400000}, {label:"2 Days", val:172800000}, {label:"3 Days", val:259200000}, {label:"7 Days", val:604800000}];
  _spans: any[] = [{label:"12 Hours", val:43200000}, {label:"1 Day", val:86400000}, {label:"3 Days", val:259200000}, {label:"7 Days", val:604800000}, {label:"14 Days", val:1209600000}];
  labelAlts: any[] = [];
  ganttData: GanttLane[];
  marks: GanttMark[] = [];
  overlayData: GanttOverlayLane[];
  selectedOverlayLaneIndex = -1;
  selectedLabelAlt: string = null;

  //blockNextRefocus: boolean = false;
  doFocus = false;
  focusStartPX = null;
  focusTopPX = null;

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
    this.zoomMS = 172800000;
    for(var cfg of this.seriesConfigs)
      for(var alt of (cfg.labelAlts ?? []))
        this.labelAlts.push({name: alt.name, label: "Use Label '" + alt.name + "'"});
    if(this.labelAlts.length > 0) this.labelAlts.push({name: null, label:"Use Standard Label"});

    if(this.lanes != null) {
      this.lanesConfig = new GanttLaneConfig(this.lanes, this.userPref);
    }
    this.overlayConfigs = [];
    if(this.overlays != null) {
      for(var item of this.overlays) {
        this.overlayConfigs.push(new GanttOverlayConfig(item, this.userPref));
      }
    }
    if(this.toolbarConfig != null) {
      for(var item of this.toolbarConfig) {
        var context: any = {dataset: this.dataset, datasetgroup: this.datasetgroup};
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
        if(dataset != null && dataset.dataTarget != null && dataset.dataTarget.select != null) {
          target = {
            cfg: cfg, 
            objectname: dataset.objectname, 
            filter: dataset.dataTarget.select, 
            currentlyInSet: dataset.selectedObjects.length > 0
          }
        }
      }
      if(target != null && target.filter != null) {
        this.doFocus = true;
        if(!target.currentlyInSet) {
          this.logService.info("Gantt actiavated with target that is not in the dataset");
          this.dataService.fetchEntireList(target.objectname, target.filter, null, null).subscribe((objs) => {
            if(objs.length > 0) {
              let starts = objs.map(obj => (new Date(obj.get(target.cfg.startAttribute))).getTime());
              let ms = Math.min(...starts);
              this.startDate = new Date(ms - (3*60*60*1000));
              super.onActivationEvent(event);  
            }
          });
        } else {
          super.onActivationEvent(event);
        }
      } else {
        super.onActivationEvent(event);
      }
    } else {
      super.onActivationEvent(event);
    }
  }

  onDatasetEvent(event: any) {
    super.onDatasetEvent(event);
    if(event.dataset.getId() == this.lanesConfig.dataset && event.event != 'select') {
      super.updateData(true)
    } 
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

  get dayMarks() : GanttMark[] {
    return this.marks.filter(m => m.type == GanttMarkType.Day);
  }

  get majorMarks() : GanttMark[] {
    return this.marks.filter(m => m.type == GanttMarkType.Day || m.type == GanttMarkType.Major);
  }

  get isLoading() : boolean {
    return this.dataset != null ? this.dataset.isLoading : this.datasetgroup != null ? this.datasetgroup.isLoading : false;
  }

  get availLabelAlts() : any[] {
    return this.groupOverlaps == false ? this.labelAlts.filter(a => a.name != this.selectedLabelAlt) : [];
  }

  get zooms() : any[] {
    return this._zooms.filter(z => z.val <= this.spanMS);
  }

  get spans() : any[] {
    return this._spans;
  }

  get headerWidth() : number {
    return (0.88 * this._headerwidth);
  }

  setZoom(ms: number) {
    this.zoomMS = ms;
    this.updateData(false);
  }

  setSpan(ms: number) {
    this.spanMS = ms;
    if(this.zoomMS > this.spanMS) this.zoomMS = this.spanMS;
    this.updateData(true);
  }

  toggleDragFilter() {
    this.doDragFilter = !this.doDragFilter;
  }

  toggleGroupOverlaps() {
    this.groupOverlaps = !this.groupOverlaps;
    this.redraw();
  }

  useLabels(name: string) {
    this.selectedLabelAlt = name;
    this.redraw();
  }

  updateOtherData() {
    let fetched = false;
    for(var cfg of this.overlayConfigs) {
      let filterSort = this.getFilterSort(cfg.dataset, cfg.startAttribute, cfg.endAttribute, null);
      if(this.datasetgroup != null) {
        fetched = this.datasetgroup.datasets[cfg.dataset].filterSort(filterSort) || fetched;
      } else {
        fetched = this.dataset.filterSort(filterSort) || fetched;
      }
    }
    return fetched;
  }

  getFilterSortForSeries(cfg: GanttSeriesConfig) : any {
    return this.getFilterSort(cfg.dataset, cfg.startAttribute, cfg.endAttribute, cfg.laneAttribute);
  }

  getFilterSort(datasetId: string, startAttribute: string, endAttribute: string, laneAttribute: string) {
    let dataset = this.datasetgroup != null ? this.datasetgroup.datasets[datasetId] : this.dataset;
    let startDate = this.startDate;
    let endDate = new Date(this.startDate.getTime() + this.spanMS);
    let filter = dataset.userFilter != null ? {...dataset.userFilter} : {};
    if(endAttribute != null) {
      filter[startAttribute] = {
        $lt: "'" + endDate.toISOString() + "'"
      }
      filter[endAttribute] = {
        $gt: "'" + startDate.toISOString() + "'",
      }
    } else {
      filter[startAttribute] = {
        $gt: "'" + startDate.toISOString() + "'",
        $lt: "'" + endDate.toISOString() + "'"
      }
    }
    if(laneAttribute != null && laneAttribute != "uid") {
      let list: RbObject[] = this.lists != null ? this.lists[this.lanesConfig.dataset] : this.list;
      if(list.length == 0) return null;
      filter[laneAttribute] = {$in: list.map(obj => "'" + obj.uid + "'")}
    }
    return {filter:filter};
  }

  calc() {
    this.calcParams();
    this.ganttData = this.getLanes();
    this.overlayData = this.getOverlayLanes();
    this.logService.debug("Gantt " + this.id + ": calc, lanes: " + this.ganttData.length + ", overlays: " + this.overlayData.length);
    this.marks = this.getMarks();
    if(this.doFocus == true) {
      this.focus();
    }
  }

  private calcParams() {
    if(this.spanMS < this.zoomMS) {
      this.spanMS = this.zoomMS;
    }
    this.startMS = this.startDate != null ? this.startDate.getTime() : (new Date()).getTime();
    this.endMS = this.startMS + this.spanMS;
    this.pxPerMS = this.mainscroll.element.nativeElement.offsetWidth / this.zoomMS;
    this.widthPX = this.spanMS * this.pxPerMS;
    this.markMajorIntervalMS = 3600000;
    this.markMinorIntervalMS = 900000;
    while(this.markMajorIntervalMS * this.pxPerMS < 40) {
      this.markMajorIntervalMS *= 2;
      this.markMinorIntervalMS = this.markMajorIntervalMS;
    }
    this.focusStartPX = null;
    this.focusTopPX = null;
  }

  private getLanes() {
    let accHeight = 0;
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
        let sub = null;
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
        if(this.lanesConfig.subAttribute != null) {
          sub = obj.get(this.lanesConfig.subAttribute);
        }        
        let lane = new GanttLane(obj.uid, label, sub, image, icon, obj);
        let spreads: GanttSpread[] = this.getSpreads(obj.uid, accHeight);
        lane.setSpreads(spreads);
        lanes.push(lane);
        accHeight += lane.height + 0.06; //+1 for borders
      }
    }
    this.heightVW = accHeight;
    return lanes;
  }

  private getSpreads(laneId: string, offsetTop: number) : GanttSpread[] {
    let spreads : GanttSpread[] = [];
    let seriesConfigs = this.seriesConfigs.sort((a, b) => b.isBackground && !a.isBackground ? 1 : 0); //Background first
    for(let cfg of seriesConfigs) {
      let dataset = this.datasetgroup != null ? this.datasetgroup.datasets[cfg.dataset] : this.dataset;
      let show = cfg.show == null || (cfg.show != null && cfg.show(null, dataset, dataset.relatedObject));
      if(show) {
        let list: RbObject[] = dataset.list;
        for(var i in list) {
          let obj = list[i];
          if(obj.get(cfg.laneAttribute) == laneId && obj != this.dragService.data) {
            let [startPX, widthPX] = this.getStartAndWidthPX(obj, cfg.startAttribute, cfg.endAttribute, cfg.durationAttribute);
            if(startPX != null && widthPX != null) {
              let label = null;
              if(cfg.labelAttribute != null) {
                label = obj.get(cfg.labelAttribute); 
              } else if(cfg.labelExpression != null) {
                label = Evaluator.eval(cfg.labelExpression, obj, null, null);
              }
              if(this.selectedLabelAlt != null && cfg.labelAlts != null) {
                let alt = cfg.labelAlts.find(a => a.name == this.selectedLabelAlt);
                if(alt != null) {
                  if(alt.attribute != null) {
                    label = obj.get(alt.attribute);
                  } else if(alt.expression != null) {
                    label = Evaluator.eval(alt.expression, obj, null, null);
                  }
                }
              }
              let color = null;
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
              } else if(cfg.colorExpression != null) {
                color = Evaluator.eval(cfg.colorExpression, obj, null, null);
              }
              if(color == null) {
                color = cfg.isBackground ? 'white' : 'var(--primary-light-color)';
              }
              let labelcolor = "#333";
              if(cfg.labelColor != null) {
                labelcolor = cfg.labelColor;
              }
              let canEdit: boolean = cfg.canEdit && (obj.canEdit(cfg.startAttribute) || obj.canEdit(cfg.laneAttribute));
              let sublane = 0;
              if(!cfg.isBackground) {
                let hasOverlap: boolean;
                do {
                  hasOverlap = false;
                  for(var os of spreads) {
                    if(!os.config.isBackground && startPX < os.start + os.width && os.start < startPX + widthPX && sublane == os.sublane) {
                      hasOverlap = true;
                    }
                  }
                  if(hasOverlap) sublane++;
                } while(hasOverlap);
              }
              let selected: boolean = cfg.isBackground == false && dataset.isObjectSelected(obj);
              let spread = new GanttSpread(laneId, label, startPX, widthPX, offsetTop, sublane, color, labelcolor, canEdit, selected, obj, cfg);
              spreads.push(spread);
              if(selected) {
                let fs = startPX;
                if(this.focusStartPX == null || (this.focusStartPX != null && fs < this.focusStartPX)) this.focusStartPX = fs;
                let topVW = offsetTop + spread.laneTop;
                let ft = (topVW * document.documentElement.clientWidth / 100);
                if(this.focusTopPX == null || (this.focusTopPX != null && ft < this.focusTopPX)) this.focusTopPX = ft;
              }
            }
          }
        }
      }
    }
    if(this.groupOverlaps) {
      spreads = this.groupOverlappingSpreads(spreads);
    } 
    return spreads;
  }

  private groupOverlappingSpreads(spreads) {
    let nextid = 0;
    let out = [];
    let groups = [];
    let groupmap = {};
    for(var spread of spreads) {
      if(spread.config.isBackground == false) {
        let firstGroupFound = null;
        for(var i = 0; i < groups.length; i++) {
          var group = groups[i];
          if(spread.config == group.config && spread.start < group.start + group.width && group.start < spread.start + spread.width) {
            if(firstGroupFound == null) {
              firstGroupFound = group;
              let newstart = Math.min(group.start, spread.start);
              let newend = Math.max(group.start + group.width, spread.start + spread.width);
              group.start = newstart;
              group.width = newend - newstart;
              groupmap[group.id] = groupmap[group.id] + 1;
              group.label = groupmap[group.id] + " items";
            } else {
              let newstart = Math.min(group.start, firstGroupFound.start);
              let newend = Math.max(group.start + group.width, firstGroupFound.start + firstGroupFound.width);
              firstGroupFound.start = newstart;
              firstGroupFound.width = newend - newstart;
              groupmap[firstGroupFound.id] = groupmap[firstGroupFound.id] + groupmap[group.id];
              firstGroupFound.label = groupmap[firstGroupFound.id] + " items";
              groups.splice(i, 1);
              i--;
            }
          }
        }
        if(firstGroupFound == null) {
          let id = nextid++;
          let group = new GanttSpread(spread.laneId, "1 item", spread.start, spread.width, 0, 0, 'var(--primary-light-color)', '#333', false, false, null, spread.config);
          group.id = id.toString();
          groupmap[group.id] = 1
          groups.push(group);
        }  
      } else {
        out.push(spread);
      }
    }
    groups.forEach(g => out.push(g));
    return out;
  }

  private getOverlayLanes() {
    let lanes : GanttOverlayLane[] = [];
    for(var cfg of this.overlayConfigs) {
      let dataset = this.datasetgroup != null ? this.datasetgroup.datasets[cfg.dataset] : this.dataset;
      if(dataset != null) {
        let spreads: GanttOverlaySpread[] = [];
        for(var obj of dataset.list) {
          let [startPX, widthPX] = this.getStartAndWidthPX(obj, cfg.startAttribute, cfg.endAttribute, cfg.durationAttribute);
          if(startPX != null && widthPX != null) {
            let color = 'var(--primary-light-color)';
            if(cfg.color != null) {
              color = cfg.color;
            } 
            let selected: boolean = false;
            if(color != null) {
              let spread = new GanttOverlaySpread(null, startPX, widthPX, color, obj, cfg);
              spreads.push(spread);
            }
          }
        }
        if(spreads.length > 0) {
          let label = spreads[0].object.get(cfg.labelAttribute);
          let lane = new GanttOverlayLane(null, label);
          lane.setSpreads(spreads);
          lanes.push(lane);
        }
      }
    }
    return lanes;
  }

  private getStartAndWidthPX(obj: RbObject, startAttribute: string, endAttribute: string, durationAttribute: string) {
    let startMS = (new Date(obj.get(startAttribute))).getTime();
    let startPX: number = Math.round((startMS - this.startMS) * this.pxPerMS);
    if(startPX < this.widthPX) {
      let durationMS = this.getObjectDuration(obj, startAttribute, endAttribute, durationAttribute);
      if(startMS + durationMS > this.endMS) {
        durationMS = this.endMS - startMS;
      }
      let endMS = startMS + durationMS;
      let endPX = Math.round((endMS - this.startMS) * this.pxPerMS);
      let widthPX = endPX - startPX;
      if(startPX > -widthPX) {
        if(startPX < 0) {
          widthPX = widthPX + startPX;
          startPX = 0;
        }
        return [startPX, widthPX];
      } else {
        return [null, null];
      }
    } else {
      return [null, null];
    }
  }

  private getObjectDuration(obj: RbObject, startAttribute: string, endAttribute: string, durationAttribute: string): number {
    let durationMS = null;
    if(durationAttribute != null) {
      durationMS = parseInt(obj.get(durationAttribute));
    } else if(endAttribute != null) {
      durationMS = (new Date(obj.get(endAttribute))).getTime() - (new Date(obj.get(startAttribute))).getTime();
    } 
    if(durationMS == null || isNaN(durationMS)) durationMS = 3600000;
    return durationMS;
  }

  private getMarks() : GanttMark[] {
    let marks: GanttMark[] = [];
    let lastMidnight = (new Date(this.startMS));
    lastMidnight.setHours(0);
    lastMidnight.setMinutes(0);
    lastMidnight.setSeconds(0);
    lastMidnight.setMilliseconds(0);
    let lastMidnightMS = lastMidnight.getTime();
    let cur = lastMidnightMS;
    while(cur < this.endMS) {
      let curDate: Date = new Date(cur);
      let sinceFirstMidnight = cur - lastMidnightMS;
      let pos = Math.round((cur - this.startMS) * this.pxPerMS);
      if(pos >= 0) {
        let dayLabel: string = this.dayNames[curDate.getDay()] + ", " + curDate.getDate() + " " + this.monthNames[curDate.getMonth()] + " " + curDate.getFullYear();
        let timeLabel: string = curDate.getHours() + ":00";
        let type: GanttMarkType = sinceFirstMidnight % 86400000 == 0 ? GanttMarkType.Day : sinceFirstMidnight % this.markMajorIntervalMS == 0 ? GanttMarkType.Major : GanttMarkType.Minor;
        marks.push(new GanttMark(pos, dayLabel, timeLabel, type));
      }
      cur = cur + this.markMinorIntervalMS;
    }
    return marks;
  }

  private focus() {
    if(this.focusStartPX != null && this.focusTopPX != null) {
      this.mainscroll.scrollToHPos(Math.max(0, this.focusStartPX - 30));
      this.mainscroll.scrollToVPos(Math.max(0, this.focusTopPX - 30));
      this.doFocus = false;
    }
  }

  public selectedOverlaySpreads() : GanttOverlaySpread[] {
    return this.selectedOverlayLaneIndex > -1 && this.selectedOverlayLaneIndex < this.overlayData.length ? this.overlayData[this.selectedOverlayLaneIndex].spreads : []
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

  public clickBackground() {
    for(let cfg of this.seriesConfigs) {
        let dataset = this.datasetgroup != null ? this.datasetgroup.datasets[cfg.dataset] : this.dataset;
        dataset.clearSelection();
    }
  }

  public clickOverlayLaneIndex(i: number) {
    if(this.selectedOverlayLaneIndex == -1 || this.selectedOverlayLaneIndex != i) {
      this.selectedOverlayLaneIndex = i;
    } else {
      this.selectedOverlayLaneIndex = -1;
    }
  }

  public droppedOn(event: any, lane: GanttLane, ignoreTime: boolean = false) {
    let update: any = {};
    let related: any = {}
    let object: RbObject = event.data;
    let config: GanttSeriesConfig = this.getSeriesConfigForObject(object);

    if(ignoreTime == false) {
      let previousStart = object.get(config.startAttribute);
      let previousDuration = this.getObjectDuration(object, config.startAttribute, config.endAttribute, config.durationAttribute);
      let tgt = event.mouseEvent.target;
      let left = event.mouseEvent.offsetX - event.offset.x;
      while(tgt.className.indexOf("rb-gantt-lane") == -1) {
        left = left + tgt.offsetLeft;
        tgt = tgt.offsetParent;
      }
      let newStartMS = this.startMS + (left / this.pxPerMS);
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
    
    if(Object.keys(update).length > 0) {
      object.setValuesAndRelated(update, related);
    }

    let targetDataset = this.datasetgroup != null ? this.datasetgroup.datasets[config.dataset] : this.dataset;
    if(targetDataset != null && targetDataset.list.indexOf(object) == -1) {
      targetDataset.add(object);
    }
  }

  public droppedOut(event: any) {
    let object: RbObject = event.data;
    let config: GanttSeriesConfig = this.getSeriesConfigForObject(object);
    let update: any = {};
    update[config.startAttribute] = null;
    update[config.laneAttribute] = null;
    object.setValues(update);
  }

  public onScroll(event) {
    this.scrollLeft = event.target.scrollLeft;
    this.scrollTop = event.target.scrollTop;
  }

  public getSizeForObject(obj: RbObject) : any {
    let cfg: GanttSeriesConfig = this.getSeriesConfigForObject(obj);
    let durationMS = this.getObjectDuration(obj, cfg.startAttribute, cfg.endAttribute, cfg.durationAttribute);
    return {
      x: Math.round(durationMS * this.pxPerMS),
      y: window.innerWidth * GanttSpreadHeight / 100
    };
  }
}
