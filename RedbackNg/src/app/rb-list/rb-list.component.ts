import { HostListener } from '@angular/core';
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-list',
  templateUrl: './rb-list.component.html',
  styleUrls: ['./rb-list.component.css']
})
export class RbListComponent implements OnInit {
  @Input('dataset') dataset: RbDatasetComponent;
  //@Input('list') list: RbObject[];
  //@Input('selectedObject') selectedObject: RbObject;
  @Input('headerattribute') headerattribute: string;
  @Input('subheadattribute') subheadattribute: string;
  @Input('supptextattribute') supptextattribute: string;
  @Input('sidetextattribute') sidetextattribute: string;
  @Input('iconattribute') iconattribute: string;
  @Input('colorattribute') colorattribute: string;
  @Input('iconmap') iconmap: any;
  @Input('colormap') colormap: any;
  @Input('isLoading') isLoading: any;

 // @Output() selectedObjectChange: EventEmitter<any> = new EventEmitter();

  isoDateRegExp: RegExp = /\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d(\.\d+|)([+-][0-2]\d:[0-5]\d|Z)/;

  constructor(
    public userpref: UserprefService
  ) { }

  get list() : RbObject[] {
    return this.dataset != null ? this.dataset.list : null;
  }

  get selectedObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : null;
  }

  ngOnInit() {
  }

  itemClicked(item: RbObject) {
    this.dataset.select(item);
  }

  getIconFor(item: RbObject) : string {
    if(this.iconattribute != null) {
      return item.get(this.iconattribute)
    } else {
      return "";
    }
  }

  getHeaderFor(item: RbObject) : string {
    if(this.headerattribute != null) {
      let val = item.get(this.headerattribute);
      if((val == null || val.trim() == "")) {
        let val2 = item.get(this.subheadattribute);
        let val3 = item.get(this.supptextattribute);
        if((val2 == null || val2.trim() == "") && (val3 == null || val3.trim() == "")) {
          val = "No Label";
        }
      }
      return this.formatText(val);
    } else {
      return "";
    }
  }

  getSubheadFor(item: RbObject) : string {
    if(this.subheadattribute != null) {
      return this.formatText(item.get(this.subheadattribute));
    } else {
      return "";
    }
  }

  getSuppTextFor(item: RbObject) : string {
    if(this.supptextattribute != null) {
      return this.formatText(item.get(this.supptextattribute));
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

}
