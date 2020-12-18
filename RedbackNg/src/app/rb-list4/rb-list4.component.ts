import { HostListener } from '@angular/core';
import { Component, Input, OnInit } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { UserprefService } from 'app/userpref.service';

@Component({
  selector: 'rb-list4',
  templateUrl: './rb-list4.component.html',
  styleUrls: ['./rb-list4.component.css']
})
export class RbList4Component implements OnInit {
  @Input('dataset') dataset: RbDatasetComponent;
  @Input('mainattribute') mainattribute: string;
  @Input('subattribute') subattribute: string;
  @Input('meta1attribute') meta1attribute: string;
  @Input('meta2attribute') meta2attribute: string;

  isoDateRegExp: RegExp = /\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d(\.\d+|)([+-][0-2]\d:[0-5]\d|Z)/;
  reachedBottom: boolean = false;

  constructor(
    public userpref: UserprefService
  ) { }

  ngOnInit(): void {
  }

  public get list() : RbObject[] {
    return this.dataset != null ? this.dataset.list : [];
  }

  public get selectedObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : null;
  }

  public get isLoading() : boolean {
    return this.dataset != null ? this.dataset.isLoading : false;
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

  public isMeta2aBadge(item: RbObject): boolean {
    return !isNaN(parseInt(this.getMeta2For(item)));
  }

  getMainFor(item: RbObject) : string {
    if(this.mainattribute != null) {
      let val = item.get(this.mainattribute);
      if((val == null || val.trim() == "")) {
        val = "No Label";
      }
      return this.formatText(val);
    } else {
      return "";
    }
  }

  getSubFor(item: RbObject) : string {
    if(this.subattribute != null) {
      return this.formatText(item.get(this.subattribute));
    } else {
      return "";
    }
  }

  getMeta1For(item: RbObject) : string {
    if(this.meta1attribute != null) {
      return this.formatText(item.get(this.meta1attribute));
    } else {
      return "";
    }
  }

  getMeta2For(item: RbObject) : string {
    if(this.meta2attribute != null) {
      return this.formatText(item.get(this.meta2attribute));
    } else {
      return "";
    }
  }

  private formatText(txt: string) : string {
    if(this.isoDateRegExp.test(txt)) {
      return (new Date(txt)).toLocaleString();
    } else {
      return txt;
    }
  }

  showCount() : boolean {
    return this.dataset.list.length > 10;
  }

  getCountText() : string {
    return this.dataset.list.length + (this.dataset.list.length % this.dataset.pageSize == 0 ? '+' : '');
  }


  itemClicked(item: RbObject) {
    this.dataset.select(item);
  }

  onScroll(event) {
    if(event.currentTarget.scrollTop > Math.floor(event.currentTarget.scrollHeight - event.currentTarget.clientHeight - 10) && this.reachedBottom == false) {
      this.dataset.fetchNextPage();
      this.reachedBottom = true;
      setTimeout(() => {this.reachedBottom = false}, 1000);
    }
  }
  
}
