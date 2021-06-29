import { Component, OnInit } from '@angular/core';
import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { Formatter } from 'app/helpers';
import { TimelineEntry, TimelineSeriesConfig } from './rb-timeline-models';

@Component({
  selector: 'rb-timeline',
  templateUrl: './rb-timeline.component.html',
  styleUrls: ['./rb-timeline.component.css']
})
export class RbTimelineComponent extends RbDataCalcComponent<TimelineSeriesConfig> {

  entries: TimelineEntry[] = [];
  formatter: Formatter = new Formatter();

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

  filterDataset() {
    
  }

  calc() {
    this.entries = [];
    this.iterateAllLists((object, config) => {
      let main = object.get(config.mainAttribute);
      let sub = object.get(config.subAttribute);
      let date = new Date(object.get(config.dateAttribute));
      this.entries.push(new TimelineEntry(date, main, sub));
    });
    this.entries.sort((a, b) => a.date.getTime() - b.date.getTime());
    let lastDate : Date = null;
    for(let entry of this.entries) {
      entry.showDatePart = lastDate != null && entry.date.getDate() == lastDate.getDate() ? false : true;
      lastDate = entry.date;
    }
    if(this.entries.length > 0) {
      this.entries[0].showTopLine = false;
      this.entries[this.entries.length - 1].showBottomLine = false;
    }
  }


}
