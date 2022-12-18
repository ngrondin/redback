import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { Formatter } from 'app/helpers';
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
  @Input('editable') editable: any;
  @Input('linkobjectattribute') linkobjectattribute: string;
  @Input('linkuidattribute') linkuidattribute: string;
  
  @Output() posted: EventEmitter<any> = new EventEmitter();
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  public value: string; 
  public isEditable: boolean = false;
  private reachedBottom: boolean = false;

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
    let str: string = object.get(this.userattribute); 
    if(str == null || (str != null && str.length == 0)) {
      str = "Unknown user";
    }
    return str;
  }

  getDateForItem(object: RbObject) : string {
    let str : string = object.get(this.dateattribute);
    if(str == null || (str != null && str.length == 0)) {
      str = "Unknown date";
    } else {
      str = Formatter.formatDateTime(new Date(str));
    }
    return str;
  }

  getEntryForItem(object: RbObject) : string {
    let str : string = object.get(this.entryattribute);
    if(str == null) {
      str = "";
    } else {
      str = str.split('\r\n').join('<br>').split('\t').join('&nbsp;&nbsp;');
    }
    return str;
  }

  keydown(event: any) {
    //console.log(event.keyCode);
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
    if(this.editable == null || this.editable == true || this.editable == 'true') {
      this.isEditable = true;
    } else if(this.editable == false || this.editable == 'false') {
      this.isEditable = false;
    } else if(typeof this.editable == 'string') {
        let object = this.dataset != null ? this.dataset.selectedObject : null;
        let relatedObject = this.dataset != null ? this.dataset.relatedObject : null;
        if(!(this.editable.indexOf("relatedObject.") > -1 && relatedObject == null) && !(this.editable.indexOf("object.") > -1 && object == null)) {
            this.isEditable = eval(this.editable);            
        } else {
            this.isEditable = false;
        }
    }
    //console.log('eval editable = ' + this.isEditable);
  }

  onScroll(event) {
    if(event.currentTarget.scrollTop > Math.floor(event.currentTarget.scrollHeight - event.currentTarget.clientHeight - 10) && this.reachedBottom == false) {
      this.dataset.fetchNextPage();
      this.reachedBottom = true;
      setTimeout(() => {this.reachedBottom = false}, 1000);
    }
  }
}
