import { Component, Input, ViewChild, ViewContainerRef, ChangeDetectorRef } from '@angular/core';
import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { RbObject, RbObjectTransaction, RELATED_LOADING, XY } from 'app/datamodel';
import { BuildService } from 'app/services/build.service';
import { DragService } from 'app/services/drag.service';
import { FilterService } from 'app/services/filter.service';
import { ModalService } from 'app/services/modal.service';
import { UserprefService } from 'app/services/userpref.service';
import { Subscription } from 'rxjs';
import { GanttDependencyType, GanttLane, GanttLaneConfig, GanttOverlayConfig, GanttOverlayLane, GanttOverlaySpread, GanttSeriesConfig, GanttSpread, GanttSizes } from './rb-gantt-models';
import { RbScrollComponent } from 'app/rb-scroll/rb-scroll.component';
import { DataService } from 'app/services/data.service';
import { LogService } from 'app/services/log.service';
import { Algos, CanvasTool, ColorTool, Evaluator, ValueComparator } from 'app/helpers';
import { NavigateService } from 'app/services/navigate.service';
import { GanttTimeConfig } from './rb-gantt-timeconfig';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';


@Component({
  selector: 'rb-gantt',
  templateUrl: './rb-gantt.component.html',
  styleUrls: ['./rb-gantt.component.css']
})
export class RbGanttComponent extends RbDataCalcComponent<GanttSeriesConfig> {
  @Input('lanes') lanesInput : any;
  @Input('overlays') overlaysInput: any[] = [];
  @Input('layers') layers: any[] = [];
  @Input('toolbar') toolbarConfig : any;
  @Input('showcontrols') showcontrols: boolean = true;
  @Input('locktonow') locktonow: boolean = false;
  @Input('allowpastdrop') allowpastdrop: boolean = true;
  @Input('allowoverlapgroup') allowoverlapgroup: boolean = false;
  @Input('snapinterval') snapinterval: number | null = null;
  @Input('headerwidth') _headerwidth: number = 17;
  @Input('startvariable') startVariable: string | null = null;
  @Input('spanvariable') spanVariable: string | null = null;
  @Input('zoomvariable') zoomVariable: string | null = null;
  @Input('emptymessage') emptyMessage: string | null = null;
  @ViewChild('customtoolbar', { read: ViewContainerRef, static: true }) toolbar?: ViewContainerRef;
  @ViewChild('mainscroll', { read: ViewContainerRef, static: true }) mainscroll?: RbScrollComponent;
  @ViewChild('canvas', { read: ViewContainerRef, static: true }) canvas?: ViewContainerRef;

  lanesConfig: GanttLaneConfig | null = null;
  seriesConfigs: GanttSeriesConfig[] = [];
  overlayConfigs: GanttOverlayConfig[] = [];
  timeConfig!: GanttTimeConfig;
  heightPX: number | null = null;
  sizes: GanttSizes;
  /*spreadHeightPX: number = 42;
  spreadMarginPX: number = 4;
  borderWidthPX: number = 300;*/
  headerWidthPX: number | null = null;
  doDragFilter: boolean = false;
  hideEmptyLanes: boolean = false;
  hideEmptyBackgrounds: boolean = false;
  overrideAllowPastDrop: boolean = false;
  scrollTop: number | null = null;
  scrollLeft: number | null = null;
  labelAlts: any[] = [];
  lanes: GanttLane[] = [];
  spreads: GanttSpread[] = [];
  overlays: GanttOverlayLane[] = [];
  selectedOverlayLaneIndex = -1;
  selectedLabelAlt: string | null = null;
  spreadMap: any = {};

  dragSelecting: boolean = false;
  dragSelectStart: XY  | null = null;
  dragSelectTopLeft: XY | null  = null;
  dragSelectSize: XY | null = null;

  doFocus: boolean = false;
  focusStartPX: number | null = null;
  focusTopPX: number | null = null;

  openGroup: string | null = null;

  public getDragSizeForObjectCallback?: Function;
  public droppedOutCallback?: Function;
  public enhanceDragDataCallback?: Function;
  dragSubscription?: Subscription;

  graphctx: any = null;

  constructor(
    private modalService: ModalService,
    private navigateService: NavigateService,
    private dragService: DragService,
    private filterService: FilterService,
    private userprefService: UserprefService,
    private buildService: BuildService,
    private dataService: DataService,
    private logService: LogService,
    private cdr: ChangeDetectorRef
  ) {
    super();
  }

