import { Component, HostBinding, Input, QueryList, ViewChildren } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject, RELATED_LOADING } from 'app/datamodel';
import { ColorConfig, Evaluator, Formatter, LinkConfig, RecalcPlanner, ValueComparator } from 'app/helpers';
import { ModalService } from 'app/services/modal.service';
import { NavigateService } from 'app/services/navigate.service';
import { PopupService } from 'app/services/popup.service';
import { UserprefService } from 'app/services/userpref.service';
import { RbColumnSelectorComponent } from 'app/popups/rb-column-selector/rb-column-selector.component';
import { RbRowViewContainerDirective } from 'app/utils/rowviewcontainer/rb-rowviewcontainer.directive';
import { LinkTableColumnConfig, LinkTableGroupConfig } from './rb-linktable-models';



@Component({
  selector: 'rb-linktable',
  templateUrl: './rb-linktable.component.html',
  styleUrls: ['./rb-linktable.component.css']
})
export class RbLinktableComponent extends RbDataObserverComponent {
  @Input('columns') _cols: any;
  @Input('group') _grp: any;
  @Input('view') view?: string;
  @Input('grid') grid: boolean = false;
  @Input('flexfill') flexfill: boolean = true;
  @HostBinding('style.flex') get fillSpace() { return this.flexfill ? "1 1 0" : "0 0 auto";}
  @ViewChildren(RbRowViewContainerDirective) headers: QueryList<RbRowViewContainerDirective>;

  allColumns: LinkTableColumnConfig[] = [];
  columns: LinkTableColumnConfig[] = [];
  group?: LinkTableGroupConfig;
  reachedBottom: boolean = false;
  scrollLeft: number = 0;
  groups: any = {};
  sums: any[] = [];
  openGroups: string[] = ['_'];
  localSort: {column: LinkTableColumnConfig, dir: number} | null = null;

  recalcPlanner!: RecalcPlanner;

  constructor(
    private modalService: ModalService,
    private navigateService: NavigateService,
    public userprefService: UserprefService,
    private popupService: PopupService,
  ) {
    super();
  }

  dataObserverInit() {
    this.recalcPlanner = new RecalcPlanner(this.calc.bind(this));
    this.allColumns = [];
    for(let item of this._cols) {
      let colPref = this.userPref != null && this.userPref.cols != null ? this.userPref.cols[item.id] : null;
      if(!(colPref == false || (colPref != null && colPref.hide == true))) {
        this.allColumns.push(new LinkTableColumnConfig(item, colPref));
      }
    }
    this.columns = this.allColumns;
    this.loadPrefs();
    if(this._grp != null) {
      this.group = new LinkTableGroupConfig(this._grp);
    }
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
    this.redraw();
  }

  onActivationEvent(state: boolean) {
    this.scrollLeft = 0;
    this.redraw();
  }

  get prefName() : string | null {
    return this.id ?? this.dataset?.getId() ?? null;
  }

  get userPref() : any {
    return this.prefName != null ? this.userprefService.getCurrentViewUISwitch("linktable", this.prefName) : null;
  }

  get hasGroup(): boolean {
    return this.group != null;
  }

  get groupKeys(): string[] {
    return Object.keys(this.groups).sort();
  }

  redraw() {
    this.recalcPlanner.request();
  }

