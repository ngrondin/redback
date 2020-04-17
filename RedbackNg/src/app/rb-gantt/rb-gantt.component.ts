import { Component, OnInit, Input, SimpleChange } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { ObserveOnMessage } from 'rxjs/internal/operators/observeOn';


class GanttSeriesConfig {
  object: string;
  startAttribute: string;
  endAttribute: string;
  labelAttribute: string;
  groupAttribute: string;
  groupLabelAttribute: string;
  colorAttribute: string;
  colorMap: any;
  isBackground: boolean;

  constructor(json: any) {
    this.object = json.dataset;
    this.startAttribute = json.startattribute;
    this.endAttribute = json.endattribute;
    this.labelAttribute = json.labelattribute;
    this.groupAttribute = json.groupattribute;
    this.groupLabelAttribute = json.grouplabelattribute;
    this.isBackground = json.isbackground;
    this.colorAttribute = json.colorattribute;
    this.colorMap = json.colormap;
  }
}

class GanttGroup {
  id: string;
  label: string;

  constructor(i: string, l: string) {
    this.id = i;
    this.label = l;
  }
}

class GanttSpread {
  id: string;
  label: string;
  start: number;
  width: number;
  group: string;
  color: string;
  isBackground: boolean;

  constructor(i: string, l: string, s: number, w: number, g: string, c: string, bg: boolean) {
    this.id = i;
    this.label = l;
    this.start = s;
    this.width = w;
    this.group = g;
    this.color = c;
    this.isBackground = bg;
  }
}

@Component({
  selector: 'rb-gantt',
  templateUrl: './rb-gantt.component.html',
  styleUrls: ['./rb-gantt.component.css']
})
export class RbGanttComponent implements OnInit {
  @Input() list : RbObject[];
  @Input() series: any[];

  seriesConfigs: GanttSeriesConfig[];
  span: number;
  zoom: number;
  start: number;
  end: number;
  multiplier: number;
  width: number;

  constructor() { }


  ngOnChanges(changes : SimpleChange) {
    if('series' in changes && this.series != null) {
      this.seriesConfigs = [];
      for(let item of this.series) {
        this.seriesConfigs.push(new GanttSeriesConfig(item));
      }
    }
    if('list' in changes && this.list != null) {
      this.calcParams();
    }
  }

  ngOnInit() {
    this.span = 259200000;
    this.zoom = 259200000;
  }

  calcParams() {
    let now = new Date();
    this.start = now.getTime();
    this.end = this.start + this.span;
    this.multiplier = window.innerWidth / this.zoom;
    this.width = this.span * this.multiplier;
  }

  public getGroups(series: number) : GanttGroup[] {
    let cfg = this.seriesConfigs[series];
    let groups : GanttGroup[] = [];
    for(var i in this.list) {
      let obj = this.list[i];
      let objGroup: GanttGroup = null;
      for(var g of groups) {
        if(obj.get(cfg.groupAttribute) == g.id) {
          objGroup = g;
        }
      }
      if(objGroup == null) {
        objGroup = new GanttGroup(obj.get(cfg.groupAttribute), obj.get(cfg.groupLabelAttribute));
        groups.push(objGroup);
      }
    }
    return groups;
  }

  public getSpreads(series: number, groupId: string) : GanttSpread[] {
    let cfg = this.seriesConfigs[series];
    let spreads : GanttSpread[] = [];
    for(var i in this.list) {
      let obj = this.list[i];
      if(obj.get(cfg.groupAttribute) == groupId) {
        let start: number = Math.round(((new Date(obj.get(cfg.startAttribute))).getTime() - this.start) * this.multiplier);
        if(start < this.width) {
          if(start < 0) {
            start = 0;
          }
          let end: number = Math.round(((new Date(obj.get(cfg.endAttribute))).getTime() - this.start) * this.multiplier);
          if(end > this.width) end = this.width;
          let duration = end - start;
          let label = cfg.isBackground ? "" : obj.get(cfg.labelAttribute);
          let color = 'white';
          if(cfg.colorAttribute != null) {
            if(cfg.colorMap != null) {
              color = cfg.colorMap[obj.get(cfg.colorAttribute)];
            } else {
              color = obj.get(cfg.colorAttribute);
            }
          }
          spreads.push(new GanttSpread(obj.uid, label, start, duration, groupId, color, cfg.isBackground));
        }
      }
    }
    return spreads;
  }

}
