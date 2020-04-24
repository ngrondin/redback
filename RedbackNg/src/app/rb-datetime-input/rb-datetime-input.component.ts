import { Component, OnInit, Input, ComponentRef, Injector, ViewContainerRef, ViewChild, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { OverlayRef, Overlay } from '@angular/cdk/overlay';
import { RbPopupListComponent } from 'app/rb-popup-list/rb-popup-list.component';
import { CONTAINER_DATA } from 'app/tokens';
import { PortalInjector, ComponentPortal } from '@angular/cdk/portal';
import { DateTimePopupConfig, RbPopupDatetimeComponent } from 'app/rb-popup-datetime/rb-popup-datetime.component';
import { RbInputCommonComponent } from 'app/rb-input-common/rb-input-common.component';

@Component({
  selector: 'rb-datetime-input',
  templateUrl: './rb-datetime-input.component.html',
  styleUrls: ['./rb-datetime-input.component.css']
})
export class RbDatetimeInputComponent extends RbInputCommonComponent implements OnInit {
  @Input('format') format: string;

  @ViewChild('input', { read: ViewContainerRef, static: false }) inputContainerRef: ViewContainerRef;

  overlayRef: OverlayRef;
  popupDatetimeComponentRef: ComponentRef<RbPopupDatetimeComponent>;

  constructor(
    public injector: Injector,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef
  ) {
    super();
   }

  ngOnInit() {
    if(this.format == null)
      this.format = 'YYYY-MM-DD HH:mm';
  }

  public get displayvalue(): string {
    if(this.attribute != null) {
      if(this.rbObject != null) {
        let iso : string = this.rbObject.get(this.attribute);
        if(iso != null) {
          return this.formatDate(new Date(iso));
        } else {
          return null;
        }
      } else {
        return null;  
      }
    } else {
      if(this.value != null) {
        return this.formatDate(new Date(this.value));
      } else {
        return null;
      }
    }
  }

  public set displayvalue(val: string) {
    
  }

  private formatDate(dt: Date) : string {
    let val = this.format;
    val = val.replace('YYYY', dt.getFullYear().toString());
    val = val.replace('YY', (dt.getFullYear() % 100).toString());
    val = val.replace('MM', this.convertToStringAndPad(dt.getMonth() + 1, 2));
    val = val.replace('DD', this.convertToStringAndPad(dt.getDate(), 2));
    val = val.replace('HH', this.convertToStringAndPad(dt.getHours(), 2));
    val = val.replace('mm', this.convertToStringAndPad(dt.getMinutes(), 2));
    return val;
  }

  private convertToStringAndPad(num: number, n: number) : string {
    let ret :string = num.toString();
    while(ret.length < n) 
      ret = '0' + ret;
    return ret;
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

      let config: DateTimePopupConfig = new DateTimePopupConfig();
      config.initialDate = this.rbObject != null && this.rbObject.data[this.attribute] != null ? new Date(this.rbObject.data[this.attribute]) : new Date();
      config.datePart = this.format.indexOf('YY') > -1 || this.format.indexOf('MM') > -1 || this.format.indexOf('DD') > -1 ? true : false;
      config.hourPart = this.format.indexOf('HH') > -1 ? true : false;
      config.minutePart = this.format.indexOf('mm') > -1 ? true : false;
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
    if(this.attribute != null) {
      if(this.rbObject != null) {
        if(dt != null)
          this.rbObject.setValue(this.attribute, dt.toISOString());
        else
          this.rbObject.setValue(this.attribute, null);
      }
    } else {
      this.valueChange.emit(dt.toISOString());
    }
    this.overlayRef.dispose();
    this.overlayRef = null;
    this.inputContainerRef.element.nativeElement.blur();
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
