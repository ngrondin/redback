import { Component, OnInit, Input, ViewChild, ViewContainerRef, Injector, InjectionToken } from '@angular/core';
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
  @Input('size') size: Number;
  @Input('editable') editable: boolean;
  @Input('object') rbObject: RbObject;
  @Input('attribute') attribute: string;
  @Input('displayattribute') displayattribute: string;
  @Input('parentattribute') parentattribute: string;
  @Input('childattribute') childattribute: string;

  @ViewChild('input', { read: ViewContainerRef, static: false }) inputContainerRef: ViewContainerRef;
  
  editedValue: string;

  constructor(
    public injector: Injector,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef
  ) {
  }

  ngOnInit() {
  }

  public get displayvalue(): string {
    if(this.rbObject != null)
      return this.rbObject.related[this.attribute].data[this.displayattribute];
    else
      return null;  
  }

  public set displayvalue(val: string) {
    this.editedValue = val;
  }

  public get readonly(): boolean {
    if(this.rbObject != null)
      return !(this.editable && this.rbObject.validation[this.attribute].editable);
    else
      return true;      
  }

  focus() {
    if(!this.readonly) {
      const overlayRef: OverlayRef = this.overlay.create({
        positionStrategy: this.overlay.position().connectedTo(this.inputContainerRef.element, { originX: 'start', originY: 'bottom' }, { overlayX: 'start', overlayY: 'top' })
      });

      const injectionTokens = new WeakMap();
      //injectionTokens.set(FilePreviewOverlayRef, overlayRef);
      //injectionTokens.set(FILE_PREVIEW_DIALOG_DATA, {option: "allo"});
      //injector : PortalInjector = new PortalInjector(this.injector, injectionTokens);

      const filePreviewPortal = new ComponentPortal(RbPopupListComponent, this.viewContainerRef, inj);
      overlayRef.attach(filePreviewPortal);
    }
  }

  commit() {
    this.rbObject.setValue(this.attribute, this.editedValue);
  }
}
