import { Component, OnInit, ComponentRef, Injector, ViewContainerRef, ViewChild } from '@angular/core';
import { RbInputCommonComponent } from 'app/rb-input-common/rb-input-common.component';
import { OverlayRef, Overlay } from '@angular/cdk/overlay';
import { RbPopupListComponent } from 'app/rb-popup-list/rb-popup-list.component';
import { CONTAINER_DATA } from 'app/tokens';
import { PortalInjector, ComponentPortal, ComponentType } from '@angular/cdk/portal';
import { RbPopupComponent } from 'app/rb-popup/rb-popup.component';

@Component({
  selector: 'rb-popup-input',
  templateUrl: './rb-popup-input.component.html',
  styleUrls: ['./rb-popup-input.component.css']
})
export abstract class RbPopupInputComponent extends RbInputCommonComponent implements OnInit {

  @ViewChild('input', { read: ViewContainerRef }) inputContainerRef: ViewContainerRef;

  overlayRef: OverlayRef;
  popupComponentRef: ComponentRef<RbPopupComponent>;

  constructor(
    public injector: Injector,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef
  ) {
    super();
  }

  ngOnInit(): void {
  }


  public openPopup(direction, maxHeight) {
    if(!this.readonly) {
      let positionStrategy: any = null;

      if(direction == 'up') {
        positionStrategy = this.overlay.position().connectedTo(this.inputContainerRef.element, { originX: 'start', originY: 'top' }, { overlayX: 'start', overlayY: 'bottom' })
      } else {
        positionStrategy = this.overlay.position().connectedTo(this.inputContainerRef.element, { originX: 'start', originY: 'bottom' }, { overlayX: 'start', overlayY: 'top' })
      }

      this.overlayRef = this.overlay.create({
        positionStrategy: positionStrategy,
        hasBackdrop: true,
        backdropClass: 'cdk-overlay-transparent-backdrop',
        maxHeight: maxHeight
      });
      this.overlayRef.backdropClick().subscribe(() => this._cancel());

      const injectorTokens = new WeakMap();
      injectorTokens.set(OverlayRef, this.overlayRef);
      injectorTokens.set(CONTAINER_DATA, this.getPopupConfig());
      let inj : PortalInjector = new PortalInjector(this.injector, injectorTokens);

      const popupPortal = new ComponentPortal<RbPopupComponent>(this.getPopupClass(), this.viewContainerRef, inj);
      this.popupComponentRef = this.overlayRef.attach(popupPortal);
      this.popupComponentRef.instance.selected.subscribe(value => this._selected(value));
    }
  }

  public closePopup() {
    this.overlayRef.dispose();
    this.overlayRef = null;
    this.popupComponentRef = null;
    this.inputContainerRef.element.nativeElement.blur();
  }

  public _cancel() {
    this.closePopup();
    this.cancelEditing();
  }  

  public _selected(value: any) {
    this.closePopup();
    this.finishEditingWithSelection(value);
  }

  public _finish() {
    this.closePopup();
    this.finishEditing();
  }

  public abstract getPopupClass() : any;

  public abstract getPopupConfig() : any;

  public abstract addPopupSubscription(instance: RbPopupComponent);

  public abstract startEditing();

  public abstract finishEditing();

  public abstract finishEditingWithSelection(value: any);

  public abstract cancelEditing();

  public abstract erase();

  public focus(event: any) {
    this.startEditing();
    if(this.overlayRef == null) {
      let position: any = this.getPositionOf(event.target);
      if(position.top > (window.innerHeight / 2)) {
        this.openPopup('up', position.top);
      } else {
        this.openPopup('down', (window.innerHeight - position.top - event.target.clientHeight - 40));
      }
    }
  }

  public blur(event: any) {
    if(this.overlayRef != null) {
      this.inputContainerRef.element.nativeElement.focus();
    }
  }

  public keydown(event: any) {
    if(event.keyCode == 13) {
      this._finish();
    } else if(event.keyCode == 9) {
      this._finish();
    } else if(event.keyCode == 27) {
      this._cancel();
    } else if(event.keyCode == 8 || event.keyCode == 127) {
      this.erase();
    }
  }


  
  public getPositionOf(element: any) : any {
    if(element.offsetParent != null) {
      let position: any = this.getPositionOf(element.offsetParent);
      position.top = position.top + element.offsetTop;
      position.left = position.left + element.offsetLeft;
      return position;
    } else {
      return { top : 0, left: 0};
    }
  }
}
