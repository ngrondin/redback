import { Component, Input } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { Evaluator } from 'app/helpers';
import { ModalService } from 'app/services/modal.service';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-list4',
  templateUrl: './rb-list4.component.html',
  styleUrls: ['./rb-list4.component.css']
})
export class RbList4Component extends RbDataObserverComponent {
  @Input('mainattribute') mainattribute: string;
  @Input('mainexpression') mainexpression: string;
  @Input('subattribute') subattribute: string;
  @Input('meta1attribute') meta1attribute: string;
  @Input('meta2attribute') meta2attribute: string;
  @Input('modal') modal: string;

  enhancedList: any[];
  isoDateRegExp: RegExp = /\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d(\.\d+|)([+-][0-2]\d:[0-5]\d|Z)/;
  reachedBottom: boolean = false;

  constructor(
    public userprefService: UserprefService,
    public modalService: ModalService
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
    return this.id != null ? this.userprefService.getUISwitch("list4", this.id) : null;
  }

  public hasMainLine() : boolean {
    return this.mainattribute != null || this.mainexpression != null;
  }

  public hasSubLine() : boolean {
    return this.subattribute != null;
  }

  public hasMetaLine() : boolean {
    return this.meta1attribute != null || this.meta2attribute != null;
  }

  public redraw() {
    this.enhancedList = [];
    for(let obj of this.list) {
      let data = {};
      if(this.userPref != null && this.userPref.mainattribute != null) {
        data["main"] = this.formatText(obj.get(this.userPref.mainattribute));
      } else if(this.mainattribute != null) {
        data["main"] = this.formatText(obj.get(this.mainattribute));
      } else if(this.mainexpression != null) {
        data["main"] = Evaluator.eval(this.mainexpression, obj, null);
      }

      if(this.userPref != null && this.userPref.subattribute != null) {
        data["sub"] = this.formatText(obj.get(this.userPref.subattribute));
      } else if(this.subattribute !== null) {
        data["sub"] = this.formatText(obj.get(this.subattribute));
      }

      if(this.meta1attribute !== null) {
        data["meta1"] = this.formatText(obj.get(this.meta1attribute));
      }

      if(this.meta2attribute !== null) {
        data["meta2"] = this.formatText(obj.get(this.meta2attribute));
        data["meta2isabadge"] = data["meta2"] !== "" && !isNaN(Number(data["meta2"]))
      }
      
      if(data["main"] == null || data["main"] == "") {
        if(data["sub"] != null && data["sub"] != "") {
          data["main"] = data["sub"];
          data["sub"] = "";
        } else {
          data["main"] = "No Label"
        }
      } 
      data["object"] = obj;
      this.enhancedList.push(data);                 
    }
  }

  private formatText(txt: string) : string {
    if(txt == null) {
      return "";
    } else if(this.isoDateRegExp.test(txt)) {
      return (new Date(txt)).toLocaleString();
    } else {
      return txt;
    }
  }

  showCount() : boolean {
    return this.dataset.totalCount > 10;
  }

  getCountText() : string {
    return this.dataset.totalCount.toString();
  }

  showRefresh() : boolean {
    return true;
  }

  itemClicked(item: RbObject) {
    this.dataset.select(item);
    if(this.modal != null) {
      this.modalService.open(this.modal);
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
