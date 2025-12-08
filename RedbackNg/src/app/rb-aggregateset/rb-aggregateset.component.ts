import { EventEmitter, Input, Output } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { DataService } from 'app/services/data.service';
import { DataTarget, RbAggregate, RbObject } from 'app/datamodel';
import { FilterService } from 'app/services/filter.service';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { Observable, Observer } from 'rxjs';
import { RbSetComponent } from 'app/abstract/rb-set';

@Component({
  selector: 'rb-aggregateset',
  templateUrl: './rb-aggregateset.component.html',
  styleUrls: ['./rb-aggregateset.component.css']
})
export class RbAggregatesetComponent extends RbSetComponent {
  @Input('tuple') tuple: any;
  @Input('metrics') metrics: any;
  @Input('base') base: any;
  
  public aggregates: RbAggregate[] = [];
  public userSearch: string;
  public userFilter: any;
  public firstLoad: boolean = true;
  public _isLoading: boolean = false;
  private observers: Observer<string>[] = [];
  public nextPage: number = 0;



  constructor(
    private dataService: DataService,
    private filterService: FilterService
  ) {
    super();
  }


  setInit() {
    //this.refreshData();
  }

  setDestroy() {
  }

  onDatasetEvent(event: any) {
    if(this.active) {
      this.refreshData();
    }
  }

  onActivationEvent(state: any) {
    if(state == true) {
      this.refreshData();
    }
  }

  onDataTargetEvent(dt: DataTarget) {
    
  }

  get relatedObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : null;
  }

  get isLoading() : boolean {
    return this._isLoading;
  }

  getObservable() : Observable<string>  {
    return new Observable<string>((observer) => {
      this.observers.push(observer);
    });
  }

  public refreshData() {
    if(this.active && this._isLoading == false) {
      this.clear();
      this.fetchNextPage();  
    }
  }

  public clear() {
    this.aggregates = [];
    this.nextPage = 0;    
  }

  public mergeFilters() : any {
    let filter = {};
    if(this.baseFilter != null) {
      filter = this.filterService.mergeFilters(filter, this.baseFilter);
    }
    if(this.master != null && this.master.relationship != null && this.relatedObject != null) {
      filter = this.filterService.mergeFilters(filter, this.master.relationship);
    } 
    if(this.userFilter != null) {
      filter = this.filterService.mergeFilters(filter, this.userFilter);
    }
    return filter;
  }

  public fetchNextPage() {
    if(this.master == null || (this.master != null && this.master.relationship && this.relatedObject != null)) {
      const filter = this.filterService.resolveFilter(this.mergeFilters(), this.relatedObject, this.dataset, this.dataset?.relatedObject);
      this.dataService.aggregate(this.objectname, filter, null, this.tuple, this.metrics, this.base, this.nextPage).subscribe(
        data => this.setAggregates(data)
      );
      this._isLoading = true;
    }
  }

  public setAggregates(data: RbAggregate[]) {
    for(let agg of data) {
      this.aggregates.push(agg);
    }
    if(data.length == 50) {
      this.nextPage = this.nextPage + 1;
      this.fetchNextPage();
    } else {
      this._isLoading = false;
      this.publishEvent('load');
    }
  }

  public publishEvent(event: string) {
    this.observers.forEach((observer) => {
      observer.next(event);
    });     
  }
}
