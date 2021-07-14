import { Component, Input, ViewContainerRef, ComponentRef, Injector, HostBinding } from '@angular/core';
import { OverlayRef, Overlay } from '@angular/cdk/overlay';
import { CONTAINER_DATA } from 'app/tokens';
import { PortalInjector, ComponentPortal } from '@angular/cdk/portal';
import { RbFilterBuilderComponent, FilterBuilderConfig } from 'app/rb-filter-builder/rb-filter-builder.component';
import { RbFieldInputComponent } from 'app/inputs/abstract/rb-field-input';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';

@Component({
  selector: 'rb-search',
  templateUrl: './rb-search.component.html',
  styleUrls: ['../inputs/abstract/rb-field-input.css']
})
export class RbSearchComponent extends RbFieldInputComponent {
  @Input('filter') filterconfig: any;
  @Input('sort') sortconfig: any;
  
  overlayRef: OverlayRef;
  filterBuilderComponentRef: ComponentRef<RbFilterBuilderComponent>;

  public filterValue: any;
  public sortValue: any;
  public queuedSearch: string = null;

  constructor(    
    public injector: Injector,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef
  ) {
    super();
    this.label = "Search";
    this.defaultIcon = "search";
    this.defaultSize = 1;
    this.grow = 1;
    this.margin = false;
  }

  dataObserverInit() {
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: string) {
    if(event == 'reset') {
      this._value = null;
      this.filterValue = null;
      this.sortValue = null;
    }
  }

  onActivationEvent(state: boolean) {
  }

  public get displayvalue(): any {
    if(this.isEditing) {
      return this.editedValue;
    } else {
      return this.value;
    }
    
  }

  public set displayvalue(val: any) {
    this.editedValue = val;
    if(this.editedValue !== this.queuedSearch) {
      let currentValue = this.editedValue;
      setTimeout(()=> {
        if(this.editedValue == currentValue) {
          this.search();
        }
      }, 500);
      this.queuedSearch = this.editedValue;
    }
  }

  search() {
    if(this.dataset != null) {
      this.dataset.search(this.editedValue);
    } else if(this.datasetgroup != null) {
      for(let dsname of Object.keys(this.datasetgroup.datasets)) {
        let ds: RbDatasetComponent = this.datasetgroup.datasets[dsname];
        ds.search(this.editedValue);
      }
      this.datasetgroup
    }
    this.queuedSearch = null;
  }

  onFocus(event: any) {
    super.onFocus(event);
    if(this.isEditing) event.target.select();
  }

  startEditing() {
    super.startEditing();
    this.editedValue = this.value;
  }

  finishEditing() {
    if(this.queuedSearch != null) {
      this.search();
    }
    this.commit(this.editedValue);
    super.finishEditing();
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

}
