import { Component, OnInit, Input, ViewChild, ViewContainerRef, Injector, InjectionToken, ComponentRef } from '@angular/core';
import { OverlayRef, CdkOverlayOrigin, Overlay, OverlayConfig } from  '@angular/cdk/overlay';
import { RbInputComponent } from '../rb-input/rb-input.component';
import { RbObject } from '../datamodel';
import { MatDialog } from '@angular/material/dialog';
import { RbPopupListComponent } from '../rb-popup-list/rb-popup-list.component';
import { ComponentPortal, PortalInjector } from '@angular/cdk/portal';
import { CONTAINER_DATA } from '../tokens';

@Component({
  selector: 'rb-related-input',
  templateUrl: './rb-related-input.component.html',
  styleUrls: ['./rb-related-input.component.css']
})
export class RbRelatedInputComponent implements OnInit {

  @Input('label') label: string;
  @Input('icon') icon: string;
  @Input('size') size: number;
  @Input('editable') editable: boolean;
  @Input('object') rbObject: RbObject;
  @Input('attribute') attribute: string;
  @Input('displayattribute') displayattribute: string;
  @Input('parentattribute') parentattribute: string;
  @Input('childattribute') childattribute: string;

  @ViewChild('input', { read: ViewContainerRef }) inputContainerRef: ViewContainerRef;
  overlayRef: OverlayRef;
  popupListComponentRef: ComponentRef<RbPopupListComponent>;
  searchValue: string; 
  highlightedObject: RbObject;

  constructor(
    public injector: Injector,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef
  ) {
  }

  ngOnInit() {
  }

  public get displayvalue(): string {
    if(this.overlayRef != null)
      return this.searchValue;
    if(this.rbObject != null && this.rbObject.related[this.attribute] != null )
      return this.rbObject.related[this.attribute].get(this.displayattribute);
    else
      return null;  
  }

  public set displayvalue(str: string) {
    this.searchValue = str;
    if(this.popupListComponentRef != null) {
      let currentValue = this.searchValue;
      setTimeout(()=> {
        if(this.searchValue == currentValue)
        this.popupListComponentRef.instance.setSearch(this.searchValue);
      }, 500);     
    }
  }

  public get readonly(): boolean {
    if(this.rbObject != null && this.rbObject.validation[this.attribute] != null)
      return !(this.editable && this.rbObject.validation[this.attribute].editable);
    else
      return true;      
  }

  public openPopupList(direction) {
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
        backdropClass: 'cdk-overlay-transparent-backdrop'
      });
      this.overlayRef.backdropClick().subscribe(() => {
        this.cancelEditing();
      });

      const injectorTokens = new WeakMap();
      injectorTokens.set(OverlayRef, this.overlayRef);
      injectorTokens.set(CONTAINER_DATA, {
        rbObject: this.rbObject, 
        attribute: this.attribute, 
        displayattribute: this.displayattribute, 
        parentattribute: this.parentattribute, 
        childattribute: this.childattribute
      });
      let inj : PortalInjector = new PortalInjector(this.injector, injectorTokens);

      const popupListPortal = new ComponentPortal(RbPopupListComponent, this.viewContainerRef, inj);
      this.popupListComponentRef = this.overlayRef.attach(popupListPortal);
      this.popupListComponentRef.instance.selected.subscribe(object => this.selected(object));
    }
  }

  public focus(event: any) {
    if(this.overlayRef == null) {
      let position: any = this.getPositionOf(event.target);
      if(position.top > (window.innerHeight / 2)) {
        this.openPopupList('up');
      } else {
        this.openPopupList('down');
      }
    }
  }

  public blur(event: any) {
    if(this.overlayRef != null) 
      this.inputContainerRef.element.nativeElement.focus();
  }

  public keydown(event: any) {
    if(event.keyCode == 13) {
      this.selected(this.highlightedObject);
    } else if(event.keyCode == 9 || event.keyCode == 27) {
      this.cancelEditing();
    } else if(event.keyCode == 8 || event.keyCode == 127) {
      this.erase();
    }
  }

  public cancelEditing() {
    this.overlayRef.dispose();
    this.overlayRef = null;
    this.popupListComponentRef = null;
    this.searchValue = '';
    this.inputContainerRef.element.nativeElement.blur();
  }

  public selected(object: RbObject) {
    let link = this.rbObject.validation[this.attribute].related.link;
    let val = (link == 'uid') ? object.uid : object.data[link];
    this.rbObject.setValueAndRelated(this.attribute, val, object);
    this.cancelEditing();
  }

  public erase() {
    this.rbObject.setValueAndRelated(this.attribute, null, null);
    this.cancelEditing();
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
