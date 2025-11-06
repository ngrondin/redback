import { Component, OnInit, Input, Output, EventEmitter, ComponentRef, ViewContainerRef, ViewChild } from '@angular/core';
import { NavigateEvent, RbObject } from 'app/datamodel';
import { DataService } from 'app/services/data.service';
import { ConfigService } from 'app/services/config.service';
import { FilterService } from 'app/services/filter.service';
import { PopupService } from 'app/services/popup.service';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { RbPopupGlobalSearchComponent } from 'app/popups/rb-popup-globalsearch/rb-popup-globalsearch.component';
import { NavigateService } from 'app/services/navigate.service';

@Component({
  selector: 'rb-global-search',
  templateUrl: './rb-global-seach.component.html',
  styleUrls: ['./rb-global-seach.component.css']
})
export class RbGlobalSeachComponent implements OnInit {
  @ViewChild('input', { read: ViewContainerRef }) inputContainerRef: ViewContainerRef;
  //@Output() navigate: EventEmitter<any> = new EventEmitter();

  searchString: string;
  showResults: boolean = false;
  popupComponentRef: ComponentRef<RbPopupComponent>;

  constructor(
    private dataService: DataService,
    private configService: ConfigService,
    private filterService: FilterService,
    private popupService: PopupService,
    private navigateService: NavigateService
  ) { }

  ngOnInit() {
  }

  startGlobalSearch() {
    if(this.popupComponentRef == null) {
      this.popupComponentRef = this.popupService.openPopup(this.inputContainerRef, RbPopupGlobalSearchComponent, {search: this.searchString});
      this.popupComponentRef.instance.selected.subscribe(item => this.navigateToObject(item.object, item.view));
      this.popupComponentRef.instance.cancelled.subscribe(() => this.stopGlobalSearch());
    }
  }

  stopGlobalSearch() {
    if(this.popupComponentRef != null) {
      this.popupService.closePopup();
      this.popupComponentRef = null;
    }
  }

  keyDown(event: any) {
    if(event.keyCode == 13) {
      this.startGlobalSearch();
    } else if(event.keyCode == 27) {
      this.stopGlobalSearch()
    } else if(event.keyCode == 9) {
      this.stopGlobalSearch();
    }
  }

  blur(event: any) {
    if(this.popupComponentRef != null) {
      this.inputContainerRef.element.nativeElement.focus();
    }
  }

  navigateToObject(object: RbObject, view: string) {
    let event: NavigateEvent = {
      view: view,
      objectname: object.objectname,
      datatargets: [{
        filter:{uid : "'" + object.uid + "'"}
      }]
    };
    this.navigateService.navigateTo(event);
    this.stopGlobalSearch();
  }
}
