import { Component, EventEmitter, Input, OnInit, Output, SimpleChange } from '@angular/core';
import { RbObject } from 'app/datamodel';

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
export class RbTableComponent implements OnInit {
  @Input('list') list: any;
  @Input('columns') _cols: any;
  @Input('selectedObject') selectedObject: RbObject;
  @Output() selectedObjectChange: EventEmitter<any> = new EventEmitter();
  @Output() deleteSelected: EventEmitter<any> = new EventEmitter();
  @Output() filterSort: EventEmitter<any> = new EventEmitter();

  columns: TableColumnConfig[];

  constructor() { }

  ngOnInit(): void {
  }


  ngOnChanges(changes : SimpleChange) {
    if('_cols' in changes && this._cols != null) {
      this.columns = [];
      for(let item of this._cols) {
        this.columns.push(new TableColumnConfig(item));
      }
    }
  }

  clickColumn(column: TableColumnConfig) {
    this.filterSort.emit({
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
    this.selectedObjectChange.emit(object);
    this.deleteSelected.emit();
  }
}
