import { EventEmitter, Input, Output } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { DataService } from 'app/services/data.service';
import { DataTarget, RbAggregate, RbObject } from 'app/datamodel';
import { FilterService } from 'app/services/filter.service';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { Observable, Observer } from 'rxjs';

@Component({
  selector: 'rb-aggregateset',
  templateUrl: './rb-aggregateset.component.html',
  styleUrls: ['./rb-aggregateset.component.css']
})
export class RbAggregatesetComponent extends RbContainerComponent {
  @Input('object') objectname: string;
  @Input('master') master: any;
  @Input('basefilter') baseFilter: any;
  @Input('tuple') tuple: any;
  @Input('metrics') metrics: any;
  @Input('datatarget') dataTarget: DataTarget;

  public aggregates: RbAggregate[] = [];
  public searchString: string;
  public userFilter: any;
  public isLoading: boolean;
  public initiated: boolean = false;
  public firstLoad: boolean = true;
  private observers: Observer<string>[] = [];
  public active: boolean;
  public nextPage: number = 0;



  constructor(
    private dataService: DataService,
    private filterService: FilterService
  ) {
    super();
  }


  containerInit() {
    if(this.active == true) {
      this.refreshData();
    }
  }

  containerDestroy() {
  }

  onDatasetEvent(event: any) {
  }

  onActivationEvent(state: any) {
    if(state == true && this.initiated == false) {
      this.refreshData();
    }
  }

  get relatedObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : null;
  }

  getObservable() : Observable<string>  {
    return new Observable<string>((observer) => {
      this.observers.push(observer);
    });
  }

  public refreshData() {
    this.aggregates = [];
    this.nextPage = 0;
    this.fetchNextPage();
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
      const filter = this.filterService.resolveFilter(this.mergeFilters(), this.relatedObject, null, this.relatedObject);
      this.dataService.aggregateObjects(this.objectname, filter, null, this.tuple, this.metrics, this.nextPage).subscribe(
        data => this.setAggregates(data)
      );
      this.isLoading = true;
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
      this.initiated = true;
      this.isLoading = false;
      this.publishEvent('loaded');
    }
  }

  public publishEvent(event: string) {
    this.observers.forEach((observer) => {
      observer.next(event);
    });     
  }
}
