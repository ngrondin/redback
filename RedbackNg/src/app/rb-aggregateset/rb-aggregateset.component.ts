import { EventEmitter, Input, Output } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { DataService } from 'app/services/data.service';
import { DataTarget, RbAggregate, RbObject } from 'app/datamodel';
import { MapService } from 'app/services/map.service';
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


  constructor(
    private dataService: DataService,
    private mapService: MapService
  ) {
    super();
  }


  containerInit() {
    this.initiated = true;
    this.refreshData();
  }

  containerDestroy() {
  }

  onDatasetEvent(event: any) {
  }

  onActivationEvent(event: any) {

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
    if(this.master == null || (this.master != null && this.master.relationship && this.relatedObject != null)) {
      const filter = this.mapService.resolveMap(this.mergeFilters(), this.relatedObject, null, this.relatedObject);
      this.dataService.aggregateObjects(this.objectname, filter, null, this.tuple, this.metrics).subscribe(
        data => this.setAggregates(data)
      );
      this.isLoading = true;
    }
  }

  public mergeFilters() : any {
    let filter = {};
    if(this.baseFilter != null) {
      filter = this.mapService.mergeMaps(filter, this.baseFilter);
    }

    if(this.master != null && this.master.relationship != null && this.relatedObject != null) {
      filter = this.mapService.mergeMaps(filter, this.master.relationship);
    } 

    if(this.userFilter != null) {
      filter = this.mapService.mergeMaps(filter, this.userFilter);
    }

    return filter;
  }

  public setAggregates(data: RbAggregate[]) {
    this.aggregates = data;
    this.publishEvent('loaded');
  }

/*
  public selectDimensions(dimensionsFilter: any) {

  }
*/

  public publishEvent(event: string) {
    this.observers.forEach((observer) => {
      observer.next(event);
    });     
  }
}
