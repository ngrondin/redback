import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'rb-list',
  templateUrl: './rb-list.component.html',
  styleUrls: ['./rb-list.component.css']
})
export class RbListComponent implements OnInit {
  @Input('list') list: RbObject[];
  @Input('selectedObject') selectedObject: RbObject;
  @Input('headerattribute') headerattribute: string;
  @Input('subheadattribute') subheadattribute: string;
  @Input('supptextattribute') supptextattribute: string;
  @Input('sidetextattribute') sidetextattribute: string;
  @Input('iconattribute') iconattribute: string;
  @Input('colorattribute') colorattribute: string;
  @Input('iconmap') iconmap: any;
  @Input('colormap') colormap: any;
  @Input('isLoading') isLoading: any;

  @Output() selectedObjectChange: EventEmitter<any> = new EventEmitter();

  constructor() { }

  ngOnInit() {
  }

  itemClicked(item: RbObject) {
    this.selectedObjectChange.emit(item);
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
      return val;
    } else {
      return "";
    }
  }

  getSubheadFor(item: RbObject) : string {
    if(this.subheadattribute != null) {
      return item.get(this.subheadattribute);
    } else {
      return "";
    }
  }

  getSuppTextFor(item: RbObject) : string {
    if(this.supptextattribute != null) {
      return item.get(this.supptextattribute);
    } else {
      return "";
    }
  }

}
