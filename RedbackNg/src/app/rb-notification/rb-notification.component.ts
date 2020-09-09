import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { ApiService } from 'app/api.service';
import { DataService } from 'app/data.service';

@Component({
  selector: 'rb-notification',
  templateUrl: './rb-notification.component.html',
  styleUrls: ['./rb-notification.component.css']
})
export class RbNotificationComponent implements OnInit {

  @Output() navigate: EventEmitter<any> = new EventEmitter();

  public count: number;
  public list: any[];
  public loading: boolean;

  constructor(
    private apiService: ApiService,
    private dataService: DataService
  ) { 
    this.count = 0;
    this.loading = false;
    this.list = [];
  }

  ngOnInit() {
    this.getCount();
  }

  public getCount() {
    this.apiService.getAssignmentCount('exception').subscribe(resp => this.setCount(resp));
  }

  public getList() {
    this.loading = true;
    this.apiService.listAssignments('exception', {}).subscribe(resp => this.setList(resp));
  }

  public setCount(data: any) {
    this.count = data.count;
  }

  public setList(data: any) {
    this.list = data.result;
    this.loading = false;
  }

  public selectNotification(notification: any) {
    let evt = {
      object : (notification.data.object != null ? notification.data.object : (notification.data.objectname != null ? notification.data.objectname : null)),
      filter : {
        uid : "'" + notification.data.uid + "'"
      },
      reset : true
    }
    this.navigate.emit(evt);
  }
}
