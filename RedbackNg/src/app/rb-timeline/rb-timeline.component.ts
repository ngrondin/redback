import { Component, HostBinding, Input, OnInit } from '@angular/core';
import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { RbObject } from 'app/datamodel';
import { Formatter } from 'app/helpers';
import { TimelineEntry, TimelineSeriesConfig } from './rb-timeline-models';

@Component({
  selector: 'rb-timeline',
  templateUrl: './rb-timeline.component.html',
  styleUrls: ['./rb-timeline.component.css']
})
export class RbTimelineComponent extends RbDataCalcComponent<TimelineSeriesConfig> {
  @Input('reverse') reverse: boolean = false;
  @Input('grow') grow: number;
  @Input('datefocus') datefocus: boolean = false;
  @HostBinding('style.flex-grow') get flexgrow() { return this.grow != null ? this.grow : 0;}

  entries: TimelineEntry[] = [];
  formatter: Formatter = new Formatter();
  showLevel: number = 0;
  maxLevel: number = 0;

  constructor() { 
    super();
    this.dofilter = false;
  }

  dataCalcInit() {
  }

  dataCalcDestroy() {
  }

  createSeriesConfig(json: any): TimelineSeriesConfig {
    return new TimelineSeriesConfig(json);
  }

  getFilterSortForSeries(cfg: TimelineSeriesConfig) : any {
    
  }

  calc() {
    this.entries = [];
    this.maxLevel = 0;
    this.iterateAllLists((object, config) => {
      let main = this.getMain(object, config);
      let sub = this.getSub(object, config);
      let date = new Date(object.get(config.dateAttribute));
      let level = config.level || 0;
      if(level > this.maxLevel) this.maxLevel = level;
      if(level <= this.showLevel) {
        this.entries.push(new TimelineEntry(date, main, sub, level));
      }
    });
    this.entries.sort((a, b) => a.date.getTime() - b.date.getTime());
    let lastDate : Date = null;
    for(let entry of this.entries) {
      if(lastDate != null && entry.date.getDate() == lastDate.getDate()) {
        entry.showDatePart = false;
        if(entry.date.getHours() == lastDate.getHours() && entry.date.getMinutes() == lastDate.getMinutes()) {
          entry.showTimePart = false;
        }
      }
      lastDate = entry.date;
    }
    if(this.entries.length > 0) {
      if(this.reverse) this.entries.reverse();
      this.entries[0].showTopLine = false;
      this.entries[this.entries.length - 1].showBottomLine = false;
    }

  }


  private getMain(object: RbObject, config: TimelineSeriesConfig) {
    return this.getValue(config.mainAttribute, config.mainExpression, object);
}

  public getSub(object: RbObject, config: TimelineSeriesConfig) {
    return this.getValue(config.subAttribute, config.subExpression, object);
  }

  private getValue(attr: string, expr: Function, obj: RbObject) {
      if(attr != null) {
          return obj.get(attr);
      } else if(expr != null) {
          return expr.call(window.redback, obj);
      } else {
          return null;
      }
  }

  public showMore() {
    this.showLevel++;
    this.calc();
  }

  public showLess() {
    this.showLevel--;
    this.calc();
  }

}
