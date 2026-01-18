import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { NavigateEvent, RbObject, RELATED_LOADING } from 'app/datamodel';
import { ColorConfig, Evaluator, Formatter, RecalcPlanner, VAEConfig } from 'app/helpers';
import { ApiService } from 'app/services/api.service';
import { ModalService } from 'app/services/modal.service';
import { NavigateService } from 'app/services/navigate.service';
import { UserprefService } from 'app/services/userpref.service';

const IsoDateRegExp: RegExp = /\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d(\.\d+|)([+-][0-2]\d:[0-5]\d|Z)/;

class ListFieldConfig {
  text: VAEConfig;
  color: ColorConfig;
  format: string

  constructor(attribute: string, expression: string, colorexpression: string, format: string) {
    this.text = new VAEConfig({attribute: attribute, expression: expression});
    this.color = new ColorConfig({expression: colorexpression});
    this.format = format;
  }

  get hasText(): boolean {
    return this.text.attribute != null || this.text.function != null;
  }

  getValue(object: RbObject): any {
    let raw = this.text.getValue(object);
    if(raw == RELATED_LOADING) {
      return {value: null, type: 'loading'};
    } else if(raw === true || raw === false) {
      return {value: raw, type: 'bool'};
    } else if(raw !== null && raw !== "" && !isNaN(raw)) { 
      return {value: raw, type: 'badge'};
    } else {
      const text = this.format != null ? Formatter.format(raw, this.format) : this.autoFormatText(raw);
      const color = this.color.getColor(object);
      return {value: text, type: 'text', color: color};
    }
  }

  private autoFormatText(txt: any) : string {
    if(txt === null || txt === undefined) {
      return "";
    } else if(IsoDateRegExp.test(txt)) {
      return Formatter.formatDateTime(new Date(txt));
    } else {
      return txt.toString();
    }
  }
}

@Component({
  selector: 'rb-list4',
  templateUrl: './rb-list4.component.html',
  styleUrls: ['./rb-list4.component.css']
})
export class RbList4Component extends RbDataObserverComponent {
  @Input('mainattribute') mainattribute: string;
  @Input('mainexpression') mainexpression: string;
  @Input('mainformat') mainformat: string;
  @Input('maincolor') maincolor: string;
  @Input('subattribute') subattribute: string;
  @Input('subexpression') subexpression: string;
  @Input('subformat') subformat: string;
  @Input('subcolor') subcolor: string;
  @Input('meta1attribute') meta1attribute: string;
  @Input('meta1expression') meta1expression: string;
  @Input('meta1format') meta1format: string;
  @Input('meta1color') meta1color: string;
  @Input('meta2attribute') meta2attribute: string;
  @Input('meta2expression') meta2expression: string;
  @Input('meta2format') meta2format: string;
  @Input('meta2color') meta2color: string;
  @Input('imageattribute') imageattribute: string;
  @Input('color') color: string;
  @Input('colormap') colormap: any;
  @Input('colorattribute') colorattribute: string;
  @Input('modal') modal: string;
  @Input('navigate') link: string;
  @Input('allowdrag') allowdrag: boolean = false;
  @Input('showrefresh') showrefresh: boolean = true;
  @Input('emptytext') emptytext: string = "No records";

  enhancedList: any[] = []
  isoDateRegExp: RegExp = /\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d(\.\d+|)([+-][0-2]\d:[0-5]\d|Z)/;
  reachedBottom: boolean = false;
  enhanceDragDataCallback: Function;

  main: ListFieldConfig;
  sub: ListFieldConfig;
  meta1: ListFieldConfig;
  meta2: ListFieldConfig;
  backColor: ColorConfig;

  recalcPlanner: RecalcPlanner;

  constructor(
    public userprefService: UserprefService,
    public apiService: ApiService,
    public modalService: ModalService,
    public navigateService: NavigateService
  ) {
    super();
  }

