import { HostListener } from '@angular/core';
import { Component, Input, OnInit } from '@angular/core';
import { RbContainerComponent } from 'app/abstract/rb-container';

@Component({
  selector: 'rb-tile',
  templateUrl: './rb-tile.component.html',
  styleUrls: ['./rb-tile.component.css']
})
export class RbTileComponent extends RbContainerComponent {
  @Input('title') title: string;
  @Input('showreload') showreload: boolean = false;
  @HostListener('mouseenter', ['$event']) onMouseEnter($event) { this.hovering = true; }
  @HostListener('mouseleave', ['$event']) onMouseLeave($event) { this.hovering = false; }

  hovering: boolean = false;

  constructor() {
    super();
  }

  containerInit() {
  }

  containerDestroy() {
  }

  onDatasetEvent(event: any) {
  }

  onActivationEvent(state: boolean) {
  }

  refresh() {
    if(this.dataset != null) this.dataset.refreshData();
    if(this.datasetgroup != null) this.datasetgroup.refreshAllData();
    if(this.aggregateset != null) this.aggregateset.refreshData();
  }

}
