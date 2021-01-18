import { Component, EventEmitter, Input, OnInit, Output, SimpleChange } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';

class TableColumnConfig {
  label: string;
  attribute: string;
  displayAttribute: string;
  type: string;
  format: string;
  size: number;
  width: number;

  constructor(json: any) {
    this.label = json.label;
    this.attribute = json.attribute;
    this.displayAttribute = json.displayattribute;
    this.type = json.type;
    this.format = json.format;
    this.size = json.size;
    this.width = (json.size != null ? (json.size * 15) + 15 : 250);
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
  
  get selectedObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : null;
  }

  get list(): RbObject[] {
    return this.dataset != null ? this.dataset.list : null;
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
    return true;
  }
}
