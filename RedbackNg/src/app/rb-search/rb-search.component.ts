import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ViewContainerRef, ComponentRef, Injector } from '@angular/core';
import { OverlayRef, Overlay } from '@angular/cdk/overlay';
import { RbPopupDatetimeComponent, DateTimePopupConfig } from 'app/rb-popup-datetime/rb-popup-datetime.component';
import { CONTAINER_DATA } from 'app/tokens';
import { PortalInjector, ComponentPortal } from '@angular/cdk/portal';
import { RbFilterBuilderComponent, FilterBuilderConfig } from 'app/rb-filter-builder/rb-filter-builder.component';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'rb-search',
  templateUrl: './rb-search.component.html',
  styleUrls: ['./rb-search.component.css']
})
export class RbSearchComponent implements OnInit {

  @Input('icon') icon: string;
  @Input('size') size: Number;
  @Input('filterconfig') filterConfig: any;
  @Output() search: EventEmitter<any> = new EventEmitter();
  @Output() filter: EventEmitter<any> = new EventEmitter();

  overlayRef: OverlayRef;
  filterBuilderComponentRef: ComponentRef<RbFilterBuilderComponent>;

  public value;
  private previousValue;
  public filterValue: any;

  constructor(    
    public injector: Injector,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef
) { }

  ngOnInit() {
  }

  keyup(event: any) {
    if(this.value !== this.previousValue) {
      let currentValue = this.value;
      setTimeout(()=> {
        if(this.value == currentValue)
          this.search.emit(this.value);
      }, 500);
      this.previousValue = this.value;
    }
  }

  setFilter(filter: any) {
    if(this.overlay != null) {
      this.overlayRef.dispose();
      this.overlayRef = null;
    }
    this.filterValue = filter;
    this.filter.emit(filter);
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
    config.filterConfig = this.filterConfig;
    config.initialFilter = this.filterValue;
    const injectorTokens = new WeakMap();
    injectorTokens.set(OverlayRef, this.overlayRef);
    injectorTokens.set(CONTAINER_DATA, config);
    let inj : PortalInjector = new PortalInjector(this.injector, injectorTokens);

    const popupPortal = new ComponentPortal(RbFilterBuilderComponent, this.viewContainerRef, inj);
    this.filterBuilderComponentRef = this.overlayRef.attach(popupPortal);
    this.filterBuilderComponentRef.instance.done.subscribe(filter => this.setFilter(filter));
  }

  cancelFilterBuilder() {
    this.overlayRef.dispose();
    this.overlayRef = null;
  }
}
