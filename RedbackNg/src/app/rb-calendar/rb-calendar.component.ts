import { EventEmitter, Input, Output } from '@angular/core';
import { Component } from '@angular/core';
import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { RbObject } from 'app/datamodel';
import { ValueComparator } from 'app/helpers';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { RbSearchTarget } from 'app/rb-search/rb-search-target';
import { FilterService } from 'app/services/filter.service';
import { UserprefService } from 'app/services/userpref.service';
import { CalendarSeriesConfig, CalendarEntry } from './rb-calendar-models';
import { NavigateService } from 'app/services/navigate.service';

@Component({
  selector: 'rb-calendar',
  templateUrl: './rb-calendar.component.html',
  styleUrls: ['./rb-calendar.component.css']
})
export class RbCalendarComponent extends RbDataCalcComponent<CalendarSeriesConfig>  implements RbSearchTarget {
  @Input('layers') layers: any[];
  @Input('filter') filterConfig: any;
  //@Output() navigate: EventEmitter<any> = new EventEmitter();

  _mode: string;
  data: any = {};
  _year: number;
  _month: number;
  _weekStarting: Date;
  startDate: Date;
  endDate: Date;
  mondayFirst: boolean = false;

  modes: any[] = [
    {display: "Month", value: "month"},
    {display: "Week", value: "week"}
  ];
  months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
  days: any = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  monthOptions: any = this.months.map((item, i) => ({display: item, value: i}));
  weeksOfThisMonth: any = [];
  weeks: any = [];
  layerOptions: any = null;
  _activeDatasets: any[] = [];

  userFilter: any = {};
  userSearchString: string;

  constructor(
    private userprefService: UserprefService,
    private filterService: FilterService,
    private navigateService: NavigateService
  ) {
    super();
  }

  dataCalcInit() {
    let dt = new Date();
    dt.setDate(1);
    this._mode = this.userPref.mode != null ? this.userPref.mode : 'month';
    if(this.userPref.mondayfirst == true) {
      this.mondayFirst = true;
      this.days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    }
    this._year = dt.getFullYear();
    this._month = dt.getMonth();
    this._weekStarting = this.findStartOfTheWeek(new Date());
    this.mondayFirst = this.userPref.mondayfirst != null ? this.userPref.mondayfirst : false;
    this.calcDisplayParams();
    if(this.layers != null && this.layers.length > 0) {
      this.layerOptions = [];
      let initialLayer = null;
      let preferedLayer = this.userPref.layer != null ? this.userPref.layer : null;
      for(let item of this.layers) {
        this.layerOptions.push({display: item.label, value: item.datasets});
        if(preferedLayer != null && preferedLayer.toString() == item.datasets.toString()) initialLayer = item.datasets;
      }
      this.activeDatasets = initialLayer ?? this.layerOptions[0].value;
    } else {
      this.updateData();
    }
  }

  dataCalcDestroy() {

  }

  createSeriesConfig(json: any): CalendarSeriesConfig {
    return new CalendarSeriesConfig(json, this.userPref);
  }

  get userPref() : any {
    return (this.id != null ? this.userprefService.getCurrentViewUISwitch("calendar", this.id) : null) ?? {};
  }

  get objectname() : string {
    return this.dataset != null ? this.dataset.objectname : this.datasetgroup != null && this.activeSeries.length > 0 ? this.datasetgroup.datasets[this.activeSeries[0].dataset].objectname : null;
  }


  get mode(): string {
    return this._mode;
  }

  set mode(m: string) {
    this._mode = m;
    this.calcDisplayParams();
    this.updateData(true);
    this.userprefService.setUISwitch('user', 'calendar', this.id, {mode: m});
  }

  get year():  number {
    return this._year;
  }

  set year(val: number) {
    this._year = val;
    this.resetWeekStarting();
    this.calcDisplayParams();
    this.updateData(true);
  }

  get month() : number {
    return this._month;
  }

  set month(val: number) {
    this._month = val;
    this.resetWeekStarting();
    this.calcDisplayParams();
    this.updateData(true);
  }

  get weekStarting() : Date {
    return this._weekStarting;
  }

  set weekStarting(val: Date) {
    this._weekStarting = val;
    this.calcDisplayParams();
    this.updateData(true);
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
    this.userprefService.setUISwitch('user', 'calendar', this.id, {layer: val});
    this.updateData(true);
  }

  findStartOfTheWeek(dt: Date) : Date {
    let dtMidnight = new Date(dt.getFullYear(), dt.getMonth(), dt.getDate(), 0, 0, 0, 0);
    let sow = new Date(dtMidnight.getTime() - ((dtMidnight.getDay() - (this.mondayFirst ? 1 : 0)) * 86400000));
    return sow;
  }

  resetWeekStarting() {
    let now = new Date();
    if(now.getFullYear() == this._year && now.getMonth() == this._month) {
      this._weekStarting = this.findStartOfTheWeek(now);
    } else {
      let dt = new Date(this._year, this._month, 1, 0, 0, 0, 0);
      this._weekStarting = this.findStartOfTheWeek(dt);
    }
  }

