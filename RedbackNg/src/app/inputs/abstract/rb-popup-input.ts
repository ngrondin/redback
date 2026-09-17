import { Component, Input } from '@angular/core';
import { ComponentRef, ViewChild, ViewContainerRef } from '@angular/core';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { RbFieldInputComponent } from '../abstract/rb-field-input';
import { PopupService } from 'app/services/popup.service';
import { FilterService } from 'app/services/filter.service';
import { AppInjector } from 'app/app.module';

@Component({template: ''})
export abstract class RbPopupInputComponent extends RbFieldInputComponent {
  @ViewChild('input', { read: ViewContainerRef }) inputContainerRef: ViewContainerRef;
  @Input('filter') filter: any;

  popupComponentRef: ComponentRef<RbPopupComponent>;
  filterService: FilterService;

  constructor(
    public popupService: PopupService
  ) {
    super();
    this.filterService = AppInjector.get(FilterService);
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
    super.startEditing();
  }

  public abstract getPopupClass() : any;

  public abstract getPopupConfig() : any;

  public getResolvedFilter() : any {
    return this.filter != null ? this.filterService.resolveFilter(this.filter, this.rbObject, this.dataset, this.relatedObject) : null;
  }

  private closePopup() {
    this.popupService.closePopup();
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
}