import { Component, OnInit, Input, ComponentRef, Injector, ViewContainerRef, ViewChild } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { OverlayRef, Overlay } from '@angular/cdk/overlay';
import { RbPopupListComponent } from 'app/rb-popup-list/rb-popup-list.component';
import { CONTAINER_DATA } from 'app/tokens';
import { PortalInjector, ComponentPortal } from '@angular/cdk/portal';
import { DateTimePopupConfig, RbPopupDatetimeComponent } from 'app/rb-popup-datetime/rb-popup-datetime.component';

@Component({
  selector: 'rb-datetime-input',
  templateUrl: './rb-datetime-input.component.html',
  styleUrls: ['./rb-datetime-input.component.css']
})
export class RbDatetimeInputComponent implements OnInit {
  @Input('label') label: string;
  @Input('icon') icon: string;
  @Input('size') size: Number;
  @Input('editable') editable: boolean;
  @Input('object') rbObject: RbObject;
  @Input('attribute') attribute: string;
  @Input('format') format: string;

  @ViewChild('input', { read: ViewContainerRef, static: false }) inputContainerRef: ViewContainerRef;

  overlayRef: OverlayRef;
  popupDatetimeComponentRef: ComponentRef<RbPopupDatetimeComponent>;

  constructor(
    public injector: Injector,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef
  ) { }

  ngOnInit() {
    if(this.format == null)
      this.format = 'YYYY-MM-DD HH:mm';
  }

  public get displayvalue(): string {
    if(this.rbObject != null) {
      let iso : string = this.rbObject.data[this.attribute];
      let dt: Date = new Date(iso);
      let val = this.format;
      val = val.replace('YYYY', dt.getFullYear().toString());
      val = val.replace('YY', (dt.getFullYear() % 100).toString());
      val = val.replace('MM', this.convertToStringAndPad(dt.getMonth() + 1, 2));
      val = val.replace('DD', this.convertToStringAndPad(dt.getDate(), 2));
      val = val.replace('HH', this.convertToStringAndPad(dt.getHours(), 2));
      val = val.replace('mm', this.convertToStringAndPad(dt.getMinutes(), 2));
      return val;
    } else
      return null;  
  }

  private convertToStringAndPad(num: number, n: number) : string {
    let ret :string = num.toString();
    while(ret.length < n) 
      ret = '0' + ret;
    return ret;
  }

  public get readonly(): boolean {
    if(this.rbObject != null)
      return !(this.editable && this.rbObject.validation[this.attribute].editable);
    else
      return true;      
  }

  public openPopupList() {
    if(!this.readonly) {
      this.overlayRef = this.overlay.create({
        positionStrategy: this.overlay.position().connectedTo(this.inputContainerRef.element, { originX: 'start', originY: 'bottom' }, { overlayX: 'start', overlayY: 'top' }),
        hasBackdrop: true,
        backdropClass: 'cdk-overlay-transparent-backdrop'
      });
      this.overlayRef.backdropClick().subscribe(() => {
        this.cancelEditing();
      });

      let config: DateTimePopupConfig = new DateTimePopupConfig();
      config.initialDate = this.rbObject != null && this.rbObject.data[this.attribute] != null ? new Date(this.rbObject.data[this.attribute]) : new Date();
      config.datePart = true;
      config.hourPart = true;
      config.minutePart = true;
      const injectorTokens = new WeakMap();
      injectorTokens.set(OverlayRef, this.overlayRef);
      injectorTokens.set(CONTAINER_DATA, config);
      let inj : PortalInjector = new PortalInjector(this.injector, injectorTokens);

      const popupListPortal = new ComponentPortal(RbPopupDatetimeComponent, this.viewContainerRef, inj);
      this.popupDatetimeComponentRef = this.overlayRef.attach(popupListPortal);
      this.popupDatetimeComponentRef.instance.selected.subscribe(object => this.selected(object));
    }
  }

  public focus(event: any) {
    if(this.overlayRef == null)
      this.openPopupList();
  }

  public blur(event: any) {
    if(this.overlayRef != null) 
      this.inputContainerRef.element.nativeElement.focus();
  }

  public keydown(event: any) {
    if(event.keyCode == 13) {
      this.selected(null);
    } else if(event.keyCode == 9 || event.keyCode == 27) {
      this.cancelEditing();
    }
  }

  public cancelEditing() {
    this.overlayRef.dispose();
    this.overlayRef = null;
    this.inputContainerRef.element.nativeElement.blur();
  }

  public selected(dt: Date) {
    if(dt != null)
      this.rbObject.setValue(this.attribute, dt.toISOString());
    else
      this.rbObject.setValue(this.attribute, null);
    this.overlayRef.dispose();
    this.overlayRef = null;
    this.inputContainerRef.element.nativeElement.blur();
  }

}