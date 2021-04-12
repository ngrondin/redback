import { EventEmitter, Input, Output } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { DateAdapter } from '@angular/material';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';


class CalendarSeriesConfig {
  dataset: string;
  dateAttribute: string;
  durationAttribute: string;
  labelAttribute: string;
  colorAttribute: string;
  colorMap: any;
  modal: string;
  canEdit: boolean;

  constructor(json: any) {
    this.dataset = json.dataset;
    this.dateAttribute = json.dateattribute;
    this.durationAttribute = json.durationattribute;
    this.labelAttribute = json.labelattribute;
    this.colorAttribute = json.colorattribute;
    this.colorMap = json.colormap;
    this.modal = json.modal;
  }
}

class CalendarEntry {
  id: string;
  label: string;
  date: Date;
  color: string;
  object: RbObject;
  config: CalendarSeriesConfig;

  constructor(i: string, l: string, d: Date, c: string, o: RbObject, cfg: CalendarSeriesConfig) {
    this.id = i;
    this.label = l;
    this.date = d;
    this.color = c;
    this.object = o;
    this.config = cfg;
  }

}


@Component({
  selector: 'rb-calendar',
  templateUrl: './rb-calendar.component.html',
  styleUrls: ['./rb-calendar.component.css']
})
export class RbCalendarComponent extends RbDataObserverComponent {
  @Input('series') series: any[];
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  seriesConfigs: CalendarSeriesConfig[];
  data: any = {};
  _year: number;
  _month: number;
  firstDay: number;
  days: any = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  months: any = [
    {display: "January", value: 0},
    {display: "Febuary", value: 1},
    {display: "March", value: 2},
    {display: "April", value: 3},
    {display: "May", value: 4},
    {display: "June", value: 5},
    {display: "July", value: 6},
    {display: "August", value: 7},
    {display: "September", value: 8},
    {display: "October", value: 9},
    {display: "November", value: 10},
    {display: "December", value: 11}
  ];
  weeks: any = [];

  constructor() {
    super();
  }

  dataObserverInit() {
    let dt = new Date();
    dt.setDate(1);
    this._year = dt.getFullYear();
    this._month = dt.getMonth();
    if(this.series != null) {
      this.seriesConfigs = [];
      for(let item of this.series) {
        this.seriesConfigs.push(new CalendarSeriesConfig(item));
      }
    }    
    this.filterDataset();
  }

  dataObserverDestroy() {
    
  }

  onDatasetEvent(event: string) {
    this.redraw();
  }

  onActivationEvent(state: boolean) {
    this.redraw();
  }

  get selectedObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : this.datasetgroup != null ? this.datasetgroup.selectedObject : null;
  }

  get list(): RbObject[] {
    return this.dataset != null ? this.dataset.list : null;
  }

  get lists(): any {
    return this.datasetgroup != null ? this.datasetgroup.lists : null;
  }

  get isLoading() : boolean {
    return this.dataset != null ? this.dataset.isLoading : this.datasetgroup != null ? this.datasetgroup.isLoading : false;
  }

  get year():  number {
    return this._year;
  }

  set year(val: number) {
    this._year = val;
    this.filterDataset();
  }

  get month() : number {
    return this._month;
  }

  set month(val: number) {
    this._month = val;
    this.filterDataset();
  }

  redraw() {
    this.calcParams();
    this.calcLists();
  }

  calcParams() {
    let firstOfMonth = new Date(this.year, this.month, 1, 0, 0, 0, 0);
    let firstofNextMonth = new Date((new Date(firstOfMonth.getTime()).setMonth(this.month + 1)));
    this.firstDay = firstOfMonth.getDay();
    let dayCount = Math.ceil(((firstofNextMonth.getTime() - firstOfMonth.getTime()) / 86400000) - 0.1);
    let weekCount = Math.ceil((dayCount + this.firstDay) / 7);
    this.weeks = [];
    this.data = {};
    let i = 1 - this.firstDay;
    for(var w = 0; w < weekCount; w++) {
      let days = [];
      for(var day of this.days) {
        if(i > 0 && i <= dayCount) {
          let dayOfMonthStr = i.toString();
          days.push(dayOfMonthStr);
          this.data[dayOfMonthStr] = [];
        } else {
          days.push(null);
        }
        i++;
      }
      this.weeks.push(days);
    }
  }

  calcLists() {
    for(let cfg of this.seriesConfigs) {
      let list: RbObject[] = this.lists != null ? this.lists[cfg.dataset] : this.list;
      for(var i in list) {
        let obj = list[i];
        let label = obj.get(cfg.labelAttribute);
        let date = new Date(obj.get(cfg.dateAttribute));
        let color = 'white';
        if(cfg.colorAttribute != null) {
          if(cfg.colorMap != null) {
            color = cfg.colorMap[obj.get(cfg.colorAttribute)];
          } else {
            color = obj.get(cfg.colorAttribute);
          }
        }        
        let entry = new CalendarEntry(obj.uid, label, date, color, obj, cfg);
        this.putEntryInData(entry);
      }
    }
  }
  
  putEntryInData(entry: CalendarEntry) {
    if(entry.date.getFullYear() == this.year && entry.date.getMonth() == this.month) {
      let dayOfMonth = entry.date.getDate();
      if(this.data[dayOfMonth] != null) {
        this.data[dayOfMonth].push(entry);
      } else {
        alert('boom');
      }
     }
  }

  clickDay(day: any) {
    let startDate = new Date(this.year, this.month, day, 0, 0, 0, 0);
    let endDate = new Date((new Date(startDate.getTime())).setDate(startDate.getDate() + 1));
    if(this.seriesConfigs.length > 0) {
      let cfg = this.seriesConfigs[0];
      let f1 = {};
      f1[cfg.dateAttribute] = {$gt: startDate.toISOString()};
      let f2 = {};
      f2[cfg.dateAttribute] = {$lt: endDate.toISOString()};
      let filter = {$and:[f1, f2]};
      let objectname = null;
      if(this.datasetgroup != null) {
        objectname = this.datasetgroup.datasets[cfg.dataset].object;
      } else {
        objectname = this.dataset.object;
      }
      let target = {
        object: objectname,
        filter: filter
      };
      this.navigate.emit(target);
    }
  }

  clickItem(item: CalendarEntry) {
    let object = item.object;
    if(object != null) {
      let target = {
        object: object.objectname,
        filter: {uid: "'" + object.uid + "'"}
      };
      this.navigate.emit(target);
    }
  }



  filterDataset() {
    let startDate = new Date(this.year, this.month, 1, 0, 0, 0, 0);
    let endDate = new Date((new Date(startDate.getTime())).setMonth(startDate.getMonth() + 1));
    for(let cfg of this.seriesConfigs) {
      let f1 = {};
      f1[cfg.dateAttribute] = {$gt: "'" + startDate.toISOString() + "'"};
      let f2 = {};
      f2[cfg.dateAttribute] = {$lt: "'" + endDate.toISOString() + "'"};
      let filter = {$and:[f1, f2]};
      if(this.datasetgroup != null) {
        this.datasetgroup.datasets[cfg.dataset].filterSort({filter: filter});
      } else {
        this.dataset.filterSort({filter: filter});
      }
    }

  }
}


