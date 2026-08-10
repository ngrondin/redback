import { Component, Input, ViewContainerRef, ComponentRef, Injector, ViewChild } from '@angular/core';
import { OverlayRef, Overlay } from '@angular/cdk/overlay';
import { CONTAINER_DATA } from 'app/tokens';
import { PortalInjector, ComponentPortal } from '@angular/cdk/portal';
import { RbFilterBuilderComponent } from 'app/rb-filter-builder/rb-filter-builder.component';
import { RbFieldInputComponent } from 'app/inputs/abstract/rb-field-input';
import { RbSearchTarget } from './rb-search-target';
import { FilterBuilderConfig } from 'app/rb-filter-builder/rb-filter-builder-configs';
import { SearchMode } from './rb-search-model';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { RbPopupHardlistComponent } from 'app/popups/rb-popup-hardlist/rb-popup-hardlist.component';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { PopupService } from 'app/services/popup.service';

@Component({
  selector: 'rb-search',
  templateUrl: './rb-search.component.html',
  styleUrls: ['../inputs/abstract/rb-field-input.css']
})
export class RbSearchComponent extends RbFieldInputComponent {
  @Input('filter') filterconfig?: any;
  @Input('sort') sortconfig?: any;
  @Input('showsearchfield') showsearchfield: boolean = true;
  @Input('searchtarget') searchtarget?: RbSearchTarget;
  @Input('modes') _modes?: any;

  @ViewChild('modeselectorbutton', { read: ViewContainerRef }) modeSelectorButtonContainerRef: ViewContainerRef;

  overlayRef: OverlayRef;
  filterBuilderComponentRef: ComponentRef<RbFilterBuilderComponent>;
  modeSelectorPopupComponentRef: ComponentRef<RbPopupComponent>;

  public searchTimer: any = null;
  public modes: SearchMode[] = [];
  public mode: SearchMode | null;

  constructor(
    public injector: Injector,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef,
    public popupService: PopupService
  ) {
    super();
    this.label = "Search";
    this.defaultIcon = "search";
    this.defaultSize = 1;
    this.grow = 1;
    this.margin = false;
  }

  inputInit() {
    if (this._modes != null) {
      for (var item of this._modes) {
        this.modes.push(new SearchMode(item));
      }
    } else {
      let mode = new SearchMode({
        filter: this.filterconfig,
        sort: this.sortconfig,
        label: this.label,
        targetdatasetid: this.targetdatasetid
      });
      if (this.searchtarget != null) mode.searchtarget = this.searchtarget;
      this.modes.push(mode);
    }
    for (let mode of this.modes) {
      if (mode.searchtarget == null) mode.resolveSearchTarget(this.dataset, this.datasetgroup);
    }
    this.selectMode(this.modes[0]);
  }

  onDatasetEvent(event: any) {
  }

  onActivationEvent(state: boolean) {
    if(this.active == true && this.mode.searchtarget instanceof RbDatasetComponent) {
      this._value = this.mode.searchtarget.userSearch;
      this.mode.filterValue = this.mode.searchtarget.userFilter;
      this.mode.sortValue = this.mode.searchtarget.userSort;
    }
  }

  public setDisplayValue(val: any) {
    this.editedValue = val;
    this.searchAfterDelay(this.editedValue);
  }

  public get hasFilter() {
    return this.mode.filterValue != null || this.mode.sortValue != null;
  }

  searchAfterDelay(val: string) {
    if(this.searchTimer != null) clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => this.search(val), 500);
  }

  search(val: string) {
    let fetched = this.mode.searchtarget.filterSort({search: val});
    if(!fetched) {
      this.searchAfterDelay(val);
    }
  }

  finishEditing() {
    this.commit(this.editedValue);
    super.finishEditing();
  }

  openModeSelector() {
    let config = this.modes.map(m => ({ label: m.label, mode: m }));
    this.modeSelectorPopupComponentRef = this.popupService.openPopup(this.modeSelectorButtonContainerRef, RbPopupHardlistComponent, config);
    this.modeSelectorPopupComponentRef.instance.selected.subscribe(value => this.selectMode(value.mode));
    this.modeSelectorPopupComponentRef.instance.cancelled.subscribe(() => this.closeModeSelector());
  }

  closeModeSelector() {
    this.popupService.closePopup();
    this.modeSelectorPopupComponentRef = null;
  }

  selectMode(mode: SearchMode) {
    this.closeModeSelector();
    this.mode = mode;
    this.label = mode.label;
  }

  openFilterBuilder() {
    this.overlayRef = this.overlay.create({
      positionStrategy: this.overlay.position().global().centerHorizontally().centerVertically(),
      hasBackdrop: true,
    });
    this.overlayRef.backdropClick().subscribe(() => {
      this.cancelFilterBuilder();
    });

    let config: FilterBuilderConfig = new FilterBuilderConfig();
    config.filterConfig = this.mode.filterconfig;
    config.sortConfig = this.mode.sortconfig;
    config.searchTarget = this.mode.searchtarget;
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
    this.mode.filterValue = event.filter;
    this.mode.sortValue = event.sort;
    this.mode.searchtarget.filterSort({filter: this.mode.filterValue, sort: this.mode.sortValue});
  }

  cancelFilterBuilder() {
    this.overlayRef.dispose();
    this.overlayRef = null;
  }

}
