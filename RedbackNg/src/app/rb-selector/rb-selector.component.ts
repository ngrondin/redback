import { Component, ComponentRef, EventEmitter, HostBinding, Input, Output, ViewChild, ViewContainerRef } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { RbPopupHardlistComponent } from 'app/popups/rb-popup-hardlist/rb-popup-hardlist.component';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { PopupService } from 'app/services/popup.service';

@Component({
  selector: 'rb-selector',
  templateUrl: '../inputs/abstract/rb-field-input.html',
  styleUrls: ['../inputs/abstract/rb-field-input.css']
})
export class RbSelectorComponent extends RbDataObserverComponent {
  @Input('label') label: string;
  @Input('displayattribute') displayattribute: string;
  @Input('tip') tip: string;
  @Input('icon') _icon: string;
  @Input('showicon') showicon: boolean = true;
  @Input('size') size: number;
  @Input('grow') grow: number;
  @Input('margin') margin: boolean = true;


  @Output('valueChange') valueChange = new EventEmitter();
  @Output('keydown') keydown = new EventEmitter();
  
  @ViewChild('input', { read: ViewContainerRef }) inputContainerRef: ViewContainerRef;
  
  @HostBinding('class.rb-input-margin') get marginclass() { return this.margin }
  @HostBinding('style.flex-grow') get flexgrow() { return this.grow != null ? this.grow : 0;}
  @HostBinding('style.width') get styleWidth() { return (this.size != null ? ((0.88 * this.size) + 'vw'): this.defaultSize != null ? ((0.88 * this.defaultSize) + 'vw'): null);}

  popupComponentRef: ComponentRef<RbPopupComponent>;
  
  isEditing: boolean = false;
  editedValue: any;
  hadUserEdit: boolean = false;
  originalValue: any;
  inputType: string = 'text';
  defaultSize: number = 15;
  mandatoryOn = false;
  alertOn = false;
  flasherOn = false;
  readonly = false;

  constructor(
    public popupService: PopupService
  ) {
    super();
  }


  dataObserverInit() {
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
  }

  public onFocus(event: any) {
    if(this.popupComponentRef == null) {
      this.startEditing();
      if(this.isEditing) {
        this.popupComponentRef = this.popupService.openPopup(this.inputContainerRef, this.getPopupClass(), this.getPopupConfig());
        this.popupComponentRef.instance.selected.subscribe(value => this.onPopupValueSelected(value));
        this.popupComponentRef.instance.cancelled.subscribe(() => this.onPopupCancel());
        event.target.select();
      }
    }
  }
  
  public onBlur(event: any) {
    if(this.popupComponentRef != null) {
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
    this.isEditing = true;
    this.hadUserEdit = false;
    this.initEditedValue();
  }

  public getPopupClass() : any {
    return RbPopupHardlistComponent;
  };

  public getPopupConfig() : any {
    let dataset = this.getDataset();
    let list = [];
    if(dataset != null) {
      list = dataset.list.map(o => ({display: o.get(this.displayattribute), value: o}));
    }
    return list;
  }

  private closePopup() {
    this.popupService.closePopup();
    this.popupComponentRef = null;
    this.inputContainerRef.element.nativeElement.blur();
  }

  public finishEditing() {
    this.cancelEditing();
  }

  public finishEditingWithSelection(value: any) {
    this.closePopup();
    this.isEditing = false;
    this.editedValue = null;  
    this.hadUserEdit = false; 
    let dataset = this.getDataset();
    if(dataset != null) {
      dataset.select(value);
    }    
  }

  public cancelEditing() {
    this.isEditing = false;
    this.editedValue = null;
    this.closePopup();
  }


  public onKeyup(event: any) {
  }

  public get displayvalue(): any {
    return this.getDisplayValue();
  }

  public getDisplayValue(): any {
    if(this.isEditing) {
      return this.getEditingDisplayValue();
    } else {
      return this.getPersistedDisplayValue();
    }
  } 

  public getPersistedDisplayValue(): any {
    let obj = this.selectedObject;
    return obj != null ? obj.get(this.displayattribute) : null;
  }

  public getEditingDisplayValue(): any {
    return this.editedValue;
  }

  public set displayvalue(val: any) {
    this.setDisplayValue(val);
  }

  public setDisplayValue(val: any) {
    if(this.isEditing) {
      this.editedValue = val;
    } 
  }

  public initEditedValue() {
    this.editedValue = "";
  }


  /*public commit(val: any, related: RbObject = null) {
    console.log("Selector selected");

  }*/

  get icon(): string {
    return this._icon != null ? this._icon : "list";
  }

  getDataset() {
    var dataset = null;
    if(this.dataset != null) {
      dataset = this.dataset;
    } if(this.datasetgroup != null && this.targetdatasetid != null) {
      dataset = this.datasetgroup.datasets[this.targetdatasetid]; 
    }
    return dataset;
  }

}