  dataCalcInit() {
    //this.cdr.detach();
    this.sizes = new GanttSizes();
    this.timeConfig = new GanttTimeConfig({startVariable: this.startVariable, spanVariable: this.spanVariable, zoomVariable: this.zoomVariable, element: this.mainscroll?.element.nativeElement});
    this.timeConfig.changes().subscribe(() => this.updateData(true));
    this.dragSubscription = this.dragService.getObservable().subscribe(event => this.onDragEvent(event));
    this.getDragSizeForObjectCallback = this.getDragSizeForObject.bind(this);
    this.droppedOutCallback = this.droppedOut.bind(this);
    this.enhanceDragDataCallback = this.enhanceDragData.bind(this);
    this.headerWidthPX = Math.min(0.88 * this._headerwidth * window.innerWidth, 17 * this._headerwidth);
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
    if(this.toolbarConfig != null && this.toolbar != null) {
      for(var item of this.toolbarConfig) {
        var context: any = {dataset: this.dataset, datasetgroup: this.datasetgroup};
        this.buildService.buildConfigRecursive(this.toolbar, item, context);
      }
    }
    this.graphctx = this.canvas?.element.nativeElement.getContext("2d");
  }

  dataCalcDestroy() {
    this.dragSubscription.unsubscribe();
  }

  createSeriesConfig(json: any): GanttSeriesConfig {
    return new GanttSeriesConfig(json, this.userPref);
  }

  componentControl(data: any): void {
    if(data.start != null) this.timeConfig.startDate = typeof data.start == 'string' ? new Date(data.start) : data.start.getTime != null ? data.start : new Date();
    if(data.span != null) this.timeConfig.span = data.span;
    if(data.zoom != null) this.timeConfig.zoom = data.zoom;
    if (data.hideemptylanes != null) this.hideEmptyLanes = data.hideemptylanes;
    if(data.hideemptybackgrounds != null) this.hideEmptyBackgrounds = data.hideemptybackgrounds;
  }

  onActivationEvent(event: any) {
    if(this.active) {
      this.scrollTop = 0;
      this.scrollLeft = 0;
      this.timeConfig.reset();
      let [dataStartDate, dataSpan] = this.getStartDateAndSpanFromDatasetFilters();
      if(dataStartDate != null) this.timeConfig.startDate = dataStartDate;
      if(dataSpan != null) this.timeConfig.span = dataSpan;
      super.onActivationEvent(event);
    }
  }

  onDatasetEvent(event: any) {
    if(event.event == 'select') {
      this.drawCanvas();
    } else {
      super.onDatasetEvent(event);
    }
    if(this.active && event.event == 'load' && event.dataset != null && event.dataset.getId() == this.lanesConfig?.dataset) {
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

  get selectedObject() : RbObject|null {
    return this.dataset != null ? this.dataset.selectedObject : this.datasetgroup != null ? this.datasetgroup.selectedObject : null;
  }

  get isLoading() : boolean {
    return this.dataset != null ? this.dataset.isLoading : this.datasetgroup != null ? this.datasetgroup.isLoading : false;
  }

  get availLabelAlts() : any[] {
    return this.labelAlts.filter(a => a.name != this.selectedLabelAlt);
  }

  get extraContext() : any {
    return {
      ganttstart: this.timeConfig.startDate.toISOString(),
      ganttend: this.timeConfig.endDate.toISOString(),
    };
  }

  get isEmpty() : boolean {
    return !(this.lanes != null && this.lanes.length > 0)
  }

  toggleDragFilter() {
    this.doDragFilter = !this.doDragFilter;
  }

  toggleHideEmptyLanes() {
    this.hideEmptyLanes = !this.hideEmptyLanes;
    this.redraw();
  }

  toggleHideEmptyBackgrounds() {
    this.hideEmptyBackgrounds = !this.hideEmptyBackgrounds;
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
        let startAttribute = cfg.applyDateFilter ? cfg.start.attribute : undefined;
        let endAttribute = cfg.applyDateFilter ? cfg.end?.attribute : undefined;
        let filterSort = this.getFilterSort(cfg.dataset, startAttribute, endAttribute);
        if(this.datasetgroup != null) {
          fetched = this.datasetgroup.datasets[cfg.dataset].filterSort(filterSort) || fetched;
        } else if(this.dataset != null) {
          fetched = this.dataset.filterSort(filterSort) || fetched;
        }
      }
    }
    return fetched;
  }

  getFilterSortForSeries(cfg: GanttSeriesConfig) : any {
    let startAttribute = cfg.applyDateFilter ? cfg.start.attribute : undefined;
    let endAttribute = cfg.applyDateFilter ? cfg.end?.attribute : undefined;
    let laneAttributes = cfg.applyLaneFilter ? cfg.laneAttributes : undefined;
    let laneForeignAttributes = cfg.applyLaneFilter ? cfg.laneForeignAttributes || this.lanesConfig?.linkAttributes : undefined;
    return this.getFilterSort(cfg.dataset, startAttribute, endAttribute, laneAttributes, laneForeignAttributes);
  }

