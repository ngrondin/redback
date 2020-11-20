import { Directive, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { RbObject, RbAggregate } from 'app/datamodel';
import { DataService } from 'app/data.service';
import { MapService } from 'app/map.service';

@Directive({
  selector: 'rb-aggregateset',
  exportAs: 'aggregateset'
})
export class RbAggregatesetDirective {
  @Input('active') active: boolean;
  @Input('object') objectname: string;
  @Input('relatedObject') relatedObject: RbObject;
  @Input('relatedFilter') relatedFilter: any;
  @Input('baseFilter') baseFilter: any;
  @Input('tuple') tuple: any;
  @Input('metrics') metrics: any;

  @Input('userFilter') inputUserFilter: any;
  @Input('searchString') inputSearchString: any;
  @Input('selectedObject') inputSelectedObject: any;

  @Output('initiated') initated: EventEmitter<any> = new EventEmitter();
  @Output('userFilterChange') userFilterChange: EventEmitter<any> = new EventEmitter();
  @Output('searchStringChange') searchStringChange: EventEmitter<any> = new EventEmitter();
  @Output('selectedObjectChange') selectedObjectChange: EventEmitter<any> = new EventEmitter();
  @Output('navigate') navigateEvent: EventEmitter<any> = new EventEmitter();

  public aggregates: RbAggregate[] = [];
  public searchString: string;
  public userFilter: any;
  public isLoading: boolean;
  public initiated: boolean = false;
  public firstLoad: boolean = true;


  constructor(
    private dataService: DataService,
    private mapService: MapService
  ) { }

  ngOnChanges(changes: SimpleChanges) {
    let doRefresh: boolean = false;
    if("relatedObject" in changes || "active" in changes) {
      doRefresh = true;
    }
    if("inputUserFilter" in changes && this.userFilter != this.inputUserFilter) {
      this.userFilter = this.inputUserFilter;
      doRefresh = true;
    }
    if("inputSearchString" in changes && this.searchString != this.inputSearchString) {
      this.searchString = this.inputSearchString;
      doRefresh = true;
    }
    if(doRefresh && this.initiated && this.active) {
      this.refreshData();
    }
  }

  ngOnInit() {
    this.initiated = true;
    this.refreshData();
    this.initated.emit(this);
  }

  public refreshData() {
    this.aggregates = [];
    if(this.relatedFilter == null || (this.relatedFilter != null && this.relatedObject != null)) {
      const filter = this.mapService.resolveMap(this.mergeFilters(), this.relatedObject, null, this.relatedObject);
      this.dataService.aggregateObjects(this.objectname, filter, this.tuple, this.metrics).subscribe(
        data => this.setAggregates(data)
      );
      this.isLoading = true;
    }
  }

  private mergeFilters() : any {
    let filter = {};
    if(this.baseFilter != null) {
      filter = this.mapService.mergeMaps(filter, this.baseFilter);
    }

    if(this.relatedFilter != null && this.relatedObject != null) {
      filter = this.mapService.mergeMaps(filter, this.relatedFilter);
    } 

    if(this.userFilter != null) {
      filter = this.mapService.mergeMaps(filter, this.userFilter);
    }

    return filter;
  }

  public setAggregates(data: RbAggregate[]) {
    this.aggregates = data;
  }


  public selectDimensions(dimensionsFilter: any) {
    let filter = this.mergeFilters();
    filter = this.mapService.mergeMaps(filter, dimensionsFilter);
    let target = {
      object: this.objectname,
      filter: filter,
      reset: true
    };
    this.navigateEvent.emit(target);
  }
}
