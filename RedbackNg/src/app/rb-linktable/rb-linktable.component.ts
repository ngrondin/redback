import { Component, Input } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { Evaluator, Formatter, LinkConfig } from 'app/helpers';
import { ModalService } from 'app/services/modal.service';
import { NavigateService } from 'app/services/navigate.service';

class LinkTableColumnConfig {
  label: string;
  attribute: string;
  expression: string;
  displayAttribute: string;
  format: string;
  size: number;
  width: number;
  showExpr: string;
  link: LinkConfig;
  modal: string;
  iconmap: any;
  alt: {[key: string]: LinkTableColumnConfig};

  constructor(json: any) {
    this.label = json.label;
    this.attribute = json.attribute;
    this.expression = json.expression;
    this.displayAttribute = json.displayattribute;
    this.format = json.format;
    this.size = json.size;
    this.width = (json.size != null ? (json.size * 0.88) : 10);
    this.showExpr = (json.show != null ? json.show : "true");
    this.link = json.link != null ? new LinkConfig(json.link) : null;
    this.modal = json.modal;
    this.iconmap = json.iconmap;
    if(json.alt != null) {
      this.alt = {};
      for(const key in json.alt) {
        this.alt[key] = new LinkTableColumnConfig(json.alt[key]);
      }
    }    
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
  @Input('grid') grid: boolean = false;

  columns: LinkTableColumnConfig[];
  reachedBottom: boolean = false;
  scrollLeft: number;


  constructor(
    private modalService: ModalService,
    private navigateService: NavigateService
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
    this.scrollLeft = 0;
  }

  getColumnConfig(object: RbObject, column: LinkTableColumnConfig): LinkTableColumnConfig {
    if(column.alt != null && column.attribute != null) {
      return column.alt[object.get(column.attribute)];
    } 
    return column;
  }

  getValue(column: LinkTableColumnConfig, object: RbObject): string {
    let cfg = this.getColumnConfig(object, column);
    let val = null;
    if(cfg != null) {
      if(cfg.expression != null) {
        val = Evaluator.eval(cfg.expression, object, null);
      } else if(cfg.attribute != null) {
        val = object.get(cfg.attribute);
      }
      if(cfg.format != null) {
        val = Formatter.format(val, cfg.format);
      }  
    }
    return val;
  }

  clickColumnHeader(column: LinkTableColumnConfig) {
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

  isClickable(column: LinkTableColumnConfig, object: RbObject) {
    let cfg = this.getColumnConfig(object, column);
    return cfg != null ? cfg.isClickable : false
  }

  clickLink(column: LinkTableColumnConfig, object: RbObject) {
    let cfg = this.getColumnConfig(object, column);
    if(cfg != null) {
      if(cfg.link != null) {
        let event = cfg.link.getNavigationEvent(object);
        this.navigateService.navigateTo(event);
      } else if(cfg.modal != null) {
        this.dataset.select(object);
        this.modalService.open(cfg.modal);
      }  
    }
  }

  isIcon(column: LinkTableColumnConfig, object: RbObject) {
    let cfg = this.getColumnConfig(object, column);
    return cfg.iconmap != null;
  }

  icon(column: LinkTableColumnConfig, object: RbObject) {
    let cfg = this.getColumnConfig(object, column);
    return cfg.iconmap != null ? cfg.iconmap[this.getValue(column, object)] : '';
  }

  onScroll(event) {
    this.scrollLeft = event.target.scrollLeft;
    if(event.currentTarget.scrollTop > Math.floor(event.currentTarget.scrollHeight - event.currentTarget.clientHeight - 30) && this.reachedBottom == false) {
      this.dataset.fetchNextPage();
      this.reachedBottom = true;
      setTimeout(() => {this.reachedBottom = false}, 1000);
    }
  }
}