  getFilterSort(datasetId: string, startAttribute?: string, endAttribute?: string, laneAttributes?: string[], laneForeignAttributes?: string[]) {
    let dataset = this.datasetgroup != null ? this.datasetgroup.datasets[datasetId] : this.dataset;
    let startDate = this.timeConfig.startDate;
    let endDate = this.timeConfig.endDate;
    let filter = dataset != null && dataset.userFilter != null ? {...dataset.userFilter} : {};
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
    if(laneAttributes != null && !(laneAttributes.length == 1 && laneAttributes[0] == 'uid') && laneForeignAttributes != null) {
      let laneList: RbObject[] = this.lists != null && this.lanesConfig != null ? this.lists[this.lanesConfig.dataset] : this.list;
      if(laneList.length == 0) return null; //Don't fetch if lanes are empty or not loaded yet.
      if(laneAttributes.length == 1 && laneForeignAttributes.length > 0) {
        filter[laneAttributes[0]] = {$in: laneList.map(lane => "'" + lane.get(laneForeignAttributes[0]) + "'")}
      } else {
        var orlist = [];
        for(var lane of laneList) {
          var clause: any = {};
          for(var i = 0; i < laneAttributes.length; i++) {
            clause[laneAttributes[i]] = "'" + lane.get(laneForeignAttributes[i]) + "'";
          }
          orlist.push(clause);
        }
        filter['$or'] = orlist;
      }
    }
    return {filter:filter};
  }

  calc() {
    this.sizes.calc();
    /*this.spreadHeightPX = Math.min(0.0167 * window.innerWidth, 32);
    this.spreadMarginPX = Math.min(0.004175 * window.innerWidth, 8);
    this.borderWidthPX = Math.min(0.000521875 * window.innerWidth, 1);*/
    this.focusStartPX = null;
    this.focusTopPX = null;

    this.calcLanes();
    this.calcDependencies();
    this.calcOverlayLanes();
    this.logService.debug("Gantt " + this.id + ": calc, lanes: " + this.lanes.length + ", overlays: " + this.overlays.length + ", spreads: " + this.spreads.length + ", dragging: " + this.spreads.filter(s => s.dragging == true).length);
    this.drawCanvas();
    if(this.doFocus == true) {
      this.focus();
    }
    //this.cdr.detectChanges();
  }

  private calcLanes() {
    let accHeight = 0;
    this.lanes = [];
    this.spreads = [];
    this.spreadMap = {};
    let laneFilter: any = null;
    if(this.lanesConfig != null) {
      if(this.doDragFilter && this.dragService.isDragging && this.lanesConfig.dragfilter != null) {
        let draggingObject = Array.isArray(this.dragService.data) ? this.dragService.data[0] : this.dragService.data;
        laneFilter = this.filterService.resolveFilter(this.lanesConfig.dragfilter, draggingObject);
      };
      let list: RbObject[] = this.lists != null ? this.lists[this.lanesConfig.dataset] : this.list;
      if(this.lanesConfig.orderAttribute != null) {
        list.sort((a, b) => ValueComparator.sort(a.get(this.lanesConfig!.orderAttribute), b.get(this.lanesConfig!.orderAttribute)));
      }
      for(let obj of list) {
        let show = laneFilter != null && !this.filterService.applies(laneFilter, obj) ? false : true;
        if(show) {
          let lane = new GanttLane(obj, this.lanesConfig, this.sizes);
          let spreads: GanttSpread[] = this.calcSpreads(lane, accHeight);
          let hasForegroundSpreads = spreads.filter(s => !s.ghost && !s.background).length > 0;
          let hasBackgroundStreaps = spreads.filter(s => !s.ghost && s.background).length > 0;
          if(!((!hasForegroundSpreads && this.hideEmptyLanes) || (!hasBackgroundStreaps && this.hideEmptyBackgrounds))) {
            lane.setSpreads(spreads);
            this.lanes.push(lane);
            accHeight += lane.height + this.sizes.borderWidth;
          }
        }
      }
      this.heightPX = accHeight;
    }
  }

  private calcSpreads(lane: GanttLane, offsetTop: number) : GanttSpread[] {
    let laneSpreads: GanttSpread[] = [];
    for (let cfg of this.seriesConfigs) {
      if (cfg.groupAttribute == null) {  //Only group parents or non-grouped series
        let laneLinkValues = lane.getLinkValuesForSeries(cfg);
        let dataset = this.getDatasetForConfig(cfg);
        if(dataset != null) {
          for(var obj of dataset.list) {
            let objLinkValues = cfg.laneAttributes.map(la => obj.get(la));
            if(this.linkValuesMatch(objLinkValues, laneLinkValues)) {
              let show = cfg.show == null || (cfg.show != null && cfg.show(obj, dataset, dataset.relatedObject));
              if (show) {
                let spread = this.calcSpread(obj, dataset, cfg, offsetTop);
                if (spread != null) {
                  let childrenSpreads = [];
                  if (cfg.groupOf != null && this.openGroup == obj.uid) {
                    let childCfg = this.seriesConfigs.find(c => c.id == cfg.groupOf)
                    let childDataset = this.getDatasetForConfig(childCfg);
                    let children = childDataset.list.filter(obj => obj.get(childCfg.groupAttribute) == this.openGroup);
                    for (var childSpread of children.map(o => this.calcSpread(o, childDataset, childCfg, offsetTop)).filter(s => s != null)) {
                      childSpread.sublaneIndex = this.getSublaneForSpread(childSpread, childrenSpreads);
                      childrenSpreads.push(childSpread);
                    }
                    spread.sublaneSpan = childrenSpreads.reduce((acc, s) => Math.max(acc, s.sublaneIndex + 1), 1);
                    spread.group = true;
                  }
                  let sublane = cfg.isBackground ? 0 : this.getSublaneForSpread(spread, laneSpreads);
                  spread.sublaneIndex = sublane;
                  laneSpreads.push(spread);
                  if (childrenSpreads.length > 0) {
                    childrenSpreads.forEach(cs => cs.sublaneIndex += spread.sublaneIndex);
                    childrenSpreads.forEach(cs => laneSpreads.push(cs));
                  }
                }
              }
            }
          }
        }
      }
    }
    return laneSpreads;
  }

  private calcSpread(obj: RbObject, dataset: RbDatasetComponent, cfg: GanttSeriesConfig,  offsetTop: number): GanttSpread | null {
    let spread: GanttSpread | null = null;
    let [startPX, widthPX] = this.timeConfig.getStartAndWidthPX(obj, cfg);
    if (startPX != null && widthPX != null) {
      let label = null;
      if(cfg.labelAttribute != null) {
        label = obj.get(cfg.labelAttribute);
      } else if(cfg.labelExpression != null) {
        label = Evaluator.eval(cfg.labelExpression, obj);
      }
      if(label == RELATED_LOADING) {
        label = "...";
      }
      let labelWidth = this.graphctx?.measureText(label).width;
      if(this.selectedLabelAlt != null && cfg.labelAlts != null) {
        let alt = cfg.labelAlts.find(a => a.name == this.selectedLabelAlt);
        if(alt != null) {
          if(alt.attribute != null) {
            label = obj.get(alt.attribute);
          } else if(alt.expression != null) {
            label = Evaluator.eval(alt.expression, obj);
          }
        }
      }
      spread = new GanttSpread(label, startPX, widthPX, offsetTop, obj, dataset, cfg, this.sizes);
      spread.color = cfg.color?.getColor(obj) || (cfg.isBackground ? 'white' : 'var(--primary-light-color)');
      spread.labelcolor = cfg.labelColor != null ? cfg.labelColor : ColorTool.contrastWith(spread.color, "#333", "#d8d8d8");
      if (cfg.groupOf != null) {
        spread.color = this.calcGroupBackground(obj.uid, spread.color, startPX, this.seriesConfigs.find(c => c.id == cfg.groupOf));
      }
      if(cfg.indicatorAttribute != null) {
        spread.indicator = obj.get(cfg.indicatorAttribute);
      } else if(cfg.indicatorExpression != null) {
        spread.indicator = Evaluator.eval(cfg.indicatorExpression, obj);
      }
      spread.tip = labelWidth > widthPX ? label : null;
      spread.dragging = this.isObjectDragging(obj);
      this.spreads.push(spread);
      this.spreadMap[`${obj.objectname}.${obj.uid}`] = spread;
      if(spread.selected) {
        if(this.focusStartPX == null || (this.focusStartPX != null && startPX < this.focusStartPX)) this.focusStartPX = startPX;
        let topPX = offsetTop + spread.laneTop;
        if(this.focusTopPX == null || (this.focusTopPX != null && topPX < this.focusTopPX)) this.focusTopPX = topPX;
      }
    }
    return spread;
  }

