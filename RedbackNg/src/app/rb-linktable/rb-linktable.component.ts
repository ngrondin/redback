import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { Formatter, LinkConfig } from 'app/helpers';
import { ModalService } from 'app/services/modal.service';

class LinkTableColumnConfig {
  label: string;
  attribute: string;
  displayAttribute: string;
  type: string;
  format: string;
  size: number;
  width: number;
  showExpr: string;
  link: LinkConfig;
  modal: string;

  constructor(json: any) {
    this.label = json.label;
    this.attribute = json.attribute;
    this.displayAttribute = json.displayattribute;
    this.type = json.type;
    this.format = json.format;
    this.size = json.size;
    this.width = (json.size != null ? (json.size * 15) + 15 : 250);
    this.showExpr = (json.show != null ? json.show : "true");
    this.link = json.link != null ? new LinkConfig(json.link) : null;
    this.modal = json.modal;
  }

  get isClickable() : boolean {
    return this.link != null || this.modal != null;
  }
}

@Component({
  selector: 'rb-linktable',
  templateUrl: './rb-linktable.component.html',
  styleUrls: ['./rb-linktable.component.css']
})
export class RbLinktableComponent extends RbDataObserverComponent {
  @Input('columns') _cols: any;
  @Input('view') view: string;
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  columns: LinkTableColumnConfig[];
  reachedBottom: boolean = false;
  scrollLeft: number;


  constructor(
    private modalService: ModalService
  ) {
    super();
  }

  dataObserverInit() {
    this.columns = [];
    for(let item of this._cols) {
      this.columns.push(new LinkTableColumnConfig(item));
    }
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
  }

  getValue(object: RbObject, column: LinkTableColumnConfig): string {
    let val = object.get(column.attribute);
    if(column.format != null) {
      val = Formatter.format(val, column.format);
    }
    return val;
  }

  clickColumn(column: LinkTableColumnConfig) {
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

  clickLink(column: LinkTableColumnConfig, object: RbObject) {
    if(column.link != null) {
      let event = column.link.getNavigationEvent(object);
      this.navigate.emit(event);
    } else if(column.modal != null) {
      this.dataset.select(object);
      this.modalService.open(column.modal);
    }

  }

  onScroll(event) {
    this.scrollLeft = event.target.scrollLeft;
    if(event.currentTarget.scrollTop > Math.floor(event.currentTarget.scrollHeight - event.currentTarget.clientHeight - 10) && this.reachedBottom == false) {
      this.dataset.fetchNextPage();
      this.reachedBottom = true;
      setTimeout(() => {this.reachedBottom = false}, 1000);
    }
  }
}