  calcDisplayParams() {
    this.weeksOfThisMonth = [];
    this.weeks = [];
    let firstOfMonth = new Date(this.year, this.month, 1, 0, 0, 0, 0);
    let dt = this.findStartOfTheWeek(firstOfMonth);
    let monthStart = dt;
    while(dt.getMonth() == this.month - 1 || dt.getMonth() == this.month || (dt.getMonth() == 11 && this.month == 0)) {
      this.weeksOfThisMonth.push({display: dt.getDate() + " " + this.months[dt.getMonth()], value: dt});
      dt = new Date(dt.getTime() + (7 * 86400000));
    }
    let monthEnd = dt;    
    if(this.mode == 'week') {
      this.startDate = this.weekStarting;
      this.endDate = new Date(this.startDate.getTime() + (7 * 86400000));
    } else if(this.mode == 'month') {
      this.startDate = monthStart;
      this.endDate = monthEnd;;
    }
    let dayCount = (this.endDate.getTime() - this.startDate.getTime()) / 86400000;
    let weekCount = Math.ceil(dayCount / 7);
    dt = new Date(this.startDate.getTime());
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

  getFilterSortForSeries(config: CalendarSeriesConfig) : any {
    let filter = {};
    filter = this.filterService.mergeFilters(filter, this.userFilter);
    filter[config.dateAttribute] = {
      $gt: "'" + this.startDate.toISOString() + "'",
      $lt: "'" + this.endDate.toISOString() + "'"
    }
    return {filter: filter, search: this.userSearchString};
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
        let icon = null;
        if(cfg.colorAttribute != null) {
          if(cfg.colorMap != null) {
            color = cfg.colorMap[obj.get(cfg.colorAttribute)];
          } else {
            color = obj.get(cfg.colorAttribute);
          }
        } else if(cfg.color != null) {
          color = cfg.color;
        }  
        if(cfg.icon != null) { // TODO: Placeholder for icon map, same as color
          icon = cfg.icon;
        }     
        let entry = new CalendarEntry(obj.uid, icon, label, date, color, obj, cfg);
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

  next() {
    if(this.mode == 'month') {
      var newMonth = this.month + 1;
      if(newMonth > 11) {
        this._year = this._year + 1;
        newMonth = 0;
      }
      this.month = newMonth;
    } else if(this.mode == 'week') {
      var newWeekStarting = new Date(this.weekStarting.getTime() + (7 * 86400000));
      if(newWeekStarting.getMonth() != this.month) {
        this._year = newWeekStarting.getFullYear();
        this._month = newWeekStarting.getMonth();
      }
      this.weekStarting = newWeekStarting;
    }
  }

  previous() {
    if(this.mode == 'month') {
      var newMonth = this.month - 1;
      if(newMonth < 0) {
        this._year = this._year - 1;
        newMonth = 11;
      }
      this.month = newMonth;
    } else if(this.mode == 'week') {
      var newWeekStarting = new Date(this.weekStarting.getTime() - (7 * 86400000));
      if(newWeekStarting.getMonth() != this.month) {
        this._year = newWeekStarting.getFullYear();
        this._month = newWeekStarting.getMonth();
      }
      this.weekStarting = newWeekStarting;
    }
  }

  clickDay(day: any) {
    if(this.activeSeries.length > 0) {
      let cfg = this.activeSeries[0];
      let ds: RbDatasetComponent = this.datasetgroup != null ? this.datasetgroup.datasets[cfg.dataset] : this.dataset;
      let filter = Object.assign({}, ds.mergedFilter);
      filter[cfg.dateAttribute] = day.filter;
      let target = {
        objectname: ds.objectname,
        filter: filter,
        search: ds.userSearch
      };
      this.navigateService.navigateTo(target);
    }
  }

  clickItem(item: CalendarEntry) {
    let object = item.object;
    if(object != null) {
      let target = {};
      if(item.config.linkView != null) {
        target['view'] = item.config.linkView;
      } else {
        target['objectname'] = object.objectname;
      }
      if(item.config.linkAttribute != null) {
        target['filter'] = {uid: "'" + object.get(item.config.linkAttribute) + "'"};
      } else {
        target['filter'] = {uid: "'" + object.uid + "'"};
      }
      this.navigateService.navigateTo(target);
    }
  }

  filterSort(event: any) : boolean {
    if(('filter' in event && ValueComparator.notEqual(event.filter, this.userFilter))
     || ('search' in event && event.search != this.userSearchString)) {
      if('filter' in event) this.userFilter = event.filter;
      if('search' in event) this.userSearchString = event.search;
      return this.updateData(true);
    } else {
      return false;
    }
  }

  public getBaseSearchFilter(): any {
    let filter = {};
    filter[this.activeSeries[0].dateAttribute] = {
      $gt: "'" + this.startDate.toISOString() + "'",
      $lt: "'" + this.endDate.toISOString() + "'"
    }
    return filter;
  }

}


