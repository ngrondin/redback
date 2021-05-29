import { EventEmitter, Input, Output } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';


class CalendarSeriesConfig {
  dataset: string;
  dateAttribute: string;
  durationAttribute: string;
  labelAttribute: string;
  colorAttribute: string;
  colorMap: any;
  color: string;
  linkAttribute: string;
  linkView: string;
  modal: string;
  canEdit: boolean;
  active: boolean = true;

  constructor(json: any) {
    this.dataset = json.dataset;
    this.dateAttribute = json.dateattribute;
    this.durationAttribute = json.durationattribute;
    this.labelAttribute = json.labelattribute;
    this.colorAttribute = json.colorattribute;
    this.colorMap = json.colormap;
    this.color = json.color;
    this.linkAttribute = json.linkattribute;
    this.linkView = json.linkview;
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
  @Input('layers') layers: any[];
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
  layerOptions: any = null;
  _activeDatasets: any[] = [];

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

  get isLoading() : boolean {
    return this.dataset != null ? this.dataset.isLoading : this.datasetgroup != null ? this.datasetgroup.isLoading : false;
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
    this.filterDataset();
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


  filterDataset() {
    let startDate = new Date(this.year, this.month, 1, 0, 0, 0, 0);
    let endDate = new Date((new Date(startDate.getTime())).setMonth(startDate.getMonth() + 1));
    for(let cfg of this.activeSeries) {
      let filter = {};
      filter[cfg.dateAttribute] = {
        $gt: "'" + startDate.toISOString() + "'",
        $lt: "'" + endDate.toISOString() + "'"
      }
      if(this.datasetgroup != null) {
        this.datasetgroup.datasets[cfg.dataset].filterSort({filter: filter});
      } else {
        this.dataset.filterSort({filter: filter});
      }
    }
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
      let filter = {};
      filter[cfg.dateAttribute] = {
        $gt: "'" + startDate.toISOString() + "'",
        $lt: "'" + endDate.toISOString() + "'"
      }      
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


