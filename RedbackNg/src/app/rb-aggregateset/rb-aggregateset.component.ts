import { EventEmitter, Input, Output } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { DataService } from 'app/services/data.service';
import { RbAggregate, RbObject } from 'app/datamodel';
import { DataTarget } from 'app/desktop-root/desktop-root.component';
import { MapService } from 'app/services/map.service';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { Observer } from 'rxjs';

@Component({
  selector: 'rb-aggregateset',
  templateUrl: './rb-aggregateset.component.html',
  styleUrls: ['./rb-aggregateset.component.css']
})
export class RbAggregatesetComponent extends RbContainerComponent {
  @Input('object') objectname: string;
  @Input('master') master: any;
  @Input('baseFilter') baseFilter: any;
  @Input('tuple') tuple: any;
  @Input('metrics') metrics: any;
  @Input('datatarget') dataTarget: DataTarget;
  @Output('navigate') navigateEvent: EventEmitter<any> = new EventEmitter();

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

  public refreshData() {
    this.aggregates = [];
    if(this.master == null || (this.master != null && this.master.relationship && this.relatedObject != null)) {
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

    if(this.master.relationship != null && this.relatedObject != null) {
      filter = this.mapService.mergeMaps(filter, this.master.relationship);
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
