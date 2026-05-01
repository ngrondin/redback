import { Component, ViewChild, ViewContainerRef, ComponentRef, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange, TypeDecorator } from '@angular/core';
import { __asyncDelegator } from 'tslib';
import { NavigateService } from 'app/services/navigate.service';
import { LoadedView } from 'app/loader';


@Component({
  selector: 'rb-view-loader',
  templateUrl: './rb-view-loader.component.html',
  styleUrls: ['./rb-view-loader.component.css']
})
export class RbViewLoaderComponent implements OnInit {
  @Input('name') name = 'default';
  @ViewChild('container', { read: ViewContainerRef, static: true }) container?: ViewContainerRef;

  public currentLoadedView: LoadedView | null = null;

  constructor(
    private navigateService: NavigateService,
  ) { 
  }

  ngOnInit() {
    this.navigateService.registerTarget(this.name, this);
  }

  detachCurrentLoadedView() {
    if(this.currentLoadedView != null && this.container != null) {
      this.currentLoadedView.detachFrom(this.container);
      this.currentLoadedView = null;
    } 
  }

  attachNewLoadedView(entry: LoadedView) {
    if(entry != null && this.container != null) {
      this.currentLoadedView = entry;
      entry.attachTo(this.container);
    }
  }
  
}
