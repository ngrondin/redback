import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'rb-log',
  templateUrl: './rb-log.component.html',
  styleUrls: ['./rb-log.component.css']
})
export class RbLogComponent implements OnInit {

  @Input('size') size: number;
  @Input('editable') editable: boolean;
  @Input('list') list: RbObject[];
  @Input('userattribute') userattribute: string;
  @Input('dateattribute') dateattribute: string;
  @Input('entryattribute') entryattribute: string;
  @Input('categoryattribute') categoryattribute: string;
  
  @Output() posted: EventEmitter<any> = new EventEmitter();

  public value: string; 

  constructor() { }

  ngOnInit() {
  }

  getUserForItem(object: RbObject) : string {
    let str: string = object.data[this.userattribute];
    if(str == null || (str != null && str.length == 0)) {
      str = "Unknown user";
    }
    return str;
  }

  getDateForItem(object: RbObject) : string {
    let str : string = object.data[this.dateattribute];
    if(str == null || (str != null && str.length == 0)) {
      str = "Unknown date";
    } else {
      str = new Date(str).toLocaleString();
    }
    return str;
  }

  getEntryForItem(object: RbObject) : string {
    let str : string = object.data[this.entryattribute];
    if(str == null) {
      str = "";
    } else {
      str = str.split('\r\n').join('<br>').split('\t').join('&nbsp;&nbsp;');
    }
    return str;
  }
  

  post() {
    let msg: any = {};
    msg[this.entryattribute] = "'" + this.value.replace("'", "\\'") + "'";
    this.posted.emit(msg);
    this.value = "";
  }
}
