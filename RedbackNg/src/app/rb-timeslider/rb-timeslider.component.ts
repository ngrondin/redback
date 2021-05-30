import { Component, EventEmitter, HostBinding, Input, OnInit, Output, ViewChild, ViewContainerRef } from '@angular/core';
import { DragService } from 'app/services/drag.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'rb-timeslider',
  templateUrl: './rb-timeslider.component.html',
  styleUrls: ['./rb-timeslider.component.css']
})
export class RbTimesliderComponent implements OnInit {
  @ViewChild('line', { read: ViewContainerRef, static: true }) line: ViewContainerRef;
  @ViewChild('thumb', { read: ViewContainerRef, static: true }) thumb: ViewContainerRef;
  @Input('mindate') minDate: Date;
  @Input('maxdate') maxDate: Date;
  @Input('currentdate') currentDate: Date;
  @Output('currentdateChange') currentDateChange = new EventEmitter();
  holding: boolean = false;
  clientOrigin: number = 0;
  positionOrigin: number = 0;
  position: number = 0;
  thumbLeft: number = -20;

  constructor(
  ) { }

  ngOnInit(): void {
    this.currentDate = this.minDate;
  }

  public get thumbCursor() : string {
    return this.holding ? "grab" : "pointer";
  }

  public mousedown(event: any) {
    this.holding = true;
    this.clientOrigin = event.clientX;
    this.positionOrigin = this.position;
  }

  public mousemove(event: any) {
    if(this.holding) {
      let cPos = event.clientX;
      let lineWidth = this.line.element.nativeElement.clientWidth;
      let thumbWidth = this.thumb.element.nativeElement.clientWidth;
      let ratio = lineWidth / 100;
      this.position = ((cPos - this.clientOrigin) / ratio) + this.positionOrigin; 
      if(this.position > 100) this.position = 100;
      if(this.position < 0) this.position = 0;
      this.thumbLeft = (this.position * ratio) - (thumbWidth / 2);
      let min = this.minDate.getTime();
      let newDate = new Date(min + ((this.maxDate.getTime() - min) * this.position / 100)); 
      this.currentDateChange.emit(newDate);
    }
  }

  public mouseup(event: any) {
    this.holding = false;
  }


}
