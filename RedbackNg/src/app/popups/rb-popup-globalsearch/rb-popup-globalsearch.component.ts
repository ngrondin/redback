import { Component, Inject, OnInit } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { ConfigService } from 'app/services/config.service';
import { DataService } from 'app/services/data.service';
import { FilterService } from 'app/services/filter.service';
import { CONTAINER_DATA } from 'app/tokens';

class GlobalSearchConfig {
  search: string;
}

class ResultItem {
  config: any;
  object: RbObject;

  constructor(c: any, o: RbObject) {
    this.config = c;
    this.object = o;
  }

  getObjectIcon(): string {
    return this.config.icon;
  }

  getObjectTypeText(): string {
    return this.config.objectlabel;
  }

  getObjectHeaderText(): string {
    return (this.config.labelprefix != null ? this.config.labelprefix + ' ' : '') + this.object.get(this.config.labelattribute);
  }

  getObjectSubText(): string {
    return this.object.get(this.config.descriptionattribute);
  }
}

@Component({
  selector: 'rb-popup-globalsearch',
  templateUrl: './rb-popup-globalsearch.component.html',
  styleUrls: ['./rb-popup-globalsearch.component.css', '../rb-popup/rb-popup.component.css']
})
export class RbPopupGlobalSearchComponent extends RbPopupComponent implements OnInit {
  searchResult: ResultItem[] = [];
  currentlyLoading: number = -1;
  objectIds: string[];

  constructor(
    @Inject(CONTAINER_DATA) public config: GlobalSearchConfig, 
    private dataService: DataService,
    private configService: ConfigService,
    private filterService: FilterService
  ) {
    super();
  }

  ngOnInit() {
    this.objectIds = Object.keys(this.configService.objectsConfig).filter(o => this.configService.objectsConfig[o].searchfilter != null);
    this.objectIds.sort((a, b) => this.configService.objectsConfig[a].searchpriority - this.configService.objectsConfig[b].searchpriority);
    this.currentlyLoading = 0;
    this.searchNext();
  }

  public getHighlighted() {

  }
  
  public setSearch(val: String) {
    
  }
  
  public keyTyped(keyCode: number) {
    
  }

  searchNext() {
    if(this.currentlyLoading >= 0 && this.currentlyLoading < this.objectIds.length) {
      let objectId = this.objectIds[this.currentlyLoading];
      let objectConfig = this.configService.objectsConfig[objectId];
      let objectname = objectConfig.object ?? objectId;
      let filter = this.filterService.resolveFilter(objectConfig.searchfilter, null);
      this.dataService.fetchList(objectname, filter, this.config.search, null, 0, 50, true).subscribe(
        data => {
          for(const object of data) {
            this.searchResult.push(new ResultItem(objectConfig, object));
          }
          this.currentlyLoading++;
          this.searchNext();
        }
      ); 
    } else {
      this.currentlyLoading = -1;
    }
  }

  select(item: ResultItem) {
    this.selected.emit({
      object: item.object,
      view: item.config.view
    });
  }
}
