import { ValueTransformer } from '@angular/compiler/src/util';
import { HostListener } from '@angular/core';
import { Component, Input, OnInit } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-list4',
  templateUrl: './rb-list4.component.html',
  styleUrls: ['./rb-list4.component.css']
})
export class RbList4Component extends RbDataObserverComponent {
  @Input('mainattribute') mainattribute: string;
  @Input('subattribute') subattribute: string;
  @Input('meta1attribute') meta1attribute: string;
  @Input('meta2attribute') meta2attribute: string;

  enhancedList: any[];
  isoDateRegExp: RegExp = /\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d(\.\d+|)([+-][0-2]\d:[0-5]\d|Z)/;
  reachedBottom: boolean = false;

  constructor(
    public userpref: UserprefService
  ) {
    super();
  }

  dataObserverInit() {
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: string) {
    this.redraw();
  }

  onActivationEvent(state: boolean) {
    this.redraw();
  }

  public hasMainLine() : boolean {
    return this.mainattribute != null;
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
      if(this.mainattribute != null) {
        data["main"] = this.formatText(obj.get(this.mainattribute));
      }
      if(this.subattribute != null) {
        data["sub"] = this.formatText(obj.get(this.subattribute));
      }
      if(this.meta1attribute != null) {
        data["meta1"] = this.formatText(obj.get(this.meta1attribute));
      }
      if(this.meta2attribute != null) {
        data["meta2"] = this.formatText(obj.get(this.meta2attribute));
        data["meta2isabadge"] = data["meta2"] != "" && !isNaN(Number(data["meta2"]))
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
