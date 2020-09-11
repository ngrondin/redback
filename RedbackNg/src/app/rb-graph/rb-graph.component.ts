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
  @Output() navigate: EventEmitter<any> = new EventEmitter();

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
    } else {
      this.graphData = this.getSeriesDataForCategory(null);
    } 
  }

  getSeriesDataForCategory(cat: String) : any[] {
    let series: any[] = [];
    for(let agg of this.aggregates) {
      let thisCat: String = this.categories != null ? agg.getDimension(this.categories.dimension) : null;
      if(cat == null || cat == thisCat) {
        let name: any = this.nullToEmptyString(agg.getDimension(this.series.labelattribute));
        let value = agg.getMetric(this.value.name);
        if(!isNaN(Date.parse(name))) {
          name = new Date(Date.parse(name.toString()));
        }
        series.push({name: name, label: 'll', value: value});
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
    let objectname = null;
    let filter = {};
    const name = event.name;
    this.aggregates.forEach(agg => {
      objectname = agg.objectname;
      if(name == agg.getDimension(this.series.labelattribute)) {
        filter[this.series.dimension] = "'" + agg.getDimension(this.series.dimension) + "'";
      }
    });
    const cat = event.series;
    if(cat != null) {
      this.aggregates.forEach(agg => {
        if(cat == agg.getDimension(this.categories.labelattribute)) {
          filter[this.categories.dimension] = "'" + agg.getDimension(this.categories.dimension) + "'";
        }
      });
    }
    let target = {
      object: objectname,
      filter: filter,
      reset: true
    };
    this.navigate.emit(target);
  }
}
