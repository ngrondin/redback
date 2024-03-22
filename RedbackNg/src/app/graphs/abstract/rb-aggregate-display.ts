import { Component } from "@angular/core";
import { EventEmitter, HostBinding, HostListener, Input, Output } from "@angular/core";
import { RbDataObserverComponent } from "app/abstract/rb-dataobserver";
import { AppInjector } from "app/app.module";
import { RbAggregate } from "app/datamodel";
import { ValueComparator, Converter, Formatter } from "app/helpers";
import { FilterService } from "app/services/filter.service";

@Component({template: ''})
export abstract class RbAggregateDisplayComponent extends RbDataObserverComponent {
    //@Input('label') label: String;
    @Input('series') series: any;
    @Input('categories') categories: any;
    @Input('value') value: any;
    @Input('target') target: any;
    @Input('min') min: number;
    @Input('max') max: number;
    @Input('grow') grow: number;
    @Input('shrink') shrink: number;
    @Input('width') width: number;
    @Input('height') height: number;
    @Input('colormap') colormap: any;
    
    @Output('navigate') navigate: EventEmitter<any> = new EventEmitter();

    @HostBinding('style.width.px') get widthStyle() { return this.width != null ? this.width : null;}
    @HostBinding('style.height.px') get heightStyle() { return this.height != null ? this.height : null;}
    
    colorScheme = {
      domain: ['#1C4E80', '#0091D5', '#A5D8DD', '#EA6A47', '#7E909A', '#202020']
    };
    graphData: any[];
    hovering: boolean = false;
    private filterService: FilterService;
  
    constructor() {
      super();
      this.filterService = AppInjector.get(FilterService);
    }
  
    dataObserverInit() {
      setTimeout(() => this.calcGraphData(), 1); // because the graph needs the parent to be fully drawn to calculate its dimensions
    }
  
    dataObserverDestroy() {
    }
  
    onActivationEvent(state: boolean) {
      if(state == true) {
        setTimeout(() => this.calcGraphData(), 1);
      }
    }
  
    onDatasetEvent(event: string) {
      this.calcGraphData();
    }
  
    get aggregates(): RbAggregate[] {
      return this.aggregateset != null ? this.aggregateset.aggregates : null;
    }
  
    get xAxisLabel(): String {
      return this.categories != null ? this.categories.label : this.series != null ? this.series.label : null;
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
            let label = this.processLabel(agg.getDimension(this.categories.labelattribute), this.categories.labelformat);
            let series = this.getSeriesDataForCategory(cat);
            this.graphData.push({code: cat, name: label, label: label, series: series});
          }
        }
        this.graphData.sort((a, b) => ValueComparator.valueCompare(a, b, 'code')); 
      } else {
        this.graphData = this.getSeriesDataForCategory(null);
      } 
    }
  
    getSeriesDataForCategory(cat: String) : any[] {
      let series: any[] = [];
      for(let agg of this.aggregates) {
        let thisCat: String = this.categories != null ? this.nullToEmptyString(agg.getDimension(this.categories.dimension)) : null;
        if(cat === null || cat === thisCat) {
          let code: any = this.series != null ? this.nullToEmptyString(agg.getDimension(this.series.dimension)) : null;
          let label: any = this.series ? this.processLabel(agg.getDimension(this.series.labelattribute), this.series.labelformat) : null;
          let value = agg.getMetric(this.value.name);
          if(this.value.convert != null) {
            value = Converter.convert(value, this.value.convert);
          }
          let target = undefined;
          if(this.target != null) {
            target = agg.getMetric(this.target.name);
          }
          series.push({code: code, name: label, label: label, value: value, target: target}); //TODO: Tfor legacy reasons
        }
      }
      if(this.series != null) {
        let sortKey = this.series.sortby == null || this.series.sortby == 'name' ? 'label' : 'value';
        let sortDir = this.series.sortdir != null ? this.series.sortdir : 1;
        series.sort((a, b) => ValueComparator.valueCompare(a, b, sortKey, sortDir));
        if(this.series.top != null) {
          series = series.filter((value, index, array) => index < this.series.top);
        }  
      }
      return series;
    }

    private processLabel(label: any, labelFormat: any = null) : any {
      if(label == null) {
        return "";
      } else if(typeof label == 'string') {
        if(label.match(/^(?:[1-9]\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\d|2[0-3]):[0-5]\d:[0-5]\d(?:.\d{1,9})?(?:Z|[+-][01]\d:[0-5]\d)$/)) {
          let dt = new Date(Date.parse(label));
          if(labelFormat != null) {
            return Formatter.formatDateTimeCustom(dt, labelFormat);
          }
          return dt;
        } 
      } 
      return label;
    }

  
   private nullToEmptyString(str: String): String {
      if(str == null) {
        return "";
      } else {
        return str;
      }
    }
  
    public onClick(event: any) {
      let dimensionFilter = {};
      if(event.code != null && event.code != "" && this.series.dimension != null) {
        dimensionFilter[this.series.dimension] = "'" + event.code + "'";
      }
      if(event.name != null && event.name != "") { //For backwards compatibility (remove when dynamic graph is removed)
        const aggregate = this.aggregates.find(agg => agg.getDimension(this.series.labelattribute) == event.name);
        if(aggregate != null) {
          const code = aggregate.getDimension(this.series.dimension);
          dimensionFilter[this.series.dimension] = "'" + code + "'";
        }
      }
      if(event.cat != null && event.cat != "" && this.categories.dimension != null) {
        dimensionFilter[this.categories.dimension] = "'" + event.cat + "'";
      }
      let aggregatesetfilter = this.aggregateset.mergeFilters();
      let filter = this.filterService.mergeFilters(aggregatesetfilter, dimensionFilter);
      let target = {
        object: this.aggregateset.objectname,
        filter: filter,
        label: (event.code ?? event.name),
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