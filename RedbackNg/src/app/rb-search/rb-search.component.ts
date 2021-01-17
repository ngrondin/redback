import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ViewContainerRef, ComponentRef, Injector } from '@angular/core';
import { OverlayRef, Overlay } from '@angular/cdk/overlay';
import { CONTAINER_DATA } from 'app/tokens';
import { PortalInjector, ComponentPortal } from '@angular/cdk/portal';
import { RbFilterBuilderComponent, FilterBuilderConfig } from 'app/rb-filter-builder/rb-filter-builder.component';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';

@Component({
  selector: 'rb-search',
  templateUrl: './rb-search.component.html',
  styleUrls: ['./rb-search.component.css']
})
export class RbSearchComponent extends RbDataObserverComponent {
  @Input('dataset') dataset: RbDatasetComponent;
  @Input('icon') icon: string;
  @Input('size') size: number;
  @Input('filter') filterconfig: any;
  @Input('sort') sortconfig: any;

  overlayRef: OverlayRef;
  filterBuilderComponentRef: ComponentRef<RbFilterBuilderComponent>;

  public searchValue;
  private previousValue;
  public filterValue: any;
  public sortValue: any;

  constructor(    
    public injector: Injector,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef
  ) {
    super();
  }

  dataObserverInit() {
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: string) {
    if(event == 'reset') {
      this.searchValue = null;
      this.filterValue = null;
      this.sortValue = null;
    }
  }

  onActivationEvent(state: boolean) {
  }

  keyup(event: any) {
    if(this.searchValue !== this.previousValue) {
      let currentValue = this.searchValue;
      setTimeout(()=> {
        if(this.searchValue == currentValue)
          this.dataset.search(this.searchValue);
      }, 500);
      this.previousValue = this.searchValue;
    }
  }

  openFilterBuilder() {
    this.overlayRef = this.overlay.create({
      positionStrategy: this.overlay.position().global().centerHorizontally().centerVertically(),
      hasBackdrop: true,
      backdropClass: 'cdk-overlay-transparent-backdrop'
    });
    this.overlayRef.backdropClick().subscribe(() => {
      this.cancelFilterBuilder();
    });

    let config: FilterBuilderConfig = new FilterBuilderConfig();
    config.filterConfig = this.filterconfig;
    config.initialFilter = this.filterValue;
    config.sortConfig = this.sortconfig;
    config.initialSort = this.sortValue;
    config.objectname = this.dataset.object;
    const injectorTokens = new WeakMap();
    injectorTokens.set(OverlayRef, this.overlayRef);
    injectorTokens.set(CONTAINER_DATA, config);
    let inj : PortalInjector = new PortalInjector(this.injector, injectorTokens);

    const popupPortal = new ComponentPortal(RbFilterBuilderComponent, this.viewContainerRef, inj);
    this.filterBuilderComponentRef = this.overlayRef.attach(popupPortal);
    this.filterBuilderComponentRef.instance.done.subscribe(event => {
      this.closeFilterBuilder(event);
    });
  }

  closeFilterBuilder(event: any) {
    this.overlayRef.dispose();
    this.overlayRef = null;
    this.filterValue = event.filter;
    this.sortValue = event.sort;
    this.dataset.filterSort({filter: this.filterValue, sort: this.sortValue});
  }

  cancelFilterBuilder() {
    this.overlayRef.dispose();
    this.overlayRef = null;
  }


  public get widthString() : string {
    if(this.size != null)
      return '' + (15 * this.size) + 'px';
    else
      return '100%';
  }
}
