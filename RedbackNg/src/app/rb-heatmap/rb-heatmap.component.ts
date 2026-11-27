import { Component, Input } from '@angular/core';
import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { RbObject } from 'app/datamodel';
import { HeatmapAxisConfig, HeatmapEntry, HeatmapHeader, HeatmapSeriesConfig } from './rb-heatmap-models';

/**
 * Heat map: a 2D matrix of boxes. The rows come from one dataset, the columns from another,
 * and each series object is drawn in the box of its row and its column, with its own label and color.
 * Clicking a box selects its series object, its row object and its column object, then runs the
 * series clickscript if there is one. Nothing is draggable.
 * The body scrolls both ways; the column headers stay on top and the row headers stay on the left.
 * Dragging the right edge of a column header (or of the corner, for the row headers) changes that width.
 */
@Component({
  selector: 'rb-heatmap',
  templateUrl: './rb-heatmap.component.html',
  styleUrls: ['./rb-heatmap.component.css']
})
export class RbHeatmapComponent extends RbDataCalcComponent<HeatmapSeriesConfig> {
  @Input('rows') rows: any;
  @Input('columns') columns: any;
  @Input('cellwidth') cellWidth: number = 60;
  @Input('cellheight') cellHeight: number = 28;
  @Input('rowheaderwidth') rowHeaderWidth: number = 220;
  @Input('emptymessage') emptymessage: string = "Nothing to show";

  rowsConfig: HeatmapAxisConfig;
  columnsConfig: HeatmapAxisConfig;

  rowHeaders: HeatmapHeader[] = [];
  colHeaders: HeatmapHeader[] = [];
  cells: HeatmapEntry[][][] = [];
  colWidths: {[key: string]: number} = {};

  constructor() {
    super();
  }

  dataCalcInit() {
    this.rowsConfig = this.rows != null ? new HeatmapAxisConfig(this.rows) : null;
    this.columnsConfig = this.columns != null ? new HeatmapAxisConfig(this.columns) : null;
  }

  dataCalcDestroy() {
  }

  createSeriesConfig(json: any): HeatmapSeriesConfig {
    return new HeatmapSeriesConfig(json);
  }

  getFilterSortForSeries(config: HeatmapSeriesConfig) {
    return null;
  }

  calc() {
    this.rowHeaders = this.headersFor(this.rowsConfig);
    this.colHeaders = this.headersFor(this.columnsConfig);
    let rowIndex = this.indexOf(this.rowHeaders);
    let colIndex = this.indexOf(this.colHeaders);
    this.cells = this.rowHeaders.map(r => this.colHeaders.map(c => []));
    for(let cfg of this.activeSeries) {
      for(let object of (this.getList(cfg) ?? [])) {
        let r = rowIndex[object.get(cfg.rowAttribute)];
        let c = colIndex[object.get(cfg.colAttribute)];
        if(r != null && c != null) {
          let label = cfg.label?.getValue(object);
          let color = cfg.color?.getColor(object) ?? "#d0d0d0";
          let tip = cfg.tip?.getValue(object);
          this.cells[r][c].push(new HeatmapEntry(label != null ? "" + label : "", color, tip != null ? "" + tip : null, object, cfg));
        }
      }
    }
  }

  private headersFor(cfg: HeatmapAxisConfig): HeatmapHeader[] {
    let list: RbObject[] = cfg != null && this.lists != null ? this.lists[cfg.dataset] : null;
    return (list ?? []).map(o => {
      let label = cfg.label.getValue(o);
      return new HeatmapHeader(o.get(cfg.keyAttribute), label != null ? "" + label : "", o);
    });
  }

  private indexOf(headers: HeatmapHeader[]): any {
    let index = {};
    headers.forEach((h, i) => { if(h.key != null && index[h.key] == null) index[h.key] = i; });
    return index;
  }

  get isEmpty(): boolean {
    return this.rowHeaders.length == 0 || this.colHeaders.length == 0;
  }

  get bodyWidth(): number {
    return this.colHeaders.reduce((sum, h) => sum + this.colWidth(h), this.rowHeaderWidth);
  }

  colWidth(header: HeatmapHeader): number {
    return this.colWidths[header.key] ?? this.cellWidth;
  }

  mousedownColumnDrag(event: any, header: HeatmapHeader) {
    this.drag(event, this.colWidth(header), (w) => this.colWidths[header.key] = w);
  }

  mousedownRowHeaderDrag(event: any) {
    this.drag(event, this.rowHeaderWidth, (w) => this.rowHeaderWidth = w);
  }

  private drag(event: any, startWidth: number, setWidth: (w: number) => void) {
    let startClientX = event.clientX;
    let moveListener = (e) => setWidth(Math.max(20, Math.round(startWidth + e.clientX - startClientX)));
    let upListener = () => {
      window.removeEventListener('mousemove', moveListener);
      window.removeEventListener('mouseup', upListener);
    };
    event.stopPropagation();
    event.preventDefault();
    window.addEventListener('mousemove', moveListener);
    window.addEventListener('mouseup', upListener);
  }

  isSelected(entry: HeatmapEntry): boolean {
    return this.getDatasetForConfig(entry.config)?.isObjectSelected(entry.object) ?? false;
  }

  isHeaderSelected(header: HeatmapHeader, cfg: HeatmapAxisConfig): boolean {
    return this.getDatasetForConfig(cfg)?.isObjectSelected(header.object) ?? false;
  }

  clickEntry(entry: HeatmapEntry, r: number, c: number) {
    let ds = this.getDatasetForConfig(entry.config);
    let alreadySelected = ds?.isObjectSelected(entry.object);
    if(!alreadySelected) ds?.select(entry.object);
    this.getDatasetForConfig(this.rowsConfig)?.select(this.rowHeaders[r].object);
    this.getDatasetForConfig(this.columnsConfig)?.select(this.colHeaders[c].object);
    if(entry.config.click != null) {
      entry.config.click({alreadyselected: alreadySelected, object: entry.object, dataset: ds, row: this.rowHeaders[r].object, column: this.colHeaders[c].object});
    }
  }
}
