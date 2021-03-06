import { Component, OnInit, ComponentRef, Injector, ViewContainerRef, ViewChild } from '@angular/core';
import { RbInputCommonComponent } from 'app/inputs/rb-input-common/rb-input-common.component';
import { OverlayRef, Overlay } from '@angular/cdk/overlay';
import { CONTAINER_DATA } from 'app/tokens';
import { PortalInjector, ComponentPortal, ComponentType } from '@angular/cdk/portal';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';

@Component({
  selector: 'rb-popup-input',
  templateUrl: './rb-popup-input.component.html',
  styleUrls: ['./rb-popup-input.component.css']
})
export abstract class RbPopupInputComponent extends RbInputCommonComponent {

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
    if(this.overlayRef != null) {
      this.overlayRef.dispose();
      this.overlayRef = null;
    }
    this.popupComponentRef = null;
    this.inputContainerRef.element.nativeElement.blur();
  }

  public getHighlighted() : any {
    return this.popupComponentRef != null ? this.popupComponentRef.instance.getHighlighted() : null;
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
    let value = this.popupComponentRef != null ? this.popupComponentRef.instance.getHighlighted() : null;
    this.closePopup();
    if(value != null) {
      this.finishEditingWithSelection(value);
    } else {
      this.finishEditing();
    }
  }

  public abstract getPopupClass() : any;

  public abstract getPopupConfig() : any;

  public abstract addPopupSubscription(instance: RbPopupComponent);

  public abstract startEditing();

  public abstract keyTyped(keyCode: number);

  public abstract finishEditing();

  public abstract finishEditingWithSelection(value: any);

  public abstract cancelEditing();

  public focus(event: any) {
    super.focus(event);
    if(this.overlayRef == null) {
      this.startEditing();
      let position: any = this.getPositionOf(event.target);
      if(position.top > (window.innerHeight / 2)) {
        this.openPopup('up', position.top);
      } else {
        this.openPopup('down', (window.innerHeight - position.top - event.target.clientHeight - 40));
      }
      event.target.select();
    }
  }

  public blur(event: any) {
    super.blur(event);
    if(this.overlayRef != null) {
      this.inputContainerRef.element.nativeElement.focus();
    }
  }

  public keydown(event: any) {
    super.keydown(event);
    if(event.keyCode == 9) {
      this._finish();
    } else if(event.keyCode == 27) {
      this._cancel();
    } else {
      this.keyTyped(event.keyCode);
      if(this.popupComponentRef != null) {
        this.popupComponentRef.instance.keyTyped(event.keyCode);
      }
    }    
  }

  public commit() {
    super.commit();
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

  public get textIsTemporary(): boolean {
    return this.overlayRef != null;
  }

}
