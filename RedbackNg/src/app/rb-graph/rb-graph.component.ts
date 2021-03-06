import { ContentChild, HostBinding, ViewChild, ViewContainerRef, ViewRef } from '@angular/core';
import { HostListener } from '@angular/core';
import { Component, OnInit, Input, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { RbAggregateObserverComponent } from 'app/abstract/rb-aggregateobserver';
import { RbAggregate } from 'app/datamodel';
import { ValueComparator } from 'app/helpers';
import { RbAggregatesetComponent } from 'app/rb-aggregateset/rb-aggregateset.component';
import { MapService } from 'app/services/map.service';

@Component({
  selector: 'rb-graph',
  templateUrl: './rb-graph.component.html',
  styleUrls: ['./rb-graph.component.css']
})
export class RbGraphComponent extends RbAggregateObserverComponent {
  @Input('graphtype') type: String;
  @Input('label') label: String;
  @Input('series') series: any;
  @Input('categories') categories: any;
  @Input('value') value: any;
  @Input('min') min: number;
  @Input('max') max: number;
  @Input('grow') grow: number;
  @Input('shrink') shrink: number;
  @Output('navigate') navigate: EventEmitter<any> = new EventEmitter();
  @HostBinding('style.flex-grow') get flexgrow() { return this.grow != null ? this.grow : 1;}
  @HostBinding('style.flex-shrink') get flexshrink() { return this.shrink != null ? this.shrink : 1;}
  //@HostBinding('style.width.px') get hostWidth() { return this.type == 'number' ? 140 : null; }
  //@HostBinding('style.height.px') get hostHeight() { return this.type == 'number' ? 140 : null; }
  
  colorScheme = {
    domain: ['#1C4E80', '#0091D5', '#A5D8DD', '#EA6A47', '#7E909A', '#202020']
  };
  graphData: any[];
  hovering: boolean = false;

  constructor(
    private mapService: MapService
  ) {
    super();
  }

  aggregatesetObserverInit() {
    setTimeout(() => this.calcGraphData(), 1); // because the graph needs the parent to be fully drawn to calculate its dimensions
  }

  aggregatesetObserverDestroy() {
  }

  onActivationEvent(state: boolean) {
    if(state == true) {
      setTimeout(() => this.calcGraphData(), 1);
    }
  }

  onAggregatesetEvent(event: string) {
    this.calcGraphData();
  }

  get aggregates(): RbAggregate[] {
    return this.aggregateset != null ? this.aggregateset.aggregates : null;
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

  get showRefresh(): boolean {
    return this.hovering == true;
  }

  calcGraphData() {
    this.graphData = [];
    if(this.categories != null) {
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
      this.graphData.sort((a, b) => ValueComparator.valueCompare(a, b, 'name')); 
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
    let sortKey = this.series.sortby == null || this.series.sortby == 'name' ? 'name' : 'value';
    let sortDir = this.series.sortdir != null ? this.series.sortdir : 1;
    series.sort((a, b) => ValueComparator.valueCompare(a, b, sortKey, sortDir));
    if(this.series.top != null) {
      series = series.filter((value, index, array) => index < this.series.top);
    }
    return series;
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
      return {x: 170 * (this.graphData != null ? this.graphData.length : 0), y: 170}
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
    let dimensionFilter = {};
    const name = event.name;
    this.aggregates.forEach(agg => {
      if(name == (agg.getDimension(this.series.labelattribute) || "")) {
        let dimensionValue = agg.getDimension(this.series.dimension);
        dimensionFilter[this.series.dimension] = dimensionValue == null ? null : typeof dimensionValue == 'number' ? dimensionValue : "'" + dimensionValue + "'";
      }
    });
    const cat = event.series;
    if(cat != null) {
      this.aggregates.forEach(agg => {
        if(cat == (agg.getDimension(this.categories.labelattribute) || "")) {
          let dimensionValue = agg.getDimension(this.categories.dimension);
          dimensionFilter[this.categories.dimension] = dimensionValue == null ? null : typeof dimensionValue == 'number' ? dimensionValue : "'" + dimensionValue + "'";
        }
      });
    }

    let aggregatesetfilter = this.aggregateset.mergeFilters();
    let filter = this.mapService.mergeMaps(aggregatesetfilter, dimensionFilter);
    let target = {
      object: this.aggregateset.objectname,
      filter: filter,
      reset: true
    };
    this.navigate.emit(target);
  }

  refresh() {
    this.aggregateset.refreshData();
  }

  @HostListener('mouseenter', ['$event']) onMouseEnter($event) {
    this.hovering = true;
  }

  @HostListener('mouseleave', ['$event']) onMouseLeave($event) {
    this.hovering = false;
  }

}
