import { HostBinding, HostListener } from '@angular/core';
import { Component } from '@angular/core';
import { SimpleChanges } from '@angular/core';
import { Input, Output, EventEmitter } from '@angular/core';
import { Color } from '@swimlane/ngx-charts';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { AppInjector } from 'app/app.module';
import { RbObject } from 'app/datamodel';
import { ColorConfig } from 'app/helpers';
import { DialogService } from 'app/services/dialog.service';
import { UserprefService } from 'app/services/userpref.service';


@Component({template: ''})
export abstract class RbInputComponent extends RbDataObserverComponent {
  @Input('attribute') attribute?: string;    
  @Input('value') _value: any = null;
  @Input('variable') variable: any;
  @Input('label') label?: string;
  @Input('tip') tip?: string;
  @Input('icon') _icon?: string;
  @Input('showicon') showicon: boolean = true;
  @Input('size') size?: number;
  @Input('grow') grow?: number;
  @Input('editable') editable: boolean = true;
  @Input('mandatory') mandatory: boolean = false;
  @Input('alert') alert: boolean = false;
  @Input('color') _color: any;
  @Input('updatescript') _updatescript?: string;
  @Output('valueChange') valueChange = new EventEmitter();
  
  @HostBinding('style.flex-grow') get flexgrow() { return this.grow != null ? this.grow : 0;}
  @HostBinding('style.width') get styleWidth() { return (this.size != null ? ('min(' + (0.88 * this.size) + 'vw, ' + (17 * this.size) + 'px)'): this.defaultSize != null ? ('min(' + (0.88 * this.defaultSize) + 'vw, ' + (17 * this.defaultSize) + 'px)'): null);}

  previousValue: any;
  previousObject: RbObject | null = null;
  flasherOn: boolean = false;
  defaultIcon: string = "description";
  defaultSize: number = 15;
  mouseIsOver: boolean = false;
  showTip: boolean = false;
  dialogService: DialogService;
  userprefService: UserprefService;
  updateScript?: Function;
  colorConfig?: ColorConfig;
  color?: string;

  constructor( ) {
    super();
    this.dialogService = AppInjector.get(DialogService);
    this.userprefService = AppInjector.get(UserprefService);
  }

  dataObserverInit() {
    if(this.dataset != null && this.attribute != null) {
      const userpref = AppInjector.get(UserprefService);
      let swtch = userpref.getCurrentViewUISwitch('input',  this.dataset.objectname + "." + this.attribute);
      if(swtch == false) {
        this.showExpr = 'false';
      }
    }
    this.inputInit();
  }

  inputInit() {
    if(this._updatescript != null) {
      this.updateScript = Function("previousvalue", "value", this._updatescript);
    }
    if(this._color != null) {
      this.colorConfig = new ColorConfig(this._color);
    }
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
    if(this.active) {
      if(this.colorConfig != null && this.rbObject != null) {
        this.color = this.colorConfig.getValue(this.rbObject);
      }
    }
  }

  onActivationEvent(state: boolean) {
  }

  ngOnChanges(changes: SimpleChanges) {
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
    } else if(this.variable != null) {
      val = window.redback[this.variable];
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
    if(this.editable == false) return true;
    if(this.attribute == null) return false;
    if(this.rbObject == null) return true;
    if(this.attribute == 'uid') {
      return this.rbObject.uid != null;
    } else {
      let targetObj: RbObject | null = this.rbObject;
      let targetAttr = this.attribute;
      if(this.attribute.indexOf(".") > -1) {
        targetObj = this.rbObject.getRelated(this.attribute.substring(0, this.attribute.lastIndexOf(".")));
        targetAttr = this.attribute.substring(this.attribute.lastIndexOf(".") + 1);
      }
      if(targetObj != null && targetObj.validation[targetAttr] != null) {
        return !targetObj.validation[targetAttr].editable;
      } else {
        return true;
      }
    }
  }

  public get mandatoryOn(): boolean {
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

  public get alertOn(): boolean {
    return this.alert;
  }



  public flash() {
    setTimeout(() => {this.flasherOn = true}, 1);
    setTimeout(() => {this.flasherOn = false}, 100);
  }

  public commit(val: any, related: RbObject | null = null) {
    var currentValue = this.value;
    if(currentValue != val) {
      if(this.attribute != null) {
        if(this.rbObject != null) {
          let targetObj : RbObject | null = this.rbObject;
          let targetAttr = this.attribute;
          if(this.attribute.indexOf(".") > -1) {
            targetObj = this.rbObject.getRelated(this.attribute.substring(0, this.attribute.lastIndexOf(".")));
            targetAttr = this.attribute.substring(this.attribute.lastIndexOf(".") + 1);
          }
          if(targetObj != null) {
            if(related != null) {
              return targetObj.setValueAndRelated(targetAttr, val, related)
            } else {
                return targetObj.setValue(targetAttr, val);
            } 
          }
        }
      } else if(this.variable != null) {
        window.redback[this.variable] = val;
        window.redback.publishEvent({event:"global", variable: this.variable});
      } else {
        this._value = val;
      }      
      this.valueChange.emit(val);  
      if(this.updateScript != null) {
        this.updateScript.call(window.redback, currentValue, val);
      }
    }
  }

  /*** Deprecated, should rather use the tip directive */
  @HostListener('mouseenter', ['$event']) onEnter(event: any) {
    this.mouseIsOver = true;
    if(this.tip != null) {
      setTimeout(() => {
        if(this.mouseIsOver == true && this.tip != null) {
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
