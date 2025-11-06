import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { NavigateEvent, RbObject } from 'app/datamodel';
import { Formatter } from 'app/helpers';
import { ActionService } from 'app/services/action.service';
import { DataService } from 'app/services/data.service';
import { NavigateService } from 'app/services/navigate.service';

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
  @Input('categories') categories: any;
  @Input('editable') editable: any;
  @Input('linkobjectattribute') linkobjectattribute: string;
  @Input('linkuidattribute') linkuidattribute: string;
  
  @Output() posted: EventEmitter<any> = new EventEmitter();

  public value: string; 
  public isEditable: boolean = false;
  private reachedBottom: boolean = false;

  constructor(
    private dataService: DataService,
    private navigateService: NavigateService
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

  hasCategory(object: RbObject): boolean {
    return this.categoryattribute != null && object.get(this.categoryattribute) != null;
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
      str = str.split('\r\n').join('<br>').split('\n').join('<br>').split('\t').join('&nbsp;&nbsp;');
    }
    return str;
  }

  getCategoryForItem(object: RbObject) : string {
    let str:string = null;
    if(this.categoryattribute != null) {
      str = object.get(this.categoryattribute); 
    }
    return str;
  }

  keydown(event: any) {

  }

  post() {
    if(this.value != null && this.value.length > 0) {
      let data = Object.assign({}, this.dataset.resolvedFilter);
      data[this.entryattribute] = this.value;
      this.dataService.create(this.dataset.objectname, null, data).subscribe((newObject) => {
        this.dataset.addObjectAndSelect(newObject);
        this.value = ""
      });
    }
  }

  clickCard(object: RbObject) {
    if(this.canClick) {
      let navEvent: NavigateEvent = {
        objectname: object.get(this.linkobjectattribute),
        datatargets: [{
          filter: {uid: "'" + object.get(this.linkuidattribute) + "'"}
        }]
      }
      this.navigateService.navigateTo(navEvent);
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
  }

  onScroll(event) {
    if(event.currentTarget.scrollTop > Math.floor(event.currentTarget.scrollHeight - event.currentTarget.clientHeight - 10) && this.reachedBottom == false) {
      this.dataset.fetchNextPage();
      this.reachedBottom = true;
      setTimeout(() => {this.reachedBottom = false}, 1000);
    }
  }
}