  calc() {
    this.groups = {};
    let totalsums = this.columns.map(c => 0);
    for(let object of this.list) {
      let grpKey: string = "_";
      if(this.group != null) {
        if(this.group.expression != null) {
          grpKey = this.group.expression(object, undefined, this.dataset);
        } else if(this.group.attribute != null) {
          grpKey = object.get(this.group.attribute);
        }
      }
      if(this.groups[grpKey] == null) this.groups[grpKey] = {sums: this.columns.map(c => 0), backColors: this.columns.map(c => null), lines: []};
      let grp = this.groups[grpKey];
      let cols = [];
      for(let c = 0; c < this.columns.length; c++) {
        let cfg = this.getColumnConfig(object, this.columns[c]);
        if(cfg != null) {
          let val = null;
          let loading = false;
          if (cfg.expression != null) {
            try {
              val = cfg.expression(object, null, this.dataset);
            } catch (err) { }
          } else if(cfg.attribute != null) {
            val = object.get(cfg.attribute);
          }
          if(val === RELATED_LOADING) {
            val = null;
            loading = true;
          }
          
          let checkboxattr = cfg.checkbox == true && cfg.attribute != null ? cfg.attribute : null;
          let formatVal = cfg.format != null ? Formatter.format(val, cfg.format) : val;
          let orderVal = cfg.orderAttribute != null && object.get(cfg.orderAttribute) !== RELATED_LOADING ? object.get(cfg.orderAttribute) : val;
          let foreColor = cfg.foreColor != null ? cfg.foreColor.getColor(object) : null;
          let backColor = cfg.backColor != null ? cfg.backColor.getColor(object) : null;
          let bold = cfg.bold != null ? cfg.bold.getValue(object) : null;
          let icon = cfg.iconmap != null ? cfg.iconmap[val] : null;
          let col = { value: val, formattedValue: formatVal, orderValue: orderVal, align: cfg.align, wrap: cfg.wrap, backColor: backColor, foreColor: foreColor, bold: bold, icon: icon, checkbox: cfg.checkbox ?? false, checkboxattr: checkboxattr, checkboxevent: cfg.checkboxevent, link: cfg.link, modal: cfg.modal, loading: loading, getWidthStr: () => cfg.getWidthStr() };
          let sumVal = loading ? 0 : cfg.expression == null && cfg.attribute == null ? 1 : !isNaN(val) ? val : 0;
          grp.sums[c] += sumVal;
          totalsums[c] += sumVal;
          if(backColor != null && grp.backColors[c] == null) grp.backColors[c] = backColor;
          cols.push(col);
        } else {
          cols.push({formattedValue: null});
        }
      }
      if(this.openGroups.indexOf(grpKey) > -1)
        grp.lines.push({object: object, cols: cols});
    }
    for(var grpKey of Object.keys(this.groups)) {
      this.groups[grpKey].sums = this.calcSumLines(this.groups[grpKey].sums, this.groups[grpKey].backColors);
      if(this.localSort != null) {
        let c = this.columns.indexOf(this.localSort.column);
        if(c > -1 && c < this.columns.length) {
          let dir = this.localSort.dir;
          this.groups[grpKey].lines.sort((a: any, b: any) => ValueComparator.sort(a.cols[c].orderValue, b.cols[c].orderValue) * dir);          
        }
      }
    }
    this.sums = this.calcSumLines(totalsums);
  }

