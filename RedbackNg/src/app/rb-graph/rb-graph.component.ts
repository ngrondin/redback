import { Component, OnInit, Input, SimpleChanges } from '@angular/core';
import { RbAggregate } from 'app/datamodel';

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

  colorScheme = {
    domain: ['#9370DB', '#87CEFA', '#FA8072', '#FF7F50', '#90EE90', '#9370DB']
  };
  graphData: any[];

  constructor() { }

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
        let cat = agg.getDimension(this.categories.dimension);
        if(cats.indexOf(cat) == -1) {
          cats.push(cat);
          let category: any = {
            name: agg.getDimension(this.categories.labelattribute),
            series: this.getSeriesDataForCategory(cat)
          }
          this.graphData.push(category);
        }
      }
    } else {
      this.graphData = this.getSeriesDataForCategory(null);
    } 
  }

  getSeriesDataForCategory(cat: string) : any[] {
    let series: any[] = [];
    for(let agg of this.aggregates) {
      let thisCat: String = this.categories != null ? agg.getDimension(this.categories.dimension) : null;
      if(cat == null || cat == thisCat) {
        let data: any = {
          name: agg.getDimension(this.series.labelattribute),
          value: agg.getMetric(this.value.name)
        }
        series.push(data);
      }
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
      return {x: 300, y: 300}
    } else {
      return {x: 400, y: 300};
    }

  }

}