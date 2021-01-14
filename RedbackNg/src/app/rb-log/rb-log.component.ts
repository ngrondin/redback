import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'rb-log',
  templateUrl: './rb-log.component.html',
  styleUrls: ['./rb-log.component.css']
})
export class RbLogComponent extends RbDataObserverComponent {
  @Input('size') size: number;
  @Input('userattribute') userattribute: string;
  @Input('dateattribute') dateattribute: string;
  @Input('entryattribute') entryattribute: string;
  @Input('categoryattribute') categoryattribute: string;
  @Input('editable') editable: string;
  
  @Output() posted: EventEmitter<any> = new EventEmitter();

  public value: string; 
  public isEditable: boolean = true;

  constructor() {
    super();
  }

  get list() : RbObject[] {
    return this.dataset != null ? this.dataset.list : null;
  }
  
  dataObserverInit() {
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
  }

  onActivationEvent(event: any) {
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

  keydown(event: any) {
    if(event.keyCode == 13) {
      this.post();
    }
  }

  post() {
    let msg: any = {};
    msg[this.entryattribute] = "'" + this.value.replace("'", "\\'") + "'";
    this.dataset.action('create', msg);
    this.value = "";
  }

  public evalEditable() {
    if(this.editable == null) {
      this.isEditable = true;
    } else if(this.editable == 'true') {
      this.isEditable = true;
    } else if(this.editable == 'false') {
        this.isEditable = false;
    } else {
        let relatedObject = this.dataset != null ? this.dataset.relatedObject : null;
        if(!(this.editable.indexOf("relatedObject.") > -1 && relatedObject == null)) {
            this.isEditable = eval(this.editable);            
        } else {
            this.isEditable = false;
        }
    }
  }
}
