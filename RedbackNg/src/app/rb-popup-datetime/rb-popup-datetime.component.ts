import { Component, OnInit, Output, EventEmitter, Inject } from '@angular/core';
import { CONTAINER_DATA } from 'app/tokens';
import { OverlayRef } from '@angular/cdk/overlay';
import { DataService } from 'app/data.service';

export class DateTimePopupConfig {
  initialDate: Date;
  datePart: boolean;
  hourPart: boolean;
  minutePart: boolean;
}

@Component({
  selector: 'rb-popup-datetime',
  templateUrl: './rb-popup-datetime.component.html',
  styleUrls: ['./rb-popup-datetime.component.css']
})
export class RbPopupDatetimeComponent implements OnInit {

  @Output() selected: EventEmitter<any> = new EventEmitter();

  currentPart: number;
  year: number;
  month: number;
  day: number;
  hour: number;
  minute: number;
  firstDayOfMonth: number;
  numberOfWeeks: number;
  calendar: any[];
  daysOfWeek: any[] = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];
  monthsOfYear: string[] = ['January', 'Feburary', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
  hoursOfDay: number[] = [0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22];
  minutesOfHour: number[] = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];

  constructor(
    @Inject(CONTAINER_DATA) public config: DateTimePopupConfig, 
    public overlayRef: OverlayRef,
    private dataService: DataService
  ) { }

  ngOnInit() {
    this.year = this.config.initialDate.getFullYear();
    this.month = this.config.initialDate.getMonth();
    this.day = this.config.initialDate.getDate();
    this.hour = this.config.initialDate.getHours();
    this.minute = this.config.initialDate.getMinutes();
    this.currentPart = (this.config.datePart ? 0 : (this.config.hourPart ? 1 : 2));
    this.calcCalendarSettings();
  }

  public getDate() : Date {
    let newDate = new Date();
    newDate.setFullYear(this.year);
    newDate.setMonth(this.month);
    newDate.setDate(this.day);
    newDate.setHours(this.hour);
    newDate.setMinutes(this.minute);
    newDate.setSeconds(0);
    newDate.setMilliseconds(0);
    return newDate;
  }

  public calcCalendarSettings() {
    let firstOfTheMonth = new Date(this.getDate().setDate(1));
    let firstOfNextMonth = new Date((new Date(firstOfTheMonth.getTime() + 2678400000)).setDate(1));
    let daysInMonth = (firstOfNextMonth.getTime() - firstOfTheMonth.getTime()) / 86400000;
    this.firstDayOfMonth = firstOfTheMonth.getDay() - 1;
    this.numberOfWeeks = (daysInMonth + this.firstDayOfMonth + 1 ) / 7;
    let day : number = -this.firstDayOfMonth;
    this.calendar = [];
    for(let w = 0; w < this.numberOfWeeks; w++) {
      let week = [];
      for(let d = 0; d < 7; d++) {
        if(day < 1 || day > daysInMonth)
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
    if(this.month > 11) {
      this.year++;
      this.month = 0;
    }
    this.calcCalendarSettings();
  }

  public previousMonth() {
    this.month--;
    if(this.month < 0) {
      this.year--;
      this.month = 11;
    }
    this.calcCalendarSettings();
  }

  public selectDate(dayOfMonth: number)
  {
    this.day = dayOfMonth;
    this.nextPart();
  }

  public selectHour(event: any)
  {
    this.hour = Math.floor(((this.getAngleFromClick(event) + 7.5) % 360) / 15);
    this.nextPart();
  }

  public selectMinute(event: any)
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
      this.selected.emit(this.getDate());
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

}
