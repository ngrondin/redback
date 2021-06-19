import { EventEmitter, Input, Output } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { LegendEntryComponent } from '@swimlane/ngx-charts';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { FilterService } from 'app/services/filter.service';


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
    private filterService: FilterService
  ) {
    super();
  }

  dataObserverInit() {
    let dt = new Date();
    dt.setDate(1);
    this._year = dt.getFullYear();
    this._month = dt.getMonth();
    this.calcParams();
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
  
  redraw() {
    this.calcLists();
  }


  calcLists() {
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


