import { Component, Input } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { ColorConfig, Evaluator, Formatter, LinkConfig } from 'app/helpers';
import { ModalService } from 'app/services/modal.service';
import { NavigateService } from 'app/services/navigate.service';
import { UserprefService } from 'app/services/userpref.service';
import { LinkTableColumnConfig, LinkTableGroupConfig } from './rb-linktable-models';



@Component({
  selector: 'rb-linktable',
  templateUrl: './rb-linktable.component.html',
  styleUrls: ['./rb-linktable.component.css']
})
export class RbLinktableComponent extends RbDataObserverComponent {
  @Input('columns') _cols: any;
  @Input('group') _grp: any;
  @Input('view') view: string;
  @Input('grid') grid: boolean = false;

  columns: LinkTableColumnConfig[];
  group: LinkTableGroupConfig;
  reachedBottom: boolean = false;
  scrollLeft: number;
  groups: any = {};
  sums: any[];
  openGroups: string[] = [null];
  
  constructor(
    private modalService: ModalService,
    private navigateService: NavigateService,
    public userprefService: UserprefService,
  ) {
    super();
  }

  dataObserverInit() {
    this.columns = [];
    for(let item of this._cols) {
      let colPref = this.userPref != null && this.userPref.cols != null ? this.userPref.cols[item.id] : null;
      if(!(colPref == false || (colPref != null && colPref.hide == true))) {
        this.columns.push(new LinkTableColumnConfig(item, colPref));
      }
    }
    if(this._grp != null) {
      this.group = new LinkTableGroupConfig(this._grp);
    }
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
    this.calc();
  }

  onActivationEvent(state: boolean) {
    this.scrollLeft = 0;
    this.calc();
  }

  get userPref() : any {
    return this.id != null ? this.userprefService.getCurrentViewUISwitch("linktable", this.id) : null;
  }

  get hasGroup(): boolean {
    return this.group != null;
  }

  get groupKeys(): string[] {
    return Object.keys(this.groups).sort();
  }

  calc() {
    this.groups = {};
    let totalsums = this.columns.map(c => 0);
    for(let object of this.list) {
      let grpKey = null;
      if(this.group != null) {
        if(this.group.expression != null) {
          grpKey = Evaluator.eval(this.group.expression, object, null, this.dataset);
        } else if(this.group.attribute != null) {
          grpKey = object.get(this.group.attribute);
        }
      }
      if(this.groups[grpKey] == null) this.groups[grpKey] = {sums:this.columns.map(c => 0), lines: []};
      let grp = this.groups[grpKey];
      let cols = [];
      for(let c = 0; c < this.columns.length; c++) {
        let cfg = this.getColumnConfig(object, this.columns[c]);
        if(cfg != null) {
          let val = null;
          if(cfg.expression != null) {
            val = Evaluator.eval(cfg.expression, object, null, this.dataset);
          } else if(cfg.attribute != null) {
            val = object.get(cfg.attribute);
          }
          let formatVal = cfg.format != null ? Formatter.format(val, cfg.format) : val;
          let foreColor = cfg.foreColor != null ? cfg.foreColor.getColor(object) : null;
          let backColor = cfg.backColor != null ? cfg.backColor.getColor(object) : null;
          let icon = cfg.iconmap != null ? cfg.iconmap[val] : null;
          let col = {value: val, formattedValue: formatVal, align: cfg.align, width: cfg.width, backColor: backColor, foreColor: foreColor, icon: icon, link: cfg.link, model: cfg.modal};
          if(!isNaN(val)) {
            grp.sums[c] += val;
            totalsums[c] += val;
          }
          cols.push(col);  
        } else {
          cols.push({formattedValue: null});
        }
      }
      if(this.openGroups.indexOf(grpKey) > -1)
        grp.lines.push({object: object, cols: cols});
    }
    for(var grpKey of Object.keys(this.groups)) {
      this.groups[grpKey].sums = this.calcSumLines(this.groups[grpKey].sums);
    }
    this.sums = this.calcSumLines(totalsums);
  }

  calcSumLines(sums: number[]): any[] {
    let ret = [];
    let c = 0;
    let firstWidth = -0.06;
    while(c < this.columns.length && this.columns[c].sum != true) {
      firstWidth += this.columns[c].width + 0.06;
      c++;
    }
    ret.push({width: firstWidth});
    while(c < this.columns.length) {
      let cfg = this.columns[c];
      if(cfg.sum) {
        let formatVal = cfg.format != null ? Formatter.format(sums[c], cfg.format) : sums[c];
        ret.push({width: cfg.width, align: cfg.align, formattedValue: formatVal, link: cfg.sumlink});  
      } else {
        ret.push({width: cfg.width});  
      }
      c++;
    }
    return ret;
  }

  getColumnConfig(object: RbObject, column: LinkTableColumnConfig): any {
    if(column.alt != null && column.attribute != null) {
      var val = object.get(column.attribute);
      if(column.alt[val] != null)
        return column.alt[val];
    } 
    return column;
  }


  clickColumnHeader(column: LinkTableColumnConfig) {
    this.dataset.filterSort({
      sort: {
        "0": {
          "attribute":column.attribute,
          "dir":1
        }
      }
    });
  }


  clickLink(link: LinkConfig, object: RbObject) {
    let event = link.getNavigationEvent(object, this.dataset);
    this.navigateService.navigateTo(event);
  }

  clickModal(modal: string) {
    this.modalService.open(modal);
  }

  clickSumLink(link: LinkConfig) {
    let event = link.getNavigationEvent(null, this.dataset);
    this.navigateService.navigateTo(event);
  }

  toggleGroup(groupKey) {
    if(this.openGroups.indexOf(groupKey) > -1) {
      this.openGroups.splice(this.openGroups.indexOf(groupKey), 1);
    } else {
      this.openGroups.push(groupKey);
    }
    this.calc();
  }

  showFooter(): boolean {
    return this.columns.reduce((acc, col) => acc || col.sum == true, false);
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
