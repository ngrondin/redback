import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { ComponentPortal, PortalInjector } from '@angular/cdk/portal';
import { Injectable, Injector, ViewContainerRef } from '@angular/core';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { CONTAINER_DATA } from 'app/tokens';

@Injectable({
  providedIn: 'root'
})
export class PopupService {
  overlayRef: OverlayRef;
  
  constructor(
    public injector: Injector,
    public overlay: Overlay
  ) { }


  public openPopup(anchorElementRef: ViewContainerRef, popupClass, configData) {
    if(this.overlay != null) {
      this.closePopup();
    }

    let position: any = this.getPositionOf(anchorElementRef.element.nativeElement);
    let direction = 'down';
    let maxHeight = 200;
    let positionData = null;
    if(position.top > (window.innerHeight / 2)) {
      direction = 'up';
      maxHeight = position.top;
      positionData = [{ originX: 'start', originY: 'top', overlayX: 'start', overlayY: 'bottom' }];
    } else {
      maxHeight = (window.innerHeight - position.top - anchorElementRef.element.nativeElement.clientHeight - 40);
      positionData = [{ originX: 'start', originY: 'bottom', overlayX: 'start', overlayY: 'top' }];
    }

    let positionStrategy: any = this.overlay.position().flexibleConnectedTo(anchorElementRef.element).withPositions(positionData);
    this.overlayRef = this.overlay.create({
      positionStrategy: positionStrategy,
      hasBackdrop: true,
      backdropClass: 'cdk-overlay-transparent-backdrop',
      maxHeight: maxHeight
    });
    
    const injectorTokens = new WeakMap();
    injectorTokens.set(OverlayRef, this.overlayRef);
    injectorTokens.set(CONTAINER_DATA, configData);
    let inj : PortalInjector = new PortalInjector(this.injector, injectorTokens);

    const popupPortal = new ComponentPortal<RbPopupComponent>(popupClass, anchorElementRef, inj);
    const popupComponentRef = this.overlayRef.attach(popupPortal);
    this.overlayRef.backdropClick().subscribe(() => popupComponentRef.instance.onOverlayClick());
    return popupComponentRef;
  }

  public closePopup() {
    if(this.overlayRef != null) {
      this.overlayRef.dispose();
      this.overlayRef = null;
    }
  }

  private getPositionOf(nativeElement: any) : any {
    if(nativeElement.offsetParent != null) {
      let position: any = this.getPositionOf(nativeElement.offsetParent);
      position.top = position.top + nativeElement.offsetTop;
      position.left = position.left + nativeElement.offsetLeft;
      return position;
    } else {
      return { top : 0, left: 0};
    }
  }

}