  dataObserverInit() {
    this.recalcPlanner = new RecalcPlanner(this.calcList.bind(this));
    this.enhanceDragDataCallback = this.enhanceDragData.bind(this);
    this.main = new ListFieldConfig(this.getUserParam("mainattribute"), this.getUserParam("mainexpression"), this.getUserParam("maincolor"), this.getUserParam("mainformat"));
    this.sub = new ListFieldConfig(this.getUserParam("subattribute"), this.getUserParam("subexpression"), this.getUserParam("subcolor"), this.getUserParam("subformat"));
    this.meta1 = new ListFieldConfig(this.getUserParam("meta1attribute"), this.getUserParam("meta1expression"), this.getUserParam("meta1color"), this.getUserParam("meta1format"));
    this.meta2 = new ListFieldConfig(this.getUserParam("meta2attribute"), this.getUserParam("meta2expression"), this.getUserParam("meta2color"), this.getUserParam("meta2format"));
    this.backColor = new ColorConfig({expression: this.getUserParam("color"), attribute: this.getUserParam("colorattribute"), map: this.getUserParam("colormap")});
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
    if(this.active) {
      if(event.event == 'load' || event.event == 'removed' || event.event == 'clear' || event.event == 'update') {
        this.redraw();
      }
    }
  }

  onActivationEvent(state: boolean) {
    if(this.active) {
      this.redraw();
    }
  }

  get userPref() : any {
    return this.id != null ? this.userprefService.getCurrentViewUISwitch("list4", this.id) : null;
  }

  getUserPref(attr: string) {
    return this.userPref != null ? this.userPref[attr] : null;
  }

  public hasMainLine() : boolean {
    return this.main.hasText;
  }

  public hasSubLine() : boolean {
    return this.sub.hasText;
  }

  public hasMetaLine() : boolean {
    return this.meta1.hasText || this.meta2.hasText;
  }

  public hasImage() : boolean {
    return this.imageattribute != null;
  }

  isSelected(object: RbObject) : boolean {
    return this.dataset.isObjectSelected(object);
  }

  public redraw() {
    this.recalcPlanner.request();
  }

  public calcList() {
    this.enhancedList = [];
    for(let obj of this.list) {
      let data: any = {
        object: obj,
        main: this.main.getValue(obj),
        sub: this.sub.getValue(obj),
        meta1: this.meta1.getValue(obj),
        meta2: this.meta2.getValue(obj),
        color: this.backColor.getColor(obj) || 'transparent'
      }

      if(this.hasImage()) {
        let fileVal = obj.get(this.imageattribute);
        if(fileVal != null) {
          data.image = 'url(\'' + fileVal.thumbnail + '\')';
        }
      }
      
      if(data.main.type != 'loading' && (data.main.value == null || data.main.value == "")) {
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

  private getUserParam(param: string) : any {
      let val = this[param];
      if(this.userPref != null && this.userPref[param] != null) {
        val = this.userPref[param];
      }
      return val;
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
    if(event.ctrlKey == true || event.metaKey == true) {
      this.dataset.addOneToSelection(item);
    } else if(event.shiftKey == true) {
      this.dataset.addRangeToSelection(item);
    } else {
      this.dataset.select(item);
      if(this.modal != null) {
        this.modalService.open(this.modal);
      } else if(this.link != null) {
        let navEvent: NavigateEvent = {
          objectname: this.rbObject.objectname,
          datatargets:[{
            filter: {uid: "'" + this.rbObject.uid + "'"}
          }]
        };
        this.navigateService.navigateTo(navEvent);
      }  
    }
  }

  public enhanceDragData(object: RbObject) : any {
    if(object != null) {
      return [object].concat(this.dataset.selectedObjects.filter(o => o.uid != object.uid));
    } else {
      return null
    }
  }

  refresh() {
    this.dataset.refreshData();
  }

  onScroll(event) {
    if(event.currentTarget.scrollTop > (event.currentTarget.scrollHeight - (1.5*event.currentTarget.clientHeight)) && this.reachedBottom == false) {
      this.reachedBottom = true;
      this.dataset.fetchNextPage();
      setTimeout(() => {this.reachedBottom = false}, 1000);
    }
  }
  
}