  private calcGroupBackground(groupKey: string, baseColor: string, startOffset: number, cfg: GanttSeriesConfig): string | null {
    let dataset = this.getDatasetForConfig(cfg);
    let list = dataset.list.filter(obj => obj.get(cfg.groupAttribute) == groupKey);
    let intervals = list.map(obj => this.timeConfig.getStartAndWidthPX(obj, cfg)).map(i => [i[0] - startOffset, i[0] + i[1] - startOffset]);
    let merged = Algos.intervalMerge(intervals);
    let transColor = `color-mix(in srgb, ${baseColor} 30%, transparent 70%)`;
    let blocks = merged.map(i => `${transColor} ${i[0] - 2}px, ${baseColor} ${i[0] + 2}px, ${baseColor} ${i[1] - 2}px, ${transColor} ${i[1] + 2}px`);
    let bg = `linear-gradient(to right, ${blocks.join(', ')})`;
    return bg;
  }

  private calcDependencies() {
    for(var spread of this.spreads) {
      if(spread.object != null) {
        for(var dep of this.getDependenciesForObject(spread.object, spread.config)) {
          let depSpread = this.spreadMap[`${dep.object.objectname}.${dep.object.uid}`]
          if(depSpread != null) {
            spread.dependencies.push({spread: depSpread, type: dep.type});
            depSpread.dependencies.push({spread: spread, type: dep.type == GanttDependencyType.SF ? GanttDependencyType.FS : dep.type});
          }
        }
      }
    }
  }

  private calcOverlayLanes() {
    this.overlays = [];
    for(var cfg of this.overlayConfigs) {
      let dataset = this.datasetgroup != null ? this.datasetgroup.datasets[cfg.dataset] : this.dataset;
      if(dataset != null) {
        let spreads: GanttOverlaySpread[] = [];
        for(var obj of dataset.list) {
          let [startPX, widthPX] = this.timeConfig.getStartAndWidthPX(obj, cfg);
          if(startPX != null && widthPX != null) {
            let color = cfg.color?.getColor(obj) || 'var(--primary-light-color)';
            if(color != null) {
              let spread = new GanttOverlaySpread(null, startPX, widthPX, color, obj, cfg);
              spreads.push(spread);
            }
          }
        }
        let label = cfg.label != null ? cfg.label : (cfg.labelAttribute != null && spreads.length > 0 ? spreads[0].object.get(cfg.labelAttribute) : null);
        let lane = new GanttOverlayLane(null, label, this.sizes.laneHeight);
        lane.setSpreads(spreads);
        this.overlays.push(lane);
      }
    }
  }

  drawCanvas() {
    var selectedSpreads = this.spreads.filter(s => s.selected);
    if(this.canvas != null) {
      this.graphctx.clearRect(0, 0, this.canvas.element.nativeElement.width, this.canvas.element.nativeElement.height);
      if(this.dragService.isDragging == false && selectedSpreads.length == 1) {
        let spread = selectedSpreads[0];
        for(let dep of spread.dependencies) {
          let startdir = dep.type == GanttDependencyType.FS ? 'right' : dep.type == GanttDependencyType.SF ? 'left' : dep.type == GanttDependencyType.SS ? 'left' : spread.offsetTop + spread.laneTop < dep.spread.offsetTop + dep.spread.laneTop ? 'bottom' : 'top';
          let enddir = dep.type == GanttDependencyType.FS ? 'left' : dep.type == GanttDependencyType.SF ? 'right' : dep.type == GanttDependencyType.SS ? 'left' : spread.offsetTop + spread.laneTop < dep.spread.offsetTop + dep.spread.laneTop ? 'top' : 'bottom';
          let startx = spread.start + (startdir == 'left' ? 0 : startdir == 'right' ? spread.width : spread.width / 2);
          let starty = spread.offsetTop + spread.laneTop + (startdir == 'top' ? 0 : startdir == 'bottom' ? spread.height : spread.height / 2);
          let endx = dep.spread.start + (enddir == 'left' ? 0 : enddir == 'right' ? dep.spread.width : dep.spread.width / 2);
          let endy = dep.spread.offsetTop + dep.spread.laneTop + (enddir == 'top' ? 0 : enddir == 'bottom' ? spread.height : spread.height / 2);
          CanvasTool.drawPolyline(this.graphctx, startx, starty, startdir, endx, endy, enddir);
        }
      }
    }
  }

  private focus() {
    if(this.focusStartPX != null && this.focusTopPX != null) {
      this.mainscroll?.scrollToHPos(Math.max(0, this.focusStartPX - 30));
      this.mainscroll?.scrollToVPos(Math.max(0, this.focusTopPX - 30));
      this.doFocus = false;
    }
  }

  public selectedOverlaySpreads() : GanttOverlaySpread[] {
    return this.selectedOverlayLaneIndex > -1 && this.selectedOverlayLaneIndex < this.overlays.length ? this.overlays[this.selectedOverlayLaneIndex].spreads : []
  }

  public clearSelection() {
    for(var cfg of this.seriesConfigs) {
      let dataset = this.getDatasetForConfig(cfg);
      dataset?.clearSelection();
    }
  }

  public select(object: RbObject) {
    let dataset = this.getDatasetForObject(object);
    dataset?.select(object);
  }

  public addOneToSelection(object: RbObject) {
    let dataset = this.getDatasetForObject(object);
    dataset?.addOneToSelection(object);
  }

  public selectArea(start: number, end: number, top: number, bottom: number) {
    this.clearSelection();
    for(var lane of this.lanes) {
      for(var s of lane.foregroundSpreads()) {
        let stop = s.offsetTop + s.laneTop;
        let sbot = stop + s.height;
        if(s.object != null && (s.start + s.width) > start &&  s.start < end && sbot > top && stop < bottom) {
          this.addOneToSelection(s.object);
        }
      }
    }
  }

  public clickSpread(spread: GanttSpread, event: any) {
    if(spread.object != null) {
      if(event.ctrlKey == true || event.metaKey == true) {
        this.addOneToSelection(spread.object);
      } else if(event.shiftKey == true) {
        this.addOneToSelection(spread.object);
      } else {
        let ds = this.getDatasetForConfig(spread.config);
        let alreadySelected = ds?.isObjectSelected(spread.object);
        if(!alreadySelected) this.select(spread.object);
        if(spread.config.click != null) {
          let event = {alreadyselected: alreadySelected, object: spread.object, dataset: ds};
          spread.config.click(event);
        }
        if(spread.config.modal != null) {
          this.modalService.open(spread.config.modal);
        } else if(spread.config.link != null && this.lanesConfig != null) {
          let navEvent = spread.config.link.getNavigationEvent(spread.object, this.getDatasetForObject(spread.object), this.extraContext);
          this.navigateService.navigateTo(navEvent);
        } else if (spread.config.groupOf != null) {
          this.openGroup = spread.object.uid;
          this.redraw();
        }
      }
    }
  }

  public clickLane(lane: GanttLane) {
    if(this.lanesConfig != null) {
      let laneDataset = this.datasetgroup != null ? this.datasetgroup.datasets[this.lanesConfig.dataset] : this.dataset;
      if(laneDataset != null) {
        laneDataset.select(lane.object);
        if(this.lanesConfig.modal != null) {
          this.modalService.open(this.lanesConfig.modal);
        } else if(this.lanesConfig.link != null) {
          let navEvent = this.lanesConfig.link.getNavigationEvent(lane.object, laneDataset, this.extraContext);
          this.navigateService.navigateTo(navEvent);
        }
      }
    }
  }

  public deleteLane(lane: GanttLane) {
    this.dataService.delete(lane.object).subscribe(result => {});
  }

  public addLane() {
    var dataset = this.getDatasetForLanesConfig();
    dataset?.create();
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
    if (this.openGroup != null) {
      this.openGroup = null;
      this.redraw();
    }
  }

  public mouseMoveBackground(event: any) {
    if(this.dragSelecting && this.dragSelectStart != null) {
      let pos = this.getXYRelativeToTarget(event, "rb-gantt-lanes");
      this.dragSelectTopLeft = new XY(Math.min(pos.x, this.dragSelectStart.x), Math.min(pos.y, this.dragSelectStart.y));
      this.dragSelectSize = new XY(Math.abs(pos.x - this.dragSelectStart.x), Math.abs(pos.y - this.dragSelectStart.y));
    }
  }

  public mouseUpBackground(event: any) {
    if(this.dragSelecting && this.dragSelectStart != null && this.dragSelectSize != null && this.dragSelectTopLeft != null && this.dragSelectSize.x > 0 && this.dragSelectSize.y > 0) {
      let start = this.dragSelectTopLeft.x;
      let end = this.dragSelectTopLeft.x + this.dragSelectSize.x;
      let top = this.dragSelectTopLeft.y;
      let bottom = this.dragSelectTopLeft.y + this.dragSelectSize.y;
      this.selectArea(start, end, top, bottom);
      event.stopPropagation();
    }
    this.dragSelecting = false;
  }

  public mouseDownHeaderbar(event: any) {
    let controller = this;
    let startClientX = event.clientX;
    let startHeaderWidthPX = this.headerWidthPX ?? 300;
    var whileMove = function(event: any) {
      let clientDelta = event.clientX - startClientX;
      controller.headerWidthPX = Math.max(0, startHeaderWidthPX + clientDelta);
    }
    var endMove = function () {
      window.removeEventListener('mousemove', whileMove);
      window.removeEventListener('mouseup', endMove);
      controller.timeConfig.calc(true);
    };
    event.stopPropagation();
    window.addEventListener('mousemove', whileMove);
    window.addEventListener('mouseup', endMove);
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
    let firstObj = Array.isArray(event.data) ? event.data[0] : event.data;
    if(firstObj instanceof RbObject) {
      this.objectsDroppedOnLane(event, lane, ignoreTime);
    } else if(firstObj instanceof GanttLane) {
      this.laneDroppedOnLane(firstObj, lane);
    }
  }

