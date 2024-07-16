import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { Evaluator, Formatter } from 'app/helpers';
import { ModalService } from 'app/services/modal.service';
import { NavigateService } from 'app/services/navigate.service';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-list4',
  templateUrl: './rb-list4.component.html',
  styleUrls: ['./rb-list4.component.css']
})
export class RbList4Component extends RbDataObserverComponent {
  @Input('mainattribute') mainattribute: string;
  @Input('mainexpression') mainexpression: string;
  @Input('mainformat') mainformat: string;
  @Input('subattribute') subattribute: string;
  @Input('subexpression') subexpression: string;
  @Input('subformat') subformat: string;
  @Input('meta1attribute') meta1attribute: string;
  @Input('meta1expression') meta1expression: string;
  @Input('meta1format') meta1format: string;
  @Input('meta2attribute') meta2attribute: string;
  @Input('meta2expression') meta2expression: string;
  @Input('meta2format') meta2format: string;
  @Input('colormap') colormap: any;
  @Input('colorattribute') colorattribute: string;
  @Input('modal') modal: string;
  @Input('navigate') link: string;
  @Input('allowdrag') allowdrag: boolean = false;
  @Input('showrefresh') showrefresh: boolean = true;

  //@Output() navigate: EventEmitter<any> = new EventEmitter();

  enhancedList: any[];
  isoDateRegExp: RegExp = /\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d(\.\d+|)([+-][0-2]\d:[0-5]\d|Z)/;
  reachedBottom: boolean = false;

  constructor(
    public userprefService: UserprefService,
    public modalService: ModalService,
    public navigateService: NavigateService
  ) {
    super();
  }

  dataObserverInit() {
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: string) {
    if(event == 'load' || event == 'removed' || event == 'clear' || event == 'update') {
      this.redraw();
    }
  }

  onActivationEvent(state: boolean) {
    this.redraw();
  }

  get userPref() : any {
    return this.id != null ? this.userprefService.getCurrentViewUISwitch("list4", this.id) : null;
  }

  getUserPref(attr: string) {
    return this.userPref != null ? this.userPref[attr] : null;
  }

  public hasMainLine() : boolean {
    return this.mainattribute != null || this.mainexpression != null;
  }

  public hasSubLine() : boolean {
    return this.subattribute != null || this.subexpression != null;
  }

  public hasMetaLine() : boolean {
    return this.meta1attribute != null || this.meta2attribute != null;
  }

  isSelected(object: RbObject) : boolean {
    return this.dataset.isObjectSelected(object);
  }

  public redraw() {
    this.enhancedList = [];
    for(let obj of this.list) {
      let data: any = {
        object: obj,
        main: this.getFieldValue(obj, "main"),
        sub: this.getFieldValue(obj, "sub"),
        meta1: this.getFieldValue(obj, "meta1"),
        meta2: this.getFieldValue(obj, "meta2")
      }

      let thisColorAttribute = this.colorattribute;
      if(this.userPref != null && this.userPref.colorattribute != null) {
        thisColorAttribute = this.userPref.colorattribute;
      } 
      let thisColorMap = this.colormap;
      if(this.userPref != null && this.userPref.colormap != null) {
        thisColorMap = this.userPref.colormap;
      }
      if(thisColorAttribute != null) {
        data.color = thisColorMap != null ? thisColorMap[obj.get(thisColorAttribute)] : obj.get(thisColorAttribute);
      } else {
        data.color = "transparent";
      }
      
      if(data.main.value == null || data.main.value == "") {
        if(data.sub.value != null && data.sub.value != "") {
          data.main.value = data.sub.value;
          data.sub.value = "";
        } else {
          data.main.value = "No Label"
        }
      }       
      this.enhancedList.push(data);                 
    }
  }

  private getFieldValue(obj: RbObject, field: string) : any {
    const fieldAttr = field + "attribute";
    const fieldExpr = field + "expression";
    const fieldFormat = field + "format";
    const prefAttr = this.getUserPref(fieldAttr);
    const prefExpr = this.getUserPref(fieldExpr);
    const raw = 
      prefAttr != null ? obj.get(prefAttr) : 
      prefExpr != null ? Evaluator.eval(prefExpr, obj, null) :
      this[fieldAttr] != null ? obj.get(this[fieldAttr]) :
      this[fieldExpr] != null ? Evaluator.eval(this[fieldExpr], obj, null) :
      null;

    let isTrueFalse = (raw === true || raw === false);
    if(isTrueFalse) {
      return {value: raw, type: 'bool'};
    } else if(raw !== null && !isNaN(raw)) { 
      return {value: raw, type: 'badge'};
    } else {
      const explicitFormat = this.getUserPref(fieldFormat) ?? this[fieldFormat];
      const text = explicitFormat != null ? Formatter.format(raw, explicitFormat) : this.autoFormatText(raw);
      return {value: text, type: 'text'};
    }
  }

  private autoFormatText(txt: any) : string {
    if(txt === null || txt === undefined) {
      return "";
    } else if(this.isoDateRegExp.test(txt)) {
      return Formatter.formatDateTime(new Date(txt));
    } else {
      return txt.toString();
    }
  }

  showCount() : boolean {
    return this.showrefresh && this.dataset.totalCount > 10;
  }

  getCountText() : string {
    return this.dataset.totalCount.toString();
  }

  showRefresh() : boolean {
    return this.showrefresh;
  }

  itemClicked(item: RbObject, event: any) {
    if(event.ctrlKey == true) {
      this.dataset.addOneToSelection(item);
    } else if(event.shiftKey == true) {
      this.dataset.addRangeToSelection(item);
    } else {
      this.dataset.select(item);
      if(this.modal != null) {
        this.modalService.open(this.modal);
      } else if(this.link != null) {
        let target = {
          "object": this.rbObject.objectname,
          "filter": {uid: "'" + this.rbObject.uid + "'"}
        };
        this.navigateService.navigateTo(target);
      }  
    }
  }

  refresh() {
    this.dataset.refreshData();
  }

  onScroll(event) {
    if(event.currentTarget.scrollTop > Math.floor(event.currentTarget.scrollHeight - event.currentTarget.clientHeight - 10) && this.reachedBottom == false) {
      this.dataset.fetchNextPage();
      this.reachedBottom = true;
      setTimeout(() => {this.reachedBottom = false}, 1000);
    }
  }
  
}
