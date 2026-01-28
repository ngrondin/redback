import { Component, OnInit, Input, SimpleChange, Output, EventEmitter, ViewChild, ViewContainerRef, Injector } from '@angular/core';
import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { RbObject, RbObjectTransaction, RELATED_LOADING, XY } from 'app/datamodel';
import { BuildService } from 'app/services/build.service';
import { DragService } from 'app/services/drag.service';
import { FilterService } from 'app/services/filter.service';
import { ModalService } from 'app/services/modal.service';
import { UserprefService } from 'app/services/userpref.service';
import { Subscription } from 'rxjs';
import { GanttDependencyType, GanttLane, GanttLaneConfig, GanttMark, GanttMarkType, GanttOverlayConfig, GanttOverlayLane, GanttOverlaySpread, GanttSeriesConfig, GanttSpread, GanttTimeBasedConfig } from './rb-gantt-models';
import { RbScrollComponent } from 'app/rb-scroll/rb-scroll.component';
import { DataService } from 'app/services/data.service';
import { LogService } from 'app/services/log.service';
import { CanvasTool, Evaluator, ValueComparator } from 'app/helpers';
import { NavigateService } from 'app/services/navigate.service';


@Component({
  selector: 'rb-gantt',
  templateUrl: './rb-gantt.component.html',
  styleUrls: ['./rb-gantt.component.css']
})
export class RbGanttComponent extends RbDataCalcComponent<GanttSeriesConfig> {
  @Input('lanes') lanesInput : any;
  @Input('overlays') overlaysInput: any[];
  @Input('layers') layers: any[];
  @Input('toolbar') toolbarConfig : any;
  @Input('locktonow') locktonow: boolean = false;
  @Input('allowpastdrop') allowpastdrop: boolean = true;
  @Input('allowoverlapgroup') allowoverlapgroup: boolean = false;
  @Input('headerwidth') _headerwidth: number = 17;
  @Input('startvariable') startVariable: string = null;
  @Input('spanvariable') spanVariable: string = null;
  @Input('zoomvariable') zoomVariable: string = null;
  @Input('emptymessage') emptyMessage: string = null;
  @ViewChild('customtoolbar', { read: ViewContainerRef, static: true }) toolbar: ViewContainerRef;
  @ViewChild('mainscroll') mainscroll: RbScrollComponent;
  @ViewChild('canvas', { read: ViewContainerRef, static: true }) canvas: ViewContainerRef;
  
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
  heightPX: number;
  spreadHeightPX: number;
  spreadMarginPX: number;
  doDragFilter: boolean = false;
  groupOverlaps: boolean = false;
  overrideAllowPastDrop: boolean = false;
  scrollTop: number;
  scrollLeft: number;
  monthNames: String[] = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
  dayNames: String[] = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
  _zooms: any[] = [{label:"12 Hours", val:43200000}, {label:"1 Day", val:86400000}, {label:"2 Days", val:172800000}, {label:"3 Days", val:259200000}, {label:"7 Days", val:604800000}];
  _spans: any[] = [{label:"12 Hours", val:43200000}, {label:"1 Day", val:86400000}, {label:"3 Days", val:259200000}, {label:"7 Days", val:604800000}, {label:"14 Days", val:1209600000}];
  labelAlts: any[] = [];
  lanes: GanttLane[];
  spreads: GanttSpread[];
  marks: GanttMark[] = [];
  overlays: GanttOverlayLane[];
  selectedOverlayLaneIndex = -1;
  selectedLabelAlt: string = null;
  spreadMap: any = {};

  dragSelecting: boolean = false;
  dragSelectStart: XY = null;
  dragSelectTopLeft: XY = null;
  dragSelectSize: XY = null;

  doFocus = false;
  showEmptyLanes = true;
  focusStartPX = null;
  focusTopPX = null;

  public getDragSizeForObjectCallback: Function;
  public droppedOutCallback: Function;
  public enhanceDragDataCallback: Function;
  dragSubscription: Subscription;

  //canvas = null
  graphctx = null;
  
  constructor(
    private modalService: ModalService,
    private navigateService: NavigateService,
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
    this.getDragSizeForObjectCallback = this.getDragSizeForObject.bind(this);
    this.droppedOutCallback = this.droppedOut.bind(this);
    this.enhanceDragDataCallback = this.enhanceDragData.bind(this);
    this.spanMS = 259200000;
    this.zoomMS = 172800000;
    for(var cfg of this.seriesConfigs)
      for(var alt of (cfg.labelAlts ?? []))
        this.labelAlts.push({name: alt.name, label: "Use Label '" + alt.name + "'"});
    if(this.labelAlts.length > 0) this.labelAlts.push({name: null, label:"Use Standard Label"});

    if(this.lanesInput != null) {
      this.lanesConfig = new GanttLaneConfig(this.lanesInput, this.userPref);
    }
    this.overlayConfigs = [];
    if(this.overlaysInput != null) {
      for(var item of this.overlaysInput) {
        this.overlayConfigs.push(new GanttOverlayConfig(item, this.userPref));
      }
    }
    if(this.toolbarConfig != null) {
      for(var item of this.toolbarConfig) {
        var context: any = {dataset: this.dataset, datasetgroup: this.datasetgroup};
        this.buildService.buildConfigRecursive(this.toolbar, item, context);
      }
    }
    this.graphctx = this.canvas.element.nativeElement.getContext("2d");
  }

  dataCalcDestroy() {

  }

  createSeriesConfig(json: any): GanttSeriesConfig {
    return new GanttSeriesConfig(json, this.userPref);
  }

  onActivationEvent(event: any) {
    if(this.active) {
      this.logService.debug("Gantt " + this.id + ": Activation (" + this.active + ")");
      this.scrollTop = 0;
      this.scrollLeft = 0;
      if(this.startVariable != null) {
        let dtvar = window.redback[this.startVariable];
        this._startDate = typeof dtvar == 'string' ? new Date(dtvar) : dtvar.getTime != null ? dtvar : this._startDate;
      }
      if(this.spanVariable != null) this.spanMS = window.redback[this.spanVariable];
      if(this.zoomVariable != null) this.zoomMS = Math.min(this.spanMS, window.redback[this.zoomVariable]);
      let selectionTarget = null;
      for(let cfg of this.seriesConfigs) {
        let dataset = this.getDatasetForConfig(cfg);
        //Retrieve the start of the Gantt from any of the datasets
        if(cfg.timeFromAttributes && dataset.resolvedFilter != null) {
          let start = null;
          let end = null;
          if(cfg.end != null) {
            start = dataset.resolvedFilter[cfg.end.attribute]?.['$gt'];
            end = dataset.resolvedFilter[cfg.start.attribute]?.['$lt'];
          } else {
            start = dataset.resolvedFilter[cfg.start.attribute]?.['$gt'];
            end = dataset.resolvedFilter[cfg.start.attribute]?.['$lt'];
          } 
          if(start != null && end != null) {
            this._startDate = new Date(start);
            this.spanMS = ((new Date(end)).getTime() - this._startDate.getTime());
            if(this.zoomMS > this.spanMS) this.zoomMS = this.spanMS;
            console.log("Retrieved the Gantt start: " + start + "  -  " + end + ", span: " + this.spanMS + ", zoom: " + this.zoomMS);
          }
        }  
        //Retrieve the selection target from any of the datasets
        if(dataset != null && dataset.dataTarget != null && dataset.dataTarget.select != null) {
          selectionTarget = {
            cfg: cfg, 
            objectname: dataset.objectname, 
            filter: dataset.dataTarget.select, 
            currentlyInSet: dataset.selectedObjects.length > 0
          }
        }
      }
      if(selectionTarget != null && selectionTarget.filter != null) {
        this.doFocus = true;
        if(!selectionTarget.currentlyInSet) {
          this.logService.info("Gantt actiavated with target that is not in the dataset");
          this.dataService.fetchEntireList(selectionTarget.objectname, selectionTarget.filter, null, null).subscribe((objs) => {
            if(objs.length > 0) {
              let starts = objs.map(obj => (new Date(obj.get(selectionTarget.cfg.start.attribute))).getTime());
              let ms = Math.min(...starts);
              this._startDate = new Date(ms - (3*60*60*1000));
              this.logService.info("Setting start date to " + this._startDate);
              this.updateData(true);
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
    if(event.event == 'select') {
      this.drawCanvas();
    } else {
      super.onDatasetEvent(event);
    }
    if(this.active && event.dataset.getId() == this.lanesConfig.dataset && event.event == 'load') {
      super.updateData(true)
    } 
  }

  onDragEvent(event: any) {
    if(this.active) {
      this.logService.debug("Gantt " + this.id + ": DragEvent ( event: " +event.type + ", dragitems: " + (Array.isArray(event.data) ? event.data.length : event.data != null ? 1 : 0) + ")");
      if(event.type == 'start' && !this.doDragFilter) {
        for(var obj of (Array.isArray(event.data) ? event.data : [event.data])) {
          if(obj != null && obj instanceof RbObject) {
            var spread = this.spreadMap[`${obj.objectname}.${obj.uid}`];
            if(spread != null) spread.dragging = true;          
          }
        }
      }
      if(this.doDragFilter || event.type == 'end') {
        this.redraw();
      } else {
        this.drawCanvas();
      }
    }
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
    if(this.startVariable != null) window.redback[this.startVariable] = this._startDate.toISOString();
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

  get headerWidth() : string {
    return 'min(' + (0.88 * this._headerwidth) + 'vw, ' + (17 * this._headerwidth) + 'px)';
  }

  get extraContext() : any {
    return {
      ganttstart: this.startDate.toISOString(), 
      ganttend: (new Date(this.startDate.getTime() + this.spanMS)).toISOString()
    };
  }

  get isEmpty() : boolean {
    return !(this.lanes != null && this.lanes.length > 0)
  }

  setZoom(ms: number) {
    this.zoomMS = ms;
    if(this.zoomVariable != null) window.redback[this.zoomVariable] = this.zoomMS;
    this.updateData(false);
  }

  setSpan(ms: number) {
    this.spanMS = ms;
    if(this.spanVariable != null) window.redback[this.spanVariable] = this.spanMS;
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

  toggleHideEmptyLanes() {
    this.showEmptyLanes = !this.showEmptyLanes;
    this.redraw();
  }

  toggleOverridePastDrop() {
    this.overrideAllowPastDrop = !this.overrideAllowPastDrop;
  }

  useLabels(name: string) {
    this.selectedLabelAlt = name;
    this.redraw();
  }

  updateOtherData() {
    let fetched = false;
    for(var cfg of this.overlayConfigs) {
      if(cfg.timeFromAttributes) {
        let startAttribute = cfg.applyDateFilter ? cfg.start.attribute : null;
        let endAttribute = cfg.applyDateFilter ? cfg.end.attribute : null;
        let filterSort = this.getFilterSort(cfg.dataset, startAttribute, endAttribute, null);
        if(this.datasetgroup != null) {
          fetched = this.datasetgroup.datasets[cfg.dataset].filterSort(filterSort) || fetched;
        } else {
          fetched = this.dataset.filterSort(filterSort) || fetched;
        }  
      }
    }
    return fetched;
  }

  getFilterSortForSeries(cfg: GanttSeriesConfig) : any {
    let startAttribute = cfg.applyDateFilter ? cfg.start.attribute : null;
    let endAttribute = cfg.applyDateFilter ? cfg.end?.attribute : null;
    let laneAttributes = cfg.applyLaneFilter ? cfg.laneAttributes : null;
    return this.getFilterSort(cfg.dataset, startAttribute, endAttribute, laneAttributes);
  }

  getFilterSort(datasetId: string, startAttribute: string, endAttribute: string, laneAttributes: string[]) {
    let dataset = this.datasetgroup != null ? this.datasetgroup.datasets[datasetId] : this.dataset;
    let startDate = this.startDate;
    let endDate = new Date(this.startDate.getTime() + this.spanMS);
    let filter = dataset.userFilter != null ? {...dataset.userFilter} : {};
    if(startAttribute != null) {
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
    }
    if(laneAttributes != null && !(laneAttributes.length == 1 && laneAttributes[0] == 'uid')) {
      let list: RbObject[] = this.lists != null ? this.lists[this.lanesConfig.dataset] : this.list;
      if(laneAttributes.length == 1) {
        filter[laneAttributes[0]] = {$in: list.map(obj => "'" + obj.get(this.lanesConfig.linkAttributes[0]) + "'")}
      } else {
        var orlist = [];
        for(var obj of list) {
          var clause = {};
          for(var i = 0; i < laneAttributes.length; i++) {
            clause[laneAttributes[i]] = "'" + obj.get(this.lanesConfig.linkAttributes[i]) + "'";
          }
          orlist.push(clause);
        }
        filter['$or'] = orlist;
      }
    }
    return {filter:filter};
  }

  calc() {
    this.calcParams();
    this.calcLanes();
    this.calcDependencies();
    this.calcOverlayLanes();
    this.calcMarks();
    this.logService.debug("Gantt " + this.id + ": calc, lanes: " + this.lanes.length + ", overlays: " + this.overlays.length + ", spreads: " + this.spreads.length + ", dragging: " + this.spreads.filter(s => s.dragging == true).length);
    if(this.canvas.element.nativeElement.width != this.widthPX || this.canvas.element.nativeElement.height != this.heightPX) {
      this.canvas.element.nativeElement.width = this.widthPX;
      this.canvas.element.nativeElement.height = this.heightPX;
    }
    this.drawCanvas();
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
    let clientWidthPX = this.mainscroll != null ? this.mainscroll.element.nativeElement.offsetWidth : 1920;
    this.pxPerMS = clientWidthPX / this.zoomMS
    this.widthPX = this.spanMS * this.pxPerMS;
    this.spreadHeightPX = Math.min(0.0167 * window.innerWidth, 32);
    this.spreadMarginPX = Math.min(0.0041 * window.innerWidth, 8);
    this.markMajorIntervalMS = 3600000;
    this.markMinorIntervalMS = 900000;
    while(this.markMajorIntervalMS * this.pxPerMS < 50) {
      this.markMajorIntervalMS *= 2;
      this.markMinorIntervalMS = this.markMajorIntervalMS;
    }
    this.focusStartPX = null;
    this.focusTopPX = null;
  }

  private calcLanes() {
    let accHeight = 0;
    let laneHeight = this.spreadHeightPX + (2*this.spreadMarginPX);
    this.lanes = [];
    this.spreads = [];
    this.spreadMap = {};
    let laneFilter: any = null;
    if(this.doDragFilter && this.dragService.isDragging && this.lanesConfig.dragfilter != null) {
      let draggingObject = Array.isArray(this.dragService.data) ? this.dragService.data[0] : this.dragService.data;
      laneFilter = this.filterService.resolveFilter(this.lanesConfig.dragfilter, draggingObject, null, null);
    };
    let list: RbObject[] = this.lists != null ? this.lists[this.lanesConfig.dataset] : this.list;
    if(this.lanesConfig.orderAttribute != null) {
      list.sort((a, b) => ValueComparator.sort(a.get(this.lanesConfig.orderAttribute), b.get(this.lanesConfig.orderAttribute)));
    }
    for(let obj of list) {
      let show = laneFilter != null && !this.filterService.applies(laneFilter, obj) ? false : true;
      if(show) {     
        let lane = new GanttLane(obj, this.lanesConfig, this.spreadHeightPX, this.spreadMarginPX);
        let spreads: GanttSpread[] = this.calcSpreads(lane.linkValues, accHeight);
        if(this.showEmptyLanes || spreads.filter(s => !s.ghost && !s.config.isBackground).length > 0) {
          lane.setSpreads(spreads);
          this.lanes.push(lane);
          accHeight += lane.height + 1; 
        }
      }
    }
    this.heightPX = accHeight;
  }

  private calcSpreads(laneValues: string[], offsetTop: number) : GanttSpread[] {
    let laneSpreads : GanttSpread[] = [];
    for(let cfg of this.seriesConfigs) {
      let dataset = this.getDatasetForConfig(cfg);
      let show = cfg.show == null || (cfg.show != null && cfg.show(null, dataset, dataset.relatedObject));
      if(show) {
        let list: RbObject[] = dataset.list;
        for(var i in list) {
          let obj = list[i];
          let objLinkValues = cfg.laneAttributes.map(la => obj.get(la));
          if(this.linkValuesMatch(objLinkValues, laneValues)) {
            let [startPX, widthPX] = this.getStartAndWidthPX(obj, cfg);
            if(startPX != null && widthPX != null) {
              let label = null;
              let loading = false;
              if(cfg.labelAttribute != null) {
                label = obj.get(cfg.labelAttribute); 
              } else if(cfg.labelExpression != null) {
                label = Evaluator.eval(cfg.labelExpression, obj, null, null);
              }
              if(label == RELATED_LOADING) {
                label = "...";
                loading = true;
              }
              let labelWidth = this.graphctx.measureText(label).width;
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
              let color = cfg.color?.getColor(obj) || (cfg.isBackground ? 'white' : 'var(--primary-light-color)');
              let labelcolor = "#333";
              if(cfg.labelColor != null) {
                labelcolor = cfg.labelColor;
              }
              let sublane = 0;
              if(!cfg.isBackground) {
                let hasOverlap: boolean;
                do {
                  hasOverlap = false;
                  for(var os of laneSpreads) {
                    if(!os.config.isBackground && startPX < os.start + os.width && os.start < startPX + widthPX && sublane == os.sublane) {
                      hasOverlap = true;
                    }
                  }
                  if(hasOverlap) sublane++;
                } while(hasOverlap);
              }
              let indicator: boolean = false;
              if(cfg.indicatorAttribute != null) {
                indicator = obj.get(cfg.indicatorAttribute); 
              } else if(cfg.indicatorExpression != null) {
                indicator = Evaluator.eval(cfg.indicatorExpression, obj, null, null);
              }
              let spread = new GanttSpread(label, startPX, widthPX, this.spreadHeightPX, this.spreadMarginPX, offsetTop, sublane, color, labelcolor, obj, dataset, cfg);
              spread.indicator = indicator;
              spread.tip = labelWidth > widthPX ? label : null;
              spread.dragging = this.isObjectDragging(obj);
              laneSpreads.push(spread);
              this.spreads.push(spread);
              this.spreadMap[`${obj.objectname}.${obj.uid}`] = spread;
              if(spread.selected) {
                if(this.focusStartPX == null || (this.focusStartPX != null && startPX < this.focusStartPX)) this.focusStartPX = startPX;
                let topPX = offsetTop + spread.laneTop;
                if(this.focusTopPX == null || (this.focusTopPX != null && topPX < this.focusTopPX)) this.focusTopPX = topPX;
              }
            }
          }
        }
      }
    }
    if(this.groupOverlaps) {
      laneSpreads = this.groupOverlappingSpreads(laneSpreads);
    } 
    return laneSpreads;
  }

  private calcDependencies() {
    for(var spread of this.spreads) {
      let deps = spread.config.dependencyAttribute != null ? spread.object.get(spread.config.dependencyAttribute) : null;
      if(deps != null) {
        for(var uid of Object.keys(deps)) {
          let depSpread = this.spreadMap[`${spread.object.objectname}.${uid}`];
          if(depSpread != null) {
            let rt = deps[uid];
            if(rt == 'DU') {
              spread.dependencies.push({spread: depSpread, type: GanttDependencyType.DU});
              depSpread.dependencies.push({spread: spread, type: GanttDependencyType.DU});
            } else if(rt == 'AF') {
              spread.dependencies.push({spread: depSpread, type: GanttDependencyType.SF});
              depSpread.dependencies.push({spread: spread, type: GanttDependencyType.FS});
            } else if(rt == 'SS') {
              spread.dependencies.push({spread: depSpread, type: GanttDependencyType.SS});
              depSpread.dependencies.push({spread: spread, type: GanttDependencyType.SS});
            }
          }
        }
      }
    }
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
          let group = new GanttSpread("1 item", spread.start, spread.width, this.spreadHeightPX, this.spreadMarginPX, 0, 0, 'var(--primary-light-color)', '#333', null, null, spread.config);
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

  private calcOverlayLanes() {
    this.overlays = [];
    for(var cfg of this.overlayConfigs) {
      let dataset = this.datasetgroup != null ? this.datasetgroup.datasets[cfg.dataset] : this.dataset;
      if(dataset != null) {
        let spreads: GanttOverlaySpread[] = [];
        for(var obj of dataset.list) {
          let [startPX, widthPX] = this.getStartAndWidthPX(obj, cfg);
          if(startPX != null && widthPX != null) {
            let color = cfg.color?.getColor(obj) || 'var(--primary-light-color)';
            if(color != null) {
              let spread = new GanttOverlaySpread(null, startPX, widthPX, color, obj, cfg);
              spreads.push(spread);
            }
          }
        }
        if(spreads.length > 0) {
          let label = cfg.label != null ? cfg.label : (cfg.labelAttribute != null ? spreads[0].object.get(cfg.labelAttribute) : null);
          let lane = new GanttOverlayLane(null, label, this.spreadHeightPX);
          lane.setSpreads(spreads);
          this.overlays.push(lane);
        }
      }
    }
  }

  private calcMarks() {
    this.marks = [];
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
        this.marks.push(new GanttMark(pos, dayLabel, timeLabel, type));
      }
      cur = cur + this.markMinorIntervalMS;
    }
    let nowPos = Math.round((new Date().getTime() - this.startMS) * this.pxPerMS);
    if(nowPos > 0 && nowPos < this.spanMS) {
      this.marks.push(new GanttMark(nowPos, null, null, GanttMarkType.Now));
    }
  }

  drawCanvas() {
    var selectedSpreads = this.spreads.filter(s => s.selected);
    this.graphctx.clearRect(0, 0, this.canvas.element.nativeElement.width, this.canvas.element.nativeElement.height);
    if(this.dragService.isDragging == false && selectedSpreads.length == 1) {
      let spread = selectedSpreads[0];
      for(let dep of spread.dependencies) {
        let startdir = dep.type == GanttDependencyType.FS ? 'right' : dep.type == GanttDependencyType.SF ? 'left' : dep.type == GanttDependencyType.SS ? 'left' : spread.offsetTop + spread.laneTop < dep.spread.offsetTop + dep.spread.laneTop ? 'bottom' : 'top';
        let enddir = dep.type == GanttDependencyType.FS ? 'left' : dep.type == GanttDependencyType.SF ? 'right' : dep.type == GanttDependencyType.SS ? 'left' : spread.offsetTop + spread.laneTop < dep.spread.offsetTop + dep.spread.laneTop ? 'top' : 'bottom';
        let startx = spread.start + (startdir == 'left' ? 0 : startdir == 'right' ? spread.width : spread.width / 2);
        let starty = spread.offsetTop + spread.laneTop + (startdir == 'top' ? 0 : startdir == 'bottom' ? this.spreadHeightPX : this.spreadHeightPX / 2);
        let endx = dep.spread.start + (enddir == 'left' ? 0 : enddir == 'right' ? dep.spread.width : dep.spread.width / 2);
        let endy = dep.spread.offsetTop + dep.spread.laneTop + (enddir == 'top' ? 0 : enddir == 'bottom' ? this.spreadHeightPX : this.spreadHeightPX / 2);
        CanvasTool.drawPolyline(this.graphctx, startx, starty, startdir, endx, endy, enddir);
      }
    }
  }

  private focus() {
    if(this.focusStartPX != null && this.focusTopPX != null) {
      this.mainscroll.scrollToHPos(Math.max(0, this.focusStartPX - 30));
      this.mainscroll.scrollToVPos(Math.max(0, this.focusTopPX - 30));
      this.doFocus = false;
    }
  }

  public selectedOverlaySpreads() : GanttOverlaySpread[] {
    return this.selectedOverlayLaneIndex > -1 && this.selectedOverlayLaneIndex < this.overlays.length ? this.overlays[this.selectedOverlayLaneIndex].spreads : []
  }

  public clearSelection() {
    for(var cfg of this.seriesConfigs) {
      let dataset = this.getDatasetForConfig(cfg);
      dataset.clearSelection();
    }
  }

  public select(object: RbObject) {
    let dataset = this.getDatasetForObject(object);
    dataset.select(object);
  }

  public addOneToSelection(object: RbObject) {
    let dataset = this.getDatasetForObject(object);
    dataset.addOneToSelection(object);
  }

  public selectArea(start: number, end: number, top: number, bottom: number) {
    this.clearSelection();
    for(var lane of this.lanes) {
      for(var s of lane.foregroundSpreads()) {
        let stop = s.offsetTop + s.laneTop;
        let sbot = stop + s.height;
        if(s.start > start && (s.start + s.width) < end && stop > top && sbot < bottom) {
          this.addOneToSelection(s.object);
        }
      }
    }
  }

  public clickSpread(spread: GanttSpread, event: any) {
    if(event.ctrlKey == true || event.metaKey == true) {
      this.addOneToSelection(spread.object);
    } else if(event.shiftKey == true) {
      this.addOneToSelection(spread.object);
    } else {
      this.select(spread.object);
      if(spread.config.modal != null) {
        this.modalService.open(spread.config.modal);
      } else if(spread.config.link != null) {
        let navEvent = this.lanesConfig.link.getNavigationEvent(spread.object, this.getDatasetForObject(spread.object), this.extraContext);
        this.navigateService.navigateTo(navEvent);
      } 
    }
  }

  public clickLane(lane: GanttLane) {
    this.select(lane.object);
    if(this.lanesConfig.modal != null) {
      this.modalService.open(this.lanesConfig.modal);
    } else if(this.lanesConfig.link != null) {
      let navEvent = this.lanesConfig.link.getNavigationEvent(lane.object, this.getDatasetForObject(lane.object), this.extraContext);
      this.navigateService.navigateTo(navEvent);
    }  
  }

  public clickBackground() {

  }

  public clickOverlayLaneIndex(i: number) {
    if(this.selectedOverlayLaneIndex == -1 || this.selectedOverlayLaneIndex != i) {
      this.selectedOverlayLaneIndex = i;
    } else {
      this.selectedOverlayLaneIndex = -1;
    }
  }


  public mouseDownBackground(event: any) {
    if(event.button == 0) {
      this.clearSelection();
      this.dragSelecting = true;
      this.dragSelectStart = this.getXYRelativeToTarget(event, "rb-gantt-lanes");
      this.dragSelectTopLeft = this.dragSelectStart;
      this.dragSelectSize = new XY(0, 0);
    }
  }

  public mouseMoveBackground(event: any) {
    if(this.dragSelecting) {
      let pos = this.getXYRelativeToTarget(event, "rb-gantt-lanes");
      this.dragSelectTopLeft = new XY(Math.min(pos.x, this.dragSelectStart.x), Math.min(pos.y, this.dragSelectStart.y));
      this.dragSelectSize = new XY(Math.abs(pos.x - this.dragSelectStart.x), Math.abs(pos.y - this.dragSelectStart.y));
    }
  }

  public mouseUpBackground(event: any) {
    if(this.dragSelecting && this.dragSelectSize.x > 0 && this.dragSelectSize.y > 0) {
      let start = this.dragSelectTopLeft.x;
      let end = this.dragSelectTopLeft.x + this.dragSelectSize.x;
      let top = this.dragSelectTopLeft.y;
      let bottom = this.dragSelectTopLeft.y + this.dragSelectSize.y;
      this.selectArea(start, end, top, bottom);
      event.stopPropagation();
    }
    this.dragSelecting = false;
  }

  public enhanceDragData(object: RbObject) : any {
    let ret: RbObject | RbObject[] = object;
    let dataset = this.getDatasetForObject(object);
    if(dataset != null && dataset.selectedObjects.length > 0 && dataset.selectedObjects.includes(object)) {
      ret = [object].concat(dataset.selectedObjects.filter(o => o.uid != object.uid));
    } 
    return ret;
  }

  public droppedOn(event: any, lane: GanttLane, ignoreTime: boolean = false) {
    let arr: RbObject[] = Array.isArray(event.data) ? event.data : [event.data];
    let masterObject: RbObject = arr[0];
    let cfg: GanttSeriesConfig = this.getBestSeriesConfigForObject(masterObject);
    if(cfg.timeFromAttributes == false) return;
    let masterPreviousLaneValues = cfg.laneAttributes.map(la => masterObject.get(la));
    let masterChangedLanes = !this.linkValuesMatch(masterPreviousLaneValues, lane.linkValues);
    let masterNewStartMS = null;
    let timeDiffMS = null;
    let tx = new RbObjectTransaction();

    if(!ignoreTime) {
      let pos = this.getXYRelativeToTarget(event.mouseEvent, "rb-gantt-lane");
      let left = pos.x - event.offset.x;
      let spreadsToSnapToEnd = lane.spreads.filter(s => s.object != masterObject && s.end - 15 < left && s.end + 15 > left).sort((s1, s2) => Math.abs(s1.end - left) - Math.abs(s2.end - left));
      if(spreadsToSnapToEnd.length > 0) {
        let [ststStartMS, ststEndMS, ststDurMS] = this.getObjectStartEndDur(spreadsToSnapToEnd[0].object, this.getSeriesConfigForObject(spreadsToSnapToEnd[0].object));
        masterNewStartMS = ststEndMS;      
      } else {
        masterNewStartMS = Math.round(this.startMS + (left / this.pxPerMS));
      }
      let prevStartStr = masterObject.get(cfg.start.attribute);
      if(prevStartStr != null) {
        timeDiffMS = Math.round(masterNewStartMS - (new Date(prevStartStr)).getTime());
      }
    }

    this.blockRecalc = true;
    for(var object of arr) {
      let update: any = {};
      let related: any = {}
      let spread = this.spreadMap[`${object.objectname}.${object.uid}`]
      if(masterNewStartMS != null) {
        let [previousStart, previousEnd, previousDuration] = this.getObjectStartEndDur(object, cfg);
        let thisStartMS = previousStart != null && timeDiffMS != null ? new Date(previousStart).getTime() + timeDiffMS : masterNewStartMS;
        let thisStart = new Date(thisStartMS);
        let thisStartStr = thisStart.toISOString();
        if(this.allowpastdrop == false && this.overrideAllowPastDrop == false && thisStartMS < (new Date()).getTime()) {
          if(spread != null) spread.dragging = false;
          break;
        }
        if(previousStart != thisStartStr) {
          update[cfg.start.attribute] = thisStartStr;
          if(cfg.duration == null && cfg.end != null) {
            let thisEnd = (new Date(thisStartMS + previousDuration)).toISOString();
            update[cfg.end.attribute] = thisEnd;
          } 
        }
      }
  
      if(masterChangedLanes) {
        let previousLinkValues = cfg.laneAttributes.map(la => object.get(la));
        if(!this.linkValuesMatch(previousLinkValues, lane.linkValues)) {
          for(var i = 0; i < lane.linkValues.length; i++) {
            let attribute = cfg.laneAttributes[i];
            if(object.canEdit(attribute)) {
              update[attribute] = lane.linkValues[i];
              if(lane.config.linkAttributes[i] == "uid") {
                related[attribute] = lane.object;
              }
            }
          }
        }
      }
  
      let targetDataset = this.datasetgroup != null ? this.datasetgroup.datasets[cfg.dataset] : this.dataset;
      if(targetDataset != null && targetDataset.list.indexOf(object) == -1) {
        targetDataset.add(object);
      }

      if(Object.keys(update).length > 0) {
        object.setValuesAndRelated(update, related, tx);
      }
    }
    this.blockRecalc = false;
    if(tx.objects.length > 0) {
      this.dataService.pushTransactionToServer(tx);
    }
    //Don't recalc here, the recalc will be done on the end drag event;
  }

  public droppedOut(event: any) {
    let arr = Array.isArray(event.data) ? event.data : [event.data];
    let tx = new RbObjectTransaction();
    this.blockRecalc = true;
    for(var object of arr) {
      let cfg: GanttSeriesConfig = this.getSeriesConfigForObject(object);
      if(cfg.timeFromAttributes) {
        let update: any = {};
        update[cfg.start.attribute] = null;
        for(var la of cfg.laneAttributes) {
          if(object.canEdit(la)) update[la] = null;
        }
        if(Object.keys(update).length > 0) {
          object.setValues(update, tx);
        }
      }
    }
    this.clearSelection();
    this.blockRecalc = false;
    if(tx.objects.length > 0) {
      this.dataService.pushTransactionToServer(tx);
    }
  }

  public getDragSizeForObject(data: any) : any {
    let obj = Array.isArray(data) ? data[0] : data;
    let cfg: GanttSeriesConfig = this.getBestSeriesConfigForObject(obj);
    if(cfg != null) {
      let [startMS, endMS, durationMS] = this.getObjectStartEndDur(obj, cfg);
      return {
        x: Math.round(durationMS * this.pxPerMS),
        y: this.spreadHeightPX
      };
    } else {
      return {x: 100, y: 20};
    }
  }

  public onScroll(event) {
    this.scrollLeft = event.target.scrollLeft;
    this.scrollTop = event.target.scrollTop;
  }

  //Utils

  private getStartAndWidthPX(obj: RbObject, cfg: GanttTimeBasedConfig) {
    let [startMS, endMS, durationMS] = this.getObjectStartEndDur(obj, cfg);
    let startPX: number = Math.round((startMS - this.startMS) * this.pxPerMS);
    if(startPX < this.widthPX) {
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

  getObjectStartEndDur(obj: RbObject, cfg: GanttTimeBasedConfig) {
    let startMS = (new Date(cfg.start.getValue(obj))).getTime();
    let durationMS = null;
    let endMS = null;
    if(cfg.duration != null) {
      durationMS = parseInt(cfg.duration.getValue(obj));
      endMS = startMS + durationMS;
    } else if(cfg.end != null) {
      endMS = (new Date(cfg.end.getValue(obj))).getTime();
      durationMS = endMS - startMS;
    } 
    if(durationMS == null || isNaN(durationMS)) durationMS = 3600000;
    return [startMS, endMS, durationMS];
  }

  getBestSeriesConfigForObject(object: RbObject) : GanttSeriesConfig {
    let cfg: GanttSeriesConfig = this.getSeriesConfigForObject(object);
    if(cfg == null) { // This will happen when a drag comes from an external dataset
      for(var c of this.seriesConfigs) {
          let dataset = this.getDatasetForConfig(c);
          if(dataset.objectname == object.objectname) cfg = c;
      }
    }
    return cfg;
  }

  linkValuesMatch(a: string[], b: string[]) {
    if(a.length != b.length) return false;
    for(var i = 0; i < a.length; i++) {
      if(a[i] != b[i]) return false;
    }
    return true;
  }

  getXYRelativeToTarget(event: any, cls: string): XY {
    let tgt = event.target;
    let x = event.offsetX;
    let y = event.offsetY;
    while(tgt.className.indexOf(cls) == -1) {
      x = x + tgt.offsetLeft;
      y = y + tgt.offsetTop;
      tgt = tgt.offsetParent;
    }
    return new XY(x, y);
  }

  isObjectDragging(obj: RbObject) : boolean {
    return this.dragService != null && this.dragService.isDragging && (this.dragService.data == obj || (Array.isArray(this.dragService.data) && this.dragService.data.indexOf(obj) > -1))/* && this.dragService.hasDropped == false*/;
  }

}
