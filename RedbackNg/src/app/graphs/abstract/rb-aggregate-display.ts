import { Component } from "@angular/core";
import { EventEmitter, HostBinding, HostListener, Input, Output } from "@angular/core";
import { RbComponent } from "app/abstract/rb-component";
import { RbDataObserverComponent } from "app/abstract/rb-dataobserver";
import { AppInjector } from "app/app.module";
import { RbAggregate } from "app/datamodel";
import { ValueComparator, Converter, Formatter } from "app/helpers";
import { RbAggregatesetComponent } from "app/rb-aggregateset/rb-aggregateset.component";
import { ApiService } from "app/services/api.service";
import { FilterService } from "app/services/filter.service";
import { NavigateService } from "app/services/navigate.service";
import { Subscription } from "rxjs";

@Component({template: ''})
export abstract class RbAggregateDisplayComponent extends RbComponent {
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
    @Input('linkview') linkview: string;
    @Input('linkfilter') linkfilter: string;
    @Input('title') title: string;
    @Input('aggregateset') aggregateset: RbAggregatesetComponent;
    @Input('script') script: string;
    @Input('scriptparam') scriptparam: any;
    
    @HostBinding('style.width') get styleWidth() { return (this.width != null ? ((0.88 * this.width) + 'vw'): null);}
    @HostBinding('style.height') get styleHeight() { return (this.height != null ? ((0.88 * this.height) + 'vw'): null);}

    colorScheme = {
      domain: ['#1C4E80', '#0091D5', '#A5D8DD', '#EA6A47', '#7E909A', '#202020']
    };
    graphData: any[] = [];
    hovering: boolean = false;
    private filterService: FilterService;
    private navigateService: NavigateService;
    private apiService: ApiService;
    private aggregatesetSubscription: Subscription;
    private _isLoading: boolean = false;
    
  
    constructor() {
      super();
      this.filterService = AppInjector.get(FilterService);
      this.navigateService = AppInjector.get(NavigateService);
      this.apiService = AppInjector.get(ApiService);
    }
  
    componentInit() {
      if(this.aggregateset != null) {
        this.aggregatesetSubscription = this.aggregateset.getObservable().subscribe(event => this.getGraphData());
      } 
      this.onActivationEvent(this.active);
    }
  
    componentDestroy() {
      if(this.aggregatesetSubscription != null) {
        this.aggregatesetSubscription.unsubscribe();
      } 
    }
  
    onActivationEvent(state: boolean) {
      if(this.active) {
        this.getGraphData();
      }
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

    get isLoading(): boolean {
      return this.aggregateset != null ? this.aggregateset.isLoading : this._isLoading;
    }

    getGraphData() {
      this._isLoading = true;
      if(this.script != null) {
        let param = this.filterService.resolveFilter(this.scriptparam, null, null, null, {});
        this.apiService.executeGlobal(this.script, param).subscribe(resp => {
          if(resp.data != null) this.graphData = resp.data;
          this._isLoading = false;
        });
      } else if(this.aggregates != null) {
        this.calcAggregateData();
      }
    }
  
    calcAggregateData() {
      this.graphData = [];
      if(this.categories != null) {
        let cats: String[] = [];
        for(let agg of this.aggregates) {
          let cat = this.nullToEmptyString(agg.getDimension(this.categories.dimension));
          if(cats.indexOf(cat) == -1) {
            cats.push(cat);
            let label = this.processLabel(agg.getDimension(this.categories.labelattribute), this.categories.labelformat);
            let series = this.calcSeriesDataForCategory(cat);
            let sum = series.reduce((acc, item) => acc + item.value, 0);
            this.graphData.push({code: cat, name: label, label: label, series: series, sum: sum});
          }
        }
        let sortKey = this.categories.sortby != null ? this.categories.sortby : 'code';
        let sortDir = this.categories.sortdir != null ? this.categories.sortdir : 1;
        this.graphData.sort((a, b) => ValueComparator.valueCompare(a, b, sortKey, sortDir)); 
        if(this.categories.top != null) {
          this.graphData = this.graphData.filter((value, index, array) => index < this.categories.top);
        }          
      } else {
        this.graphData = this.calcSeriesDataForCategory(null);
      } 
    }
  
    calcSeriesDataForCategory(cat: String) : any[] {
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
      if(typeof label == 'undefined' || label == null) {
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
      let objectname = this.linkview == null ? this.aggregateset.objectname : null;
      let view = this.linkview;
      let filter = null;

      if(this.linkfilter != null) {
        filter = this.filterService.resolveFilter(this.linkfilter, null, null, null, {cat: event.cat, code: event.code});
        filter = this.filterService.unresolveFilter(filter);
      } else if(this.aggregateset != null) {
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
        filter = this.filterService.mergeFilters(aggregatesetfilter, dimensionFilter);
      }
      let target: any = {
        filter: filter,
        reset: true
      };
      if(view != null) {
        target.view = view;
      } else {
        target.objectname = objectname;
      }
      this.navigateService.navigateTo(target);
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