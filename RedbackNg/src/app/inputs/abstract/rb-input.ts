import { HostBinding, HostListener } from '@angular/core';
import { SimpleChanges } from '@angular/core';
import { Input, Output, EventEmitter } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { AppInjector } from 'app/app.module';
import { RbObject } from 'app/datamodel';
import { DialogService } from 'app/services/dialog.service';
import { UserprefService } from 'app/services/userpref.service';



export abstract class RbInputComponent extends RbDataObserverComponent {
  @Input('attribute') attribute: string;    
  @Input('object') _rbObject: RbObject;
  @Input('value') _value: any;
  @Input('label') label: string;
  @Input('tip') tip: string;
  @Input('icon') _icon: string;
  @Input('showicon') showicon: boolean = true;
  @Input('size') size: number;
  @Input('grow') grow: number;
  @Input('editable') editable: boolean = true;
  @Input('mandatory') mandatory: boolean = false;
  @Output('valueChange') valueChange = new EventEmitter();
  
  @HostBinding('style.flex-grow') get flexgrow() { return this.grow != null ? this.grow : 0;}
  @HostBinding('style.width') get styleWidth() { return (this.size != null ? ((0.88 * this.size) + 'vw'): this.defaultSize != null ? ((0.88 * this.defaultSize) + 'vw'): null);}

  previousValue: any;
  previousObject: RbObject;
  flasherOn: boolean = false;
  defaultIcon: string;
  defaultSize: number = 15;
  mouseIsOver: boolean = false;
  showTip: boolean = false;
  dialogService: DialogService;
  userprefService: UserprefService;


  constructor( ) {
    super();
    this.dialogService = AppInjector.get(DialogService);
    this.userprefService = AppInjector.get(UserprefService);
  }

  dataObserverInit() {
    if(this.dataset != null && this.attribute != null) {
      const userpref = AppInjector.get(UserprefService);
      let swtch = userpref.getUISwitch('input',  this.dataset.object + "." + this.attribute);
      if(swtch == false) {
        this.show = 'false';
      }
    }
    this.inputInit();
  }

  abstract inputInit();

  dataObserverDestroy() {
  }

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
  }

  ngOnChanges(changes: SimpleChanges) {
  }

  get rbObject() : RbObject {
    if(this.dataset != null) {
        return this.dataset.selectedObject;
    } else {
        return this._rbObject;
    }
  }

  get value() : any {
    let val = null;
    if(this.attribute != null) {
        if(this.rbObject != null) {
            if(this.attribute == 'uid') {
                val = this.rbObject.uid;
            } else {
                val = this.rbObject.get(this.attribute);
            } 
        } else {
            val = null;
        }
    } else {
        val = this._value;
    }
    if(val != null && this.previousValue != val && (this.rbObject == null || (this.rbObject != null && this.previousObject != null && this.previousObject == this.rbObject))) {
        this.flash();
    }   
    this.previousValue = val;
    this.previousObject = this.rbObject; 
    return val;
  }

  get icon(): string {
    return this._icon != null ? this._icon : this.defaultIcon;
  }

  public get readonly(): boolean {
    if(this.attribute != null) {
      if(this.rbObject != null) {
        if(this.attribute == 'uid') {
          return this.rbObject.uid != null;
        } else if(this.rbObject.validation[this.attribute] != null) {
          return !(this.editable && this.rbObject.validation[this.attribute].editable);
        } else {
          return true;      
        }
      } else {
        return true;
      }
    } else {
      return !this.editable;
    }
  }

  public get mandatoryalert(): boolean {
    if(this.value == null) {
      if(this.attribute != null) {
        if(this.rbObject != null) {
          if(this.attribute == 'uid') {
            return true;
          } else if(this.rbObject.validation[this.attribute] != null) {
            return this.mandatory || this.rbObject.validation[this.attribute].mandatory;
          } else {
            return false;      
          }
        } else {
          return false;
        }
      } else {
        return this.mandatory;
      }    
    } else {
      return false;
    }
  }

  public abstract get displayvalue(): any;

  public abstract set displayvalue(val: any);

  public flash() {
    setTimeout(() => {this.flasherOn = true}, 1);
    setTimeout(() => {this.flasherOn = false}, 100);
  }

  public commit(val: any, related: RbObject = null) {
    if(this.attribute != null) {
        if(this.rbObject != null) {
            this.rbObject.setValueAndRelated
            if(related != null) {
                return this.rbObject.setValueAndRelated(this.attribute, val, related)
            } else {
                return this.rbObject.setValue(this.attribute, val);
            } 
        } 
    } else {
        this._value = val;
    }      
    this.valueChange.emit(val);
  }


  @HostListener('mouseenter', ['$event']) onEnter(event: any) {
    this.mouseIsOver = true;
    if(this.tip != null) {
      setTimeout(() => {
        if(this.mouseIsOver == true) {
          this.dialogService.showTooltip(this.tip, event.target, "below");
        }
      }, 1000);  
    }
  }

  @HostListener('mouseleave', ['$event']) onLeave(event: any) {
    this.mouseIsOver = false;
    this.dialogService.hideTooltip();
  }
}
