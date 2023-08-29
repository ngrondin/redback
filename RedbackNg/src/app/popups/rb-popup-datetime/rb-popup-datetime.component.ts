import { Component, OnInit, Output, EventEmitter, Inject } from '@angular/core';
import { CONTAINER_DATA } from 'app/tokens';
import { OverlayRef } from '@angular/cdk/overlay';
import { DataService } from 'app/services/data.service';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';

export class DateTimePopupConfig {
  initialDate: Date;
  datePart: boolean;
  hourPart: boolean;
  minutePart: boolean;
}

@Component({
  selector: 'rb-popup-datetime',
  templateUrl: './rb-popup-datetime.component.html',
  styleUrls: ['./rb-popup-datetime.component.css', '../rb-popup/rb-popup.component.css']
})
export class RbPopupDatetimeComponent extends RbPopupComponent implements OnInit {

  @Output() selected: EventEmitter<any> = new EventEmitter();

  currentPart: number;
  year: number;
  month: number;
  day: number;
  hour: number;
  minute: number;
  daysInMonth: number;
  firstDayOfMonth: number;
  numberOfWeeks: number;
  calendar: any[];
  daysOfWeek: any[] = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];
  monthsOfYear: string[] = ['January', 'Feburary', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
  hoursOfDay: any[]; //number[] = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23];
  minutesOfHour: any[]; //number[] = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];

  constructor(
    @Inject(CONTAINER_DATA) public config: DateTimePopupConfig, 
    public overlayRef: OverlayRef,
    private dataService: DataService
  ) {
    super();
  }

  ngOnInit() {
    this.year = this.config.initialDate.getFullYear();
    this.month = this.config.initialDate.getMonth();
    this.day = this.config.initialDate.getDate();
    this.hour = this.config.initialDate.getHours();
    this.minute = this.config.initialDate.getMinutes();
    this.currentPart = (this.config.datePart ? 0 : (this.config.hourPart ? 1 : 2));
    this.hoursOfDay = [];
    for(let i = 0; i < 24; i++) {
      this.hoursOfDay.push({
        val: i,
        x: Math.sin(2 * Math.PI * i / 24),
        y: -Math.cos(2 * Math.PI * i / 24)
      })
    }
    this.minutesOfHour = [];
    for(let i = 0; i < 60; i += 5) {
      this.minutesOfHour.push({
        val: i,
        x: Math.sin(2 * Math.PI * i / 60),
        y: -Math.cos(2 * Math.PI * i / 60)
      })
    }    
    this.calcCalendarSettings();
  }

  public getOutput() : any {
    return new Date(this.year, this.month, this.day, this.config.hourPart == true ? this.hour : 0, this.config.minutePart == true ? this.minute : 0, 0, 0);
  }

  public validate() {
    if(this.month > 11) {
      this.year++;
      this.month = 0;
    }
    if(this.month < 0) {
      this.year--;
      this.month = 11;
    }
    let daysInMonth = new Date(this.year, this.month + 1, 0).getDate();
    while(this.day > daysInMonth) {
      this.day--;
    }
  }

  public calcCalendarSettings() {
    let firstOfTheMonth = new Date(this.getOutput().setDate(1));
    this.daysInMonth = new Date(this.year, this.month + 1, 0).getDate();
    this.firstDayOfMonth = firstOfTheMonth.getDay() - 1;
    this.numberOfWeeks = (this.daysInMonth + this.firstDayOfMonth + 1 ) / 7;
    let day : number = -this.firstDayOfMonth;
    this.calendar = [];
    for(let w = 0; w < this.numberOfWeeks; w++) {
      let week = [];
      for(let d = 0; d < 7; d++) {
        if(day < 1 || day > this.daysInMonth)
          week.push("");
        else
          week.push(day);
        day++;
      }
      this.calendar.push(week);
    }
  }

  public nextMonth() {
    this.month++;
    this.validate();
    this.calcCalendarSettings();
  }

  public previousMonth() {
    this.month--;
    this.validate();
    this.calcCalendarSettings();
  }

  public selectDate(dayOfMonth: number)
  {
    this.day = dayOfMonth;
    this.nextPart();
  }

  public selectHour(hour: number)
  {
    this.hour = hour;
    this.nextPart();
  }

  public selectHourFromBack(event: any)
  {
    this.hour = Math.floor(((this.getAngleFromClick(event) + 7.5) % 360) / 15);
    this.nextPart();
  }

  public selectMinute(minute: number)
  {
    this.minute = minute;
    this.nextPart();
  }

  public selectMinuteFromBack(event: any)
  {
    this.minute = Math.floor(((this.getAngleFromClick(event) + 3) % 360) / 6);
    this.nextPart();
  }

  public nextPart() {
    this.currentPart++;
    if(this.currentPart == 1 && this.config.hourPart == false)
      this.currentPart++;
    if(this.currentPart == 2 && this.config.minutePart == false)
      this.currentPart++;
    if(this.currentPart == 3) 
      this.selected.emit(this.getOutput());
  }

  public getAngleFromClick(event: any) : number {
    let x = event.layerX - (event.target.clientWidth / 2);
    let y = event.layerY - (event.target.clientHeight / 2);
    let angle = 0;
    if(x == 0){
      if(y >= 0)
        angle = 180;
      else
        angle = 0;
    } else {
      angle = 57.29 * Math.atan(y / x);
      if(x < 0)
        angle = angle + 270;
      else
        angle = angle + 90;
    }
    return angle;
  }

  public getHighlighted(): any {
    return this.getOutput()
  }

  public setSearch(val: string) {
    try {
      let dt = val != null && val.length > 0 ? new Date((this.config.datePart == false ? '1970-01-01 ' : '') + val) : new Date();
      this.year = dt.getFullYear();
      this.month = dt.getMonth();
      this.day = dt.getDate();
      this.hour = dt.getHours();
      this.minute = dt.getMinutes();
    } catch(err) {
    }
  }

  public keyTyped(keyCode: number) {
    if(keyCode == 13) {
      this.selected.emit(this.getOutput());
    }
  }
}
