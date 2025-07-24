import { Component, ComponentRef, Input } from '@angular/core';
import { RbDataButtonComponent } from '../abstract/rb-databutton';
import { RbPopupActionsComponent } from 'app/popups/rb-popup-actions/rb-popup-actions.component';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { ActionService } from 'app/services/action.service';
import { DataService } from 'app/services/data.service';
import { PopupService } from 'app/services/popup.service';
import { UserprefService } from 'app/services/userpref.service';
import { RbActiongroupAction } from '../rb-actiongroup/rb-actiongroup.component';
import { RbPopupListComponent } from 'app/popups/rb-popup-list/rb-popup-list.component';
import { RbObject } from 'app/datamodel';
import { FilterService } from 'app/services/filter.service';
import { RbActionButtonComponent } from '../rb-actionbutton/rb-actionbutton.component';
import { LogService } from 'app/services/log.service';

@Component({
  selector: 'rb-actionlist',
  templateUrl: '../rb-button/rb-button.html',
  styleUrls: ['../rb-button/rb-button.css']
})
export class RbActionlistComponent extends RbActionButtonComponent {
  @Input('object') object: string;
  @Input('displayattribute') displayattribute: string;
  @Input('basefilter') basefilter: any;

  popupComponentRef: ComponentRef<RbPopupComponent>;

  constructor(
    actionService: ActionService,
    public userpref: UserprefService,
    public popupService: PopupService,
    public filterService: FilterService,
    public logService: LogService
  ) {
    super(actionService);
    this.label = 'Actions';
  }
  

  dataObserverInit() {

  }

  dataObserverDestroy() {

  }

  onDatasetEvent(event: any) {

  }

  click() {
    if(this.popupComponentRef == null) {
      this.openPopup();
    } else {
      this.closePopup();
    }
  }
  
  public openPopup() {
    let config = {
      objectname: this.object,
      displayattribute: this.displayattribute,
      filter: this.filterService.resolveFilter(this.basefilter, this.dataset.selectedObject, this.dataset.selectedObject, this.dataset.relatedObject)
    }
    this.popupComponentRef = this.popupService.openPopup(this.buttonContainerRef, RbPopupListComponent, config);
    this.popupComponentRef.instance.selected.subscribe(object => this.objectSelected(object));
    this.popupComponentRef.instance.cancelled.subscribe(() => this.closePopup());
  }

  public closePopup() {
    this.popupService.closePopup();
    this.popupComponentRef = null;
  }

  public objectSelected(object: RbObject) {
    this.logService.info("Action list selected object " + object.uid);
    this.closePopup();
    this.running = true;
    this.actionService.action(this.dataset, this.action, this.target, this.param, {pickedObject:object}, this.confirm, this.timeout).subscribe((rbObject) => {
      this.running = false;
    })
  }
}
