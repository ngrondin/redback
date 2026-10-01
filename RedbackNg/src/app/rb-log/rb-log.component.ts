import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { NavigateEvent, RbObject } from 'app/datamodel';
import { Formatter, RecalcPlanner, VAEConfig } from 'app/helpers';
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
  @Input('groupattribute') groupattribute: string;
  @Input('userexpression') userexpression: string;
  @Input('dateexpression') dateexpression: string;
  @Input('entryexpression') entryexpression: string;
  @Input('categoryexpression') categoryexpression: string;
  @Input('groupexpression') groupexpression: string;
  @Input('groupborder') groupborder: boolean = false;
  //@Input('categories') categories: any;
  @Input('editable') editable: any;
  @Input('linkobjectattribute') linkobjectattribute: string;
  @Input('linkuidattribute') linkuidattribute: string;
  
  @Output() posted: EventEmitter<any> = new EventEmitter();

  public value: string; 
  public isEditable: boolean = false;
  public reachedBottom: boolean = false;
  public data: any = {};
  public editing: RbObject = null;
  public editValue: string;

  recalcPlanner!: RecalcPlanner;
  user!: VAEConfig;
  date!: VAEConfig;
  entry!: VAEConfig;
  category!: VAEConfig;
  group!: VAEConfig;

  constructor(
    private dataService: DataService,
    private navigateService: NavigateService
  ) {
    super();
  }

  dataObserverInit() {
    this.recalcPlanner = new RecalcPlanner(this.calcList.bind(this))
    this.user = new VAEConfig({attribute: this.userattribute, expression: this.userexpression});
    this.date = new VAEConfig({attribute: this.dateattribute, expression: this.dateexpression});
    this.entry = new VAEConfig({attribute: this.entryattribute, expression: this.entryexpression});
    this.category = new VAEConfig({attribute: this.categoryattribute, expression: this.categoryexpression});
    this.group = new VAEConfig({attribute: this.groupattribute, expression: this.groupexpression});
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
    if(this.active == true) {
      this.evalEditable();
      if(event.event == 'load' || event.event == 'removed' || event.event == 'clear' || event.event == 'update') {
        this.recalcPlanner.request();
      }      
    } else {
      this.isEditable = false;
    }
  }

  onActivationEvent(event: any) {
    if(this.active == true) {
      this.evalEditable();
      this.recalcPlanner.request();
    } else {
      this.isEditable = false;
    }
  }

  public get canClick() : boolean {
    return this.linkobjectattribute != null && this.linkuidattribute != null;
  }

  public get groups(): string[] {
    return this.data != null ? Object.keys(this.data) : [null];
  }

  public calcList() {
    let data = {};
    for(var object of this.list) {
      let grp: string|null = this.group.getValue(object);
      let user: string|null = this.user.getValue(object);
      if(user == null || (user != null && user.length == 0)) user = "Unknown user";
      let cat: string|null = this.category.getValue(object);
      let dtstr : string = this.date.getValue(object);
      if(dtstr == null || (dtstr != null && dtstr.length == 0)) {
        dtstr = "Unknown date";
      } else {
        dtstr = Formatter.formatDateTime(new Date(dtstr));
      }
      let entry : string = this.entry.getValue(object);
      if(entry == null) {
        entry = "";
      } else {
        entry = entry.split('\r\n').join('<br>').split('\n').join('<br>').split('\t').join('&nbsp;&nbsp;');
      }
      if(data[grp] == null) data[grp] = [];
      data[grp].push({
        entry: entry,
        user: user,
        date: dtstr,
        category: cat,
        object: object
      })
    }
    this.data = data;
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

  startEdit(object: RbObject) {
    this.editing = object;
    this.editValue = object.get(this.entryattribute);
  }

  saveEdit() {
    if(this.editValue != null && this.editValue.length > 0) {
      this.editing.setValue(this.entryattribute, this.editValue);
    }
    this.editing = null;
  }

  deleteItem(object: RbObject) {
    this.dataset.delete(object);
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
