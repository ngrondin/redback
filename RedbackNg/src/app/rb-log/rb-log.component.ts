import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { ActionService } from 'app/services/action.service';

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
  @Input('linkobjectattribute') linkobjectattribute: string;
  @Input('linkuidattribute') linkuidattribute: string;
  
  @Output() posted: EventEmitter<any> = new EventEmitter();
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  public value: string; 
  public isEditable: boolean = true;

  constructor(
    private actionService: ActionService
  ) {
    super();
  }

  dataObserverInit() {
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
    if(this.active == true) {
      this.evalEditable();
    } else {
      this.isEditable = false;
    }
  }

  onActivationEvent(event: any) {
    if(this.active == true) {
      this.evalEditable();
    } else {
      this.isEditable = false;
    }
  }

  public get canClick() : boolean {
    return this.linkobjectattribute != null && this.linkuidattribute != null;
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
    console.log(event.keyCode);
    if(event.keyCode == 13) {
      this.post();
    }
  }

  post() {
    let msg: any = {};
    msg[this.entryattribute] = "'" + this.value.replace("'", "\\'") + "'";
    this.actionService.action(this.dataset, 'create', msg).subscribe();
    this.value = "";
  }

  clickCard(object: RbObject) {
    if(this.canClick) {
      let target = {};
      target['object'] = object.get(this.linkobjectattribute);
      target['filter'] = {uid: "'" + object.get(this.linkuidattribute) + "'"};
      this.navigate.emit(target);
    }

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
    console.log('eval editable = ' + this.isEditable);
  }
}
