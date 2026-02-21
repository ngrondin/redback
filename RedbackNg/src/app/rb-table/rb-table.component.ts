import { Component, EventEmitter, Input, OnInit, Output, SimpleChange } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject, RbObjectTransaction } from 'app/datamodel';
import { LinkConfig, ValueComparator } from 'app/helpers';
import { DataService } from 'app/services/data.service';

class TableColumnConfig {
  label: string;
  attribute: string;
  displayAttribute: string;
  parentAttribute: string;
  childAttribute: string;
  type: string;
  format: string;
  icon: string;
  size: number;
  width: string;
  editable: boolean;
  linkview: string;
  showExpr: string;
  alt: {[key: string]: TableColumnConfig};

  constructor(json: any) {
    this.label = json.label;
    this.attribute = json.attribute;
    this.displayAttribute = json.displayattribute;
    this.parentAttribute = json.parentattribute;
    this.childAttribute = json.childattribute;
    this.type = json.type;
    this.format = json.format;
    this.icon = json.icon;
    this.size = json.size ?? 17;
    this.width = 'min(' + (0.88 * this.size) + 'vw, ' + (17 * this.size) + 'px)';
    //this.width = (json.size != null ? (json.size * 15) + 15 : 250);
    this.editable = (json.editable != null ? json.editable : true);
    this.linkview = json.linkview;
    this.showExpr = (json.show != null ? json.show : "true");
    if(json.alt != null) {
      this.alt = {};
      for(const key in json.alt) {
        this.alt[key] = new TableColumnConfig(json.alt[key]);
      }
    }
  }
}

@Component({
  selector: 'rb-table',
  templateUrl: './rb-table.component.html',
  styleUrls: ['./rb-table.component.css']
})
export class RbTableComponent extends RbDataObserverComponent {
  @Input('columns') _cols: any;
  @Input('headersonemtpy') headersonempty: boolean = true;
  @Input('candeleterows') candeleterows: boolean = true;
  @Input('emptymessage') emptymessage: string = null;
  @Input('orderattribute') orderattribute: string;

  columns: TableColumnConfig[];
  reachedBottom: boolean = false;
  orderedList: RbObject[] = [];

  constructor(
    public dataService: DataService
  ) {
    super();
  }

  dataObserverInit() {
    this.columns = [];
    for(let item of this._cols) {
      this.columns.push(new TableColumnConfig(item));
    }
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
    if(this.active) {
      this.calc();
    }
  }

  onActivationEvent(state: boolean) {
    if(state == true) {
      this.calc();
    }
  }

  get canOrder() : boolean {
    return this.orderattribute != null;
  }

  calc() {
    if(this.orderattribute != null) {
      this.orderedList = [...this.list].sort((a, b) => ValueComparator.sort(a.get(this.orderattribute), b.get(this.orderattribute)));
    } else {
      this.orderedList = this.list;
    }
  }

  getColumnsForObject(object: RbObject): TableColumnConfig[] {
    let cols: TableColumnConfig[] = this.columns.map(col => {
      if(col.type == 'alt') {
        const val = object.get(col.attribute);
        return col.alt[val];
      } else {
        return col;
      }
    });
    return cols;
  }

  clickColumn(column: TableColumnConfig) {
    this.dataset.filterSort({
      filter: {},
      sort: {
        "0": {
          "attribute":column.attribute,
          "dir":1
        }
      }
    });
  }

  deleteObject(object: RbObject) {
    this.dataset.delete(object);
  }

  canDelete(object: RbObject) {
    return this.candeleterows && object.canDelete()
  }

  onScroll(event) {
    if(event.currentTarget.scrollTop > (event.currentTarget.scrollHeight - event.currentTarget.clientHeight) - 300 && this.reachedBottom == false) {
      this.reachedBottom = true;
      this.dataset.fetchNextPage();
      setTimeout(() => {this.reachedBottom = false}, 1000);
    }
  }

  public droppedOn(event: any, obj: RbObject) {
    let tx = new RbObjectTransaction();
    if(this.orderedList.map(o => o.get(this.orderattribute)).filter(v => v == null).length > 0) {
      for(let i = 0; i < this.orderedList.length; i++) {
        this.orderedList[i].setValue(this.orderattribute, i, tx);
      }
    }

    let droppedObject = event.data;
    let droppedOn = obj;
    let allOrderValues = this.orderedList.map(o => o.get(this.orderattribute));
    let droppedPrevOrderValue = droppedObject.get(this.orderattribute);

    let newList = this.orderedList.filter(o => o != droppedObject);
    let index = newList.indexOf(droppedOn);
    newList.splice(index, 0, droppedObject);
    for(let i = 0; i < newList.length; i++) {
      newList[i].setValue(this.orderattribute, allOrderValues[i], tx);
    }
    this.dataService.pushTransactionToServer(tx);
  }
}
