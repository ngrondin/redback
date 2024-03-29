import { Component, EventEmitter, Input, OnInit, Output, SimpleChange } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';

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
  width: number;
  showExpr: string;

  constructor(json: any) {
    this.label = json.label;
    this.attribute = json.attribute;
    this.displayAttribute = json.displayattribute;
    this.parentAttribute = json.parentattribute;
    this.childAttribute = json.childattribute;
    this.type = json.type;
    this.format = json.format;
    this.icon = json.icon;
    this.size = json.size;
    this.width = (json.size != null ? (json.size * 15) + 15 : 250);
    this.showExpr = (json.show != null ? json.show : "true");
  }
}

@Component({
  selector: 'rb-table',
  templateUrl: './rb-table.component.html',
  styleUrls: ['./rb-table.component.css']
})
export class RbTableComponent extends RbDataObserverComponent {
  @Input('columns') _cols: any;

  columns: TableColumnConfig[];

  constructor() {
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

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
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
    return object.canDelete()
  }
}
