import { EventEmitter, Input, Output } from '@angular/core';
import { Component } from '@angular/core';
import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { RbObject } from 'app/datamodel';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { UserprefService } from 'app/services/userpref.service';
import { CalendarSeriesConfig, CalendarEntry } from './rb-calendar-models';

@Component({
  selector: 'rb-calendar',
  templateUrl: './rb-calendar.component.html',
  styleUrls: ['./rb-calendar.component.css']
})
export class RbCalendarComponent extends RbDataCalcComponent<CalendarSeriesConfig> {
  @Input('layers') layers: any[];
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  data: any = {};
  _year: number;
  _month: number;
  firstDay: number;
  startDate: Date;
  endDate: Date;

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
  layerOptions: any = null;
  _activeDatasets: any[] = [];

  constructor(
    private userprefService: UserprefService
  ) {
    super();
  }

  dataCalcInit() {
    let dt = new Date();
    dt.setDate(1);
    this._year = dt.getFullYear();
    this._month = dt.getMonth();
    this.calcParams(); 
    if(this.layers != null && this.layers.length > 0) {
      this.layerOptions = [];
      for(let item of this.layers) {
        this.layerOptions.push({display: item.label, value: item.datasets});
      }
      this.activeDatasets = this.layerOptions[0].value;
    } else {
      this.filterDataset();
    }
  }

  dataCalcDestroy() {

  }

  createSeriesConfig(json: any): CalendarSeriesConfig {
    return new CalendarSeriesConfig(json, this.userPref);
  }

  get userPref() : any {
    return this.id != null ? this.userprefService.getUISwitch("calendar", this.id) : null;
  }

  get activeSeries() : CalendarSeriesConfig[] {
    return this.seriesConfigs.filter(item => item.active);
  }

  get activeDatasets() : any {
    return this._activeDatasets;
  }

  set activeDatasets(val: any) {
    this._activeDatasets = val;
    for(let cfg of this.seriesConfigs) {
      if(val.indexOf(cfg.dataset) > -1) {
        cfg.active = true;
      } else {
        cfg.active = false;
      }
    }
    this.redraw();
    this.filterDataset();
  }

  get year():  number {
    return this._year;
  }

  set year(val: number) {
    this._year = val;
    this.calcParams();
    this.filterDataset();
  }

  get month() : number {
    return this._month;
  }

  set month(val: number) {
    this._month = val;
    this.calcParams();
    this.filterDataset();
  }

  calcParams() {
    let firstOfMonth = new Date(this.year, this.month, 1, 0, 0, 0, 0);
    this.firstDay = firstOfMonth.getDay();
    this.startDate = new Date(firstOfMonth.getTime() - (this.firstDay * 86400000));
    let firstofNextMonth = new Date((new Date(firstOfMonth.getTime()).setMonth(this.month + 1)));
    this.endDate = new Date(firstofNextMonth.getTime() + ((7 - firstofNextMonth.getDay()) * 86400000));
    let dayCount = (this.endDate.getTime() - this.startDate.getTime()) / 86400000;
    let weekCount = Math.ceil(dayCount / 7);
    this.weeks = [];
    this.data = {};
    let dt = new Date(this.startDate.getTime());
    for(var w = 0; w < weekCount; w++) {
      let days = [];
      for(var day of this.days) {
        let dateId = dt.getFullYear() + "-" + (dt.getMonth() + 1) + "-" + dt.getDate();
        days.push({
          id: dateId,
          label: dt.getDate().toString(), 
          mainMonth: dt.getMonth() == this._month ? true : false,
          filter:{
            $gt:"'" + dt.toISOString() + "'", 
            $lt:"'" + (new Date(dt.getTime() + 86400000)).toISOString() + "'"
          }
        });
        dt.setDate(dt.getDate() + 1);
      }
      this.weeks.push(days);
    }
  }

  filterDataset() {
    for(let cfg of this.activeSeries) {
      let filter = {};
      filter[cfg.dateAttribute] = {
        $gt: "'" + this.startDate.toISOString() + "'",
        $lt: "'" + this.endDate.toISOString() + "'"
      }
      if(this.datasetgroup != null) {
        this.datasetgroup.datasets[cfg.dataset].filterSort({filter: filter});
      } else {
        this.dataset.filterSort({filter: filter});
      }
    }
  }

  calc() {
    this.data = {};
    for(let cfg of this.activeSeries) {
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
        } else if(cfg.color != null) {
          color = cfg.color;
        }       
        let entry = new CalendarEntry(obj.uid, label, date, color, obj, cfg);
        this.putEntryInData(entry);
      }
    }
  }
  
  putEntryInData(entry: CalendarEntry) {
    if(entry.date.getTime() >= this.startDate.getTime() && entry.date.getTime() <= this.endDate.getTime()) {
      let dateId = entry.date.getFullYear() + "-" + (entry.date.getMonth() + 1) + "-" + entry.date.getDate();
      if(this.data[dateId] == null) this.data[dateId] = [];
      this.data[dateId].push(entry);
     }
  }

  clickDay(day: any) {
    if(this.activeSeries.length > 0) {
      let cfg = this.activeSeries[0];
      let ds: RbDatasetComponent = this.datasetgroup != null ? this.datasetgroup.datasets[cfg.dataset] : this.dataset;
      let filter = Object.assign({}, ds.mergedFilter);
      filter[cfg.dateAttribute] = day.filter;
      let target = {
        object: ds.object,
        filter: filter,
        search: ds.searchString
      };
      this.navigate.emit(target);
    }
  }

  clickItem(item: CalendarEntry) {
    let object = item.object;
    if(object != null) {
      let target = {};
      if(item.config.linkView != null) {
        target['view'] = item.config.linkView;
      } else {
        target['object'] = object.objectname;
      }
      if(item.config.linkAttribute != null) {
        target['filter'] = {uid: "'" + object.get(item.config.linkAttribute) + "'"};
      } else {
        target['filter'] = {uid: "'" + object.uid + "'"};
      }
      this.navigate.emit(target);
    }
  }


}