  public objectsDroppedOnLane(event: any, lane: GanttLane, ignoreTime: boolean = false) {
    let arr: RbObject[] = Array.isArray(event.data) ? event.data : [event.data];
    let masterObject: RbObject = arr[0];
    let cfg = this.getBestSeriesConfigForObject(masterObject);
    if(cfg == null || cfg.timeFromAttributes == false) return;
    let laneLinkAttributes = (cfg.laneForeignAttributes || this.lanesConfig?.linkAttributes);
    let laneLinkValue = lane.getLinkValuesForSeries(cfg)
    let masterPreviousLaneValues = cfg.laneAttributes.map(la => masterObject.get(la));
    let masterChangedLanes = !this.linkValuesMatch(masterPreviousLaneValues, laneLinkValue);
    let masterNewStartMS = null;
    let timeDiffMS = null;
    let tx = new RbObjectTransaction();

    if(!ignoreTime) {
      let pos = this.getXYRelativeToTarget(event.mouseEvent, "rb-gantt-lane");
      masterNewStartMS = this.snap(masterObject, pos.x - event.offset.x, lane);
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
        let [previousStartMS, previousEndMS, previousDurationMS] = cfg.getObjectStartEndDur(object);
        let thisStartMS = previousStartMS != null && timeDiffMS != null ? previousStartMS + timeDiffMS : masterNewStartMS;
        if(this.allowpastdrop == false && this.overrideAllowPastDrop == false && thisStartMS < (new Date()).getTime()) {
          if(spread != null) spread.dragging = false;
          break;
        }
        if(previousStartMS != thisStartMS) {
          update[cfg.start.attribute] = (new Date(thisStartMS)).toISOString();
          if(cfg.duration == null && cfg.end != null) {
            let thisEnd = (new Date(thisStartMS + previousDurationMS)).toISOString();
            update[cfg.end.attribute] = thisEnd;
          }
        }
      }

      if(masterChangedLanes) {
        let objectPreviousLinkValues = cfg.laneAttributes.map(la => object.get(la));
        if(!this.linkValuesMatch(objectPreviousLinkValues, laneLinkValue)) {
          for(var i = 0; i < laneLinkValue.length; i++) {
            let attribute = cfg.laneAttributes[i];
            if(object.canEdit(attribute)) {
              update[attribute] = laneLinkValue[i];
              if(laneLinkAttributes[i] == "uid") {
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
    this.dataService.pushTransactionToServer(tx);
    //Don't recalc here, the recalc will be done on the end drag event;
  }

  public laneDroppedOnLane(droppedLane: GanttLane, lane: GanttLane, ignoreTime: boolean = false) {
    if(this.lanesConfig != null && this.lanesConfig.orderAttribute != null) {
      let tx = new RbObjectTransaction();
      let list: RbObject[] = this.lists != null ? this.lists[this.lanesConfig.dataset] : this.list;
      list.sort((a, b) => ValueComparator.sort(a.get(this.lanesConfig!.orderAttribute), b.get(this.lanesConfig!.orderAttribute)));
      if(list.map(o => o.get(this.lanesConfig!.orderAttribute)).filter(v => v == null).length > 0) {
        for(let i = 0; i < list.length; i++) {
          list[i].setValue(this.lanesConfig.orderAttribute, i, tx);
        }
      }

      let allOrderValues = list.map(o => o.get(this.lanesConfig!.orderAttribute));
      let newList = list.filter(o => o != droppedLane.object);
      let index = newList.indexOf(lane.object);
      newList.splice(index, 0, droppedLane.object);
      for(let i = 0; i < newList.length; i++) {
        newList[i].setValue(this.lanesConfig.orderAttribute, allOrderValues[i], tx);
      }
      this.dataService.pushTransactionToServer(tx);
    }
  }

  public droppedOut(event: any) {
    let arr = Array.isArray(event.data) ? event.data : [event.data];
    let tx = new RbObjectTransaction();
    this.blockRecalc = true;
    for(var object of arr) {
      let cfg = this.getSeriesConfigForObject(object);
      if(cfg != null && cfg.timeFromAttributes) {
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

  public onScroll(event: any) {
    this.scrollLeft = event.target.scrollLeft;
    this.scrollTop = event.target.scrollTop;
  }

  //Utils

  public getStartDateAndSpanFromDatasetFilters() : [Date|null, number|null] {
    for(let cfg of this.seriesConfigs) {
      let dataset = this.getDatasetForConfig(cfg);
      if(dataset != null && cfg.timeFromAttributes && dataset.resolvedFilter != null) {
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
          let startDate = new Date(start);
          let span = ((new Date(end)).getTime() - ((new Date(start)).getTime()));
          return [startDate, span];
        }
      }
    }
    return [null, null];
  }

  public getDragSizeForObject(data: any) : any {
    let obj = Array.isArray(data) ? data[0] : data;
    if(obj instanceof RbObject) {
      let cfg = this.getBestSeriesConfigForObject(obj);
      if(cfg != null) {
        return {
          x: this.timeConfig.getWidthOfObject(obj, cfg),
          y: this.sizes.laneHeight - (2*this.sizes.marginSize)
        };
      } else {
        return {x: 100, y: 20};
      }
    } else if(obj instanceof GanttLane) {
      return {
        x: this.headerWidthPX,
        y: obj.height
      };
    }
  }

  snap(object: RbObject, droppedLeft: number, lane: GanttLane): number {
    let candidates = [];
    let cfg = this.getBestSeriesConfigForObject(object);
    if(cfg != null) {
      for(const dep of this.getDependenciesForObject(object, cfg)) {
        let depSpread = this.spreadMap[`${dep.object.objectname}.${dep.object.uid}`]
        if(depSpread != null && (dep.type == GanttDependencyType.SS || dep.type == GanttDependencyType.DU)) {
          let [startMS, endMS, durMS] = depSpread.config.getObjectStartEndDur(depSpread.object);
          candidates.push({d: Math.abs(depSpread.start - droppedLeft), t: startMS});
        }
      }
    }
    for(const otherSpread of lane.spreads.filter(s => s.object != object)) {
      let [startMS, endMS, durMS] = otherSpread.config.getObjectStartEndDur(otherSpread.object!);
      candidates.push({d: Math.abs(otherSpread.end - droppedLeft), t: endMS});
    }
    candidates = candidates.filter(c => c.d < 15);
    candidates.sort((a, b) => a.d - b.d);
    if(candidates.length > 0) {
      return candidates[0].t;
    } else {
      let ms = this.timeConfig.getTimeOfLeftPX(droppedLeft);
      if(this.snapinterval != null) {
        ms = Math.round(ms / this.snapinterval) * this.snapinterval;
      }
      return ms;
    }
  }

  getBestSeriesConfigForObject(object: RbObject) : GanttSeriesConfig | undefined {
    let cfg = this.getSeriesConfigForObject(object);
    if(cfg == null) { // This will happen when a drag comes from an external dataset
      for(var c of this.seriesConfigs) {
          let dataset = this.getDatasetForConfig(c);
          if(dataset != null && dataset.objectname == object.objectname) cfg = c;
      }
    }
    return cfg;
  }

  getDatasetForLanesConfig() {
      return this.datasetgroup != null && this.lanesConfig != null ? this.datasetgroup.datasets[this.lanesConfig.dataset] : this.dataset;
  }

  getDependenciesForObject(object: RbObject, config?: GanttSeriesConfig) {
    let ret = [];
    if(config == null) config = this.getBestSeriesConfigForObject(object) ?? undefined;
    if(config != null) {
      let deps = config.dependencyAttribute != null ? object.get(config.dependencyAttribute) : null;
      if(deps != null) {
        let dataset = this.getDatasetForConfig(config);
        if(dataset != null) {
          for(var uid of Object.keys(deps)) {
            let depObject = dataset.list.find(o => o.uid == uid);
            if(depObject != null) {
              let rt = deps[uid];
              if(rt == 'DU') {
                ret.push({object: depObject, type: GanttDependencyType.DU});
              } else if(rt == 'AF') {
                ret.push({object: depObject, type: GanttDependencyType.SF});
              } else if(rt == 'SS') {
                ret.push({object: depObject, type: GanttDependencyType.SS});
              }
            }
          }
        }
      }
    }
    return ret;
  }

  getSublaneForSpread(spread: GanttSpread, spreads: GanttSpread[]) {
    let sublane = 0;
    let hasOverlap: boolean;
    do {
      hasOverlap = false;
      for(var os of spreads.filter(s => s.background == false)) {
        if(spread.start < os.end && os.start < spread.end && sublane <= os.sublaneIndex + os.sublaneSpan - 1 && os.sublaneIndex <= sublane + spread.sublaneSpan - 1) {
          hasOverlap = true;
        }
      }
      if(hasOverlap) sublane++;
    } while(hasOverlap);
    return sublane;
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
