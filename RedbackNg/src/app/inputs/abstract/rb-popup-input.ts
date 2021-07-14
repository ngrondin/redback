import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { ComponentPortal, PortalInjector } from '@angular/cdk/portal';
import { Component } from '@angular/core';
import { ComponentRef, Injector, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { CONTAINER_DATA } from 'app/tokens';
import { RbFieldInputComponent } from '../abstract/rb-field-input';

@Component({template: ''})
export abstract class RbPopupInputComponent extends RbFieldInputComponent {
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


  public onFocus(event: any) {
    if(this.overlayRef == null) {
      this.startEditing();
      if(this.isEditing) {
        let position: any = this.getPositionOf(event.target);
        if(position.top > (window.innerHeight / 2)) {
          this.openPopup('up', position.top);
        } else {
          this.openPopup('down', (window.innerHeight - position.top - event.target.clientHeight - 40));
        }
        event.target.select();
      }
    }
  }
  
  public onBlur(event: any) {
    if(this.overlayRef != null) {
      this.inputContainerRef.element.nativeElement.focus();
    }
  }
  
  public onKeydown(event: any) {
    if(event.keyCode == 9) {
        this.finishEditing()
    } else if(event.keyCode == 13) {
        this.finishEditing();
    } else if(event.keyCode == 27) {
        this.cancelEditing();
    } else if(this.isEditing) {
        this.onKeyTyped(event.keyCode);
        if(this.popupComponentRef != null) {
          this.popupComponentRef.instance.keyTyped(event.keyCode);
        }
    }
  }
  
  public onKeyTyped(keyCode: number) {

  }

  public onPopupCancel() {
    this.cancelEditing();
  }  

  public onPopupValueSelected(value: any) {
    this.finishEditingWithSelection(value);
  }

  public startEditing() {
    super.startEditing();
  }

  public abstract getPopupClass() : any;

  public abstract getPopupConfig() : any;

  private openPopup(direction, maxHeight) {
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
      this.overlayRef.backdropClick().subscribe(() => this.onPopupCancel());

      const injectorTokens = new WeakMap();
      injectorTokens.set(OverlayRef, this.overlayRef);
      injectorTokens.set(CONTAINER_DATA, this.getPopupConfig());
      let inj : PortalInjector = new PortalInjector(this.injector, injectorTokens);

      const popupPortal = new ComponentPortal<RbPopupComponent>(this.getPopupClass(), this.viewContainerRef, inj);
      this.popupComponentRef = this.overlayRef.attach(popupPortal);
      this.popupComponentRef.instance.selected.subscribe(value => this.onPopupValueSelected(value));
    }
  }

  private closePopup() {
    if(this.overlayRef != null) {
      this.overlayRef.dispose();
      this.overlayRef = null;
    }
    this.popupComponentRef = null;
    this.inputContainerRef.element.nativeElement.blur();
  }

  public finishEditing() {
    let val = this.popupComponentRef != null ? this.popupComponentRef.instance.getHighlighted() : null;
    if(val != null) {
      this.finishEditingWithSelection(val);
    } else {
      this.cancelEditing();
    }
  }

  public finishEditingWithSelection(value: any) {
    this.closePopup();
    super.finishEditing();
  }

  public cancelEditing() {
      super.cancelEditing();
      this.closePopup();
  }

  private getPositionOf(element: any) : any {
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
