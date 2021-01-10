import { Component, OnInit, Input, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { RbAggregate } from 'app/datamodel';
import { MapService } from 'app/map.service';

@Component({
  selector: 'rb-graph',
  templateUrl: './rb-graph.component.html',
  styleUrls: ['./rb-graph.component.css']
})
export class RbGraphComponent implements OnInit {
  @Input('type') type: String;
  @Input('label') label: String;
  @Input('series') series: any;
  @Input('categories') categories: any;
  @Input('value') value: any;
  @Input('min') min: number = 0;
  @Input('max') max: number = 100;
  @Input('aggregates') aggregates: RbAggregate[];
  @Output('selectDimensions') selectDimensionsEvent: EventEmitter<any> = new EventEmitter();

  colorScheme = {
    domain: ['#1C4E80', '#0091D5', '#A5D8DD', '#EA6A47', '#7E909A', '#202020']
  };
  graphData: any[];

  constructor(
    private mapService: MapService
  ) { }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    if("aggregates" in changes) {
      this.calcGraphData();
    }
  }

  calcGraphData() {
    if(this.categories != null) {
      this.graphData = [];
      let cats: String[] = [];
      for(let agg of this.aggregates) {
        let cat = this.nullToEmptyString(agg.getDimension(this.categories.dimension));
        if(cats.indexOf(cat) == -1) {
          cats.push(cat);
          let category: any = {
            name: this.nullToEmptyString(agg.getDimension(this.categories.labelattribute)), 
            series: this.getSeriesDataForCategory(cat)
          }
          this.graphData.push(category);
        }
      }
      this.graphData.sort((a, b) => (a.name.toString() > b.name.toString()) ? 1 : ((b.name.toString() > a.name.toString()) ? -1 : 0)); 
    } else {
      this.graphData = this.getSeriesDataForCategory(null);
    } 
  }

  getSeriesDataForCategory(cat: String) : any[] {
    let series: any[] = [];
    for(let agg of this.aggregates) {
      let thisCat: String = this.categories != null ? this.nullToEmptyString(agg.getDimension(this.categories.dimension)) : null;
      if(cat === null || cat === thisCat) {
        let name: any = this.nullToEmptyString(agg.getDimension(this.series.labelattribute));
        if(typeof name == 'string' && name.match(/^(?:[1-9]\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\d|2[0-3]):[0-5]\d:[0-5]\d(?:.\d{1,9})?(?:Z|[+-][01]\d:[0-5]\d)$/)) {
          name = new Date(Date.parse(name));
        }
        let value = agg.getMetric(this.value.name);
        series.push({name: name, label: 'll', value: value});
      }
    }
    if(this.series.sortby == null || this.series.sortby == 'name') {
      series.sort((a, b) => (a.name.toString() > b.name.toString()) ? 1 : ((b.name.toString() > a.name.toString()) ? -1 : 0)); 
    } else if(this.series.sortby == 'value') {
      series.sort((a, b) => a.value - b.value);
    }
    return series;
  }


  get xAxisLabel(): String {
    return this.categories != null ? this.categories.label : this.series.label;
  }

  get yAxisLabel(): String {
    return this.value.label
  }

  get is2d(): Boolean {
    return this.categories != null ? true : false;
  }

  get width(): number {
    return this.getSize().x;
  }

  get height(): number {
    return this.getSize().y;
  }

  private getSize(): any {
    if(this.type == 'hbar' || this.type == 'vbar') {
      if(this.is2d) {
        return {x: 500, y: 300};
      } else {
        return {x: 400, y: 300};
      }
    } else if(this.type == 'gauge') {
      return {x: 350, y: 250};
    } else if(this.type == 'number') {
      return {x: 170 * (this.graphData.length), y: 170}
    } else if(this.type == 'line') {
      return {x: 800, y: 250};
    } else {
      return {x: 400, y: 300};
    }

  }

  private nullToEmptyString(str: String): String {
    if(str == null) {
      return "";
    } else {
      return str;
    }
  }

  public onClick(event: any) {
    console.log("Graph click");
    let filter = {};
    const name = event.name;
    this.aggregates.forEach(agg => {
      if(name == (agg.getDimension(this.series.labelattribute) || "")) {
        let dimensionValue = agg.getDimension(this.series.dimension);
        filter[this.series.dimension] = dimensionValue == null ? null : typeof dimensionValue == 'number' ? dimensionValue : "'" + dimensionValue + "'";
      }
    });
    const cat = event.series;
    if(cat != null) {
      this.aggregates.forEach(agg => {
        if(cat == (agg.getDimension(this.categories.labelattribute) || "")) {
          let dimensionValue = agg.getDimension(this.categories.dimension);
          filter[this.categories.dimension] = dimensionValue == null ? null : typeof dimensionValue == 'number' ? dimensionValue : "'" + dimensionValue + "'";
        }
      });
    }
    this.selectDimensionsEvent.emit(filter);
  }
}
