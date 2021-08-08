import { Component, Input, ViewContainerRef, ComponentRef, Injector, HostBinding } from '@angular/core';
import { OverlayRef, Overlay } from '@angular/cdk/overlay';
import { CONTAINER_DATA } from 'app/tokens';
import { PortalInjector, ComponentPortal } from '@angular/cdk/portal';
import { RbFilterBuilderComponent, FilterBuilderConfig } from 'app/rb-filter-builder/rb-filter-builder.component';
import { RbFieldInputComponent } from 'app/inputs/abstract/rb-field-input';
import { RbSearchTarget } from './rb-search-target';

@Component({
  selector: 'rb-search',
  templateUrl: './rb-search.component.html',
  styleUrls: ['../inputs/abstract/rb-field-input.css']
})
export class RbSearchComponent extends RbFieldInputComponent {
  @Input('filter') filterconfig: any;
  @Input('sort') sortconfig: any;
  @Input('target') searchTarget: RbSearchTarget;
  
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

  inputInit() {
    if(this.searchTarget == null) {
      if(this.dataset != null) {
        this.searchTarget = this.dataset;
      } else if(this.datasetgroup != null) {
        this.searchTarget = this.datasetgroup;
      }
    }
  }

  onDatasetEvent(event: string) {

  }

  onActivationEvent(state: boolean) {
    if(this.active == true && this.dataset != null) {
      this._value = this.dataset.searchString;
      this.filterValue = this.dataset.userFilter;
      this.sortValue = this.dataset.userSort;    
    }
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
      this.searchAfterDelay();
      this.queuedSearch = this.editedValue;
    }
  }

  public get hasFilter() {
    return this.filterValue != null || this.sortValue != null;
  }

  searchAfterDelay() {
    let searchValue = this.editedValue;
    setTimeout(()=> {
      if(this.editedValue == searchValue) {
        this.search();
      }
    }, 500);    
  }

  search() {
    let fetched = this.searchTarget.filterSort({search: this.editedValue});
    if(fetched == true) {
      this.queuedSearch = null;
    } else {
      console.error("Search blocked");
      this.searchAfterDelay();
    }
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
    config.objectname = this.searchTarget.objectname;
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
    this.searchTarget.filterSort({filter: this.filterValue, sort: this.sortValue});
  }

  cancelFilterBuilder() {
    this.overlayRef.dispose();
    this.overlayRef = null;
  }

}