  calcSumLines(sums: number[], backColors?: string[]): any[] {
    let ret = [];
    let firstSumIndex = this.columns.findIndex(col => col.sum == true);
    if(firstSumIndex > -1) {
      let getFirstWidthStr = () => {
        let firstWidth = 0;//-0.06;
        for(var c = 0; c < firstSumIndex; c++) {
          firstWidth += this.columns[c].width;
        }
        return'min(' + (firstWidth * 0.88) + 'vw, ' + (firstWidth * 16.896) + 'px)';
      }
      ret.push({getWidthStr: () => getFirstWidthStr(), link: this.group?.link});
      for(var c = firstSumIndex; c < this.columns.length; c++) {
        let cfg = this.columns[c];
        if(cfg.sum) {
          let formatVal = cfg.format != null ? Formatter.format(sums[c], cfg.format) : sums[c];
          let backColor = backColors != null ? backColors[c] : null;
          ret.push({getWidthStr: () => cfg.getWidthStr(), align: cfg.align, formattedValue: formatVal, link: cfg.sumlink, backColor: backColor});
        } else {
          ret.push({getWidthStr: () => cfg.getWidthStr()});
        }
      }
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

  //Only an own attribute of the object can be sorted on the database, expressions and related paths (aircraft.code) sort the loaded rows
  isDatabaseSort(column: LinkTableColumnConfig): boolean {
    return column.expression == null && column.orderAttribute != null && column.orderAttribute.indexOf('.') == -1;
  }

  getSortDir(column: LinkTableColumnConfig): number | null {
    if(this.localSort != null) {
      return this.localSort.column == column ? this.localSort.dir : null;
    }
    let sort = this.dataset?.getUserSort()?.["0"];
    return sort != null && this.isDatabaseSort(column) && sort.attribute == column.orderAttribute ? sort.dir : null;
  }

  clickColumnHeader(column: LinkTableColumnConfig) {
    let dir = this.getSortDir(column) == 1 ? -1 : 1;
    if(this.isDatabaseSort(column)) {
      this.localSort = null;
      this.dataset?.filterSort({
        sort: {
          "0": {
            "attribute":column.orderAttribute,
            "dir":dir
          }
        }
      });
    } else {
      this.localSort = {column: column, dir: dir};
    }
    this.redraw();
  }

  mousedownHeaderDrag(event: any, column: LinkTableColumnConfig) {
    let startClientX = event.clientX;
    let startHeaderWidthPX = column.width;
    let moveListener = (event) => {
      let delta = (event.clientX - startClientX) / 16.8;
      column.setWidth(Math.max(0, Math.round(startHeaderWidthPX + delta)));
    };
    let upListener = (event) => {
      window.removeEventListener('mousemove', moveListener);
      window.removeEventListener('mouseup', upListener);
      this.savePrefs();
    };
    event.stopPropagation();
    window.addEventListener('mousemove', moveListener);
    window.addEventListener('mouseup', upListener);
  }

  openColumnSelector(event: any, column: LinkTableColumnConfig) {
    event.preventDefault();
    if(this.prefName != null) {
      let items = this.allColumns.filter(c => c.label != null).map(c => ({key: c.key, label: c.label, shown: this.columns.includes(c)}));
      let anchor = this.headers.find(h => h.object == column).viewContainerRef;
      let popup = this.popupService.openPopup(anchor, RbColumnSelectorComponent, {items: items}, event.buttons != 0);
      popup.instance.selected.subscribe(selected => {
        this.popupService.closePopup();
        let selectedKeys = selected.filter(s => s.shown == true).map(s => s.key);
        this.columns = this.allColumns.filter(c => selectedKeys.includes(c.key));
        this.savePrefs();
        this.redraw();
      });
    }
  }

  clickLink(link: LinkConfig, object: RbObject) {
    let event = link.getNavigationEvent(object, this.dataset);
    this.navigateService.navigateTo(event);
  }

  clickModal(modal: string, object: RbObject) {
    if(this.dataset != null) {
      this.dataset.select(object);
      this.modalService.open(modal);
    }
  }

  clickSumLink(link: LinkConfig) {
    let event = link.getNavigationEvent(undefined, this.dataset);
    this.navigateService.navigateTo(event);
  }

  toggleGroup(groupKey: string) {
    if(this.openGroups.indexOf(groupKey) > -1) {
      this.openGroups.splice(this.openGroups.indexOf(groupKey), 1);
    } else {
      this.openGroups.push(groupKey);
    }
    this.redraw();
  }

  toggleCheckbox(attribute: string, eventfunc: Function, object: RbObject) {
    if (attribute != null) {
      let val = object.get(attribute);
      val = val == true ? false : true;
      object.setValue(attribute, val);
    }
    if (eventfunc != null) {
      eventfunc(object);
    }
  }

  showFooter(): boolean {
    return this.columns.reduce((acc, col) => acc || col.sum == true, false);
  }

  onScroll(event: any) {
    this.scrollLeft = event.target.scrollLeft;
    if(this.dataset != null && event.currentTarget.scrollTop > Math.floor(event.currentTarget.scrollHeight - event.currentTarget.clientHeight - 30) && this.reachedBottom == false) {
      this.dataset.fetchNextPage();
      this.reachedBottom = true;
      setTimeout(() => {this.reachedBottom = false}, 1000);
    }
  }

  savePrefs() {
    let data = {
      //hiddencols: selected.filter((i: any) => !i.shown).map((i: any) => i.key),
      hiddencols: this.allColumns.filter(c => !this.columns.includes(c)).map(c => c.key),
      colwidths: this.columns.reduce((acc, c) => {acc[c.key] = c.width; return acc;}, {})
    };
    this.userprefService.setUISwitch('user', 'linktable', this.prefName, data);
  }

  loadPrefs() {
    let hidden = this.userPref?.hiddencols ?? [];
    this.columns = this.allColumns.filter(c => !hidden.includes(c.key));
    let widths = this.userPref?.colwidths;
    if(widths != null) {
      for(var key of Object.keys(widths)) {
        var col = this.allColumns.find(c => c.key == key);
        if(col != null) {
          col.setWidth(widths[key]);
        }
      }
    }
    if(this.localSort != null && !this.columns.includes(this.localSort.column)) this.localSort = null;
  }
}
