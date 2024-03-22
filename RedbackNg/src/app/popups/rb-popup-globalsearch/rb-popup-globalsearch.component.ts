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

@Component({
  selector: 'rb-popup-globalsearch',
  templateUrl: './rb-popup-globalsearch.component.html',
  styleUrls: ['./rb-popup-globalsearch.component.css', '../rb-popup/rb-popup.component.css']
})
export class RbPopupGlobalSearchComponent extends RbPopupComponent implements OnInit {

  searchResult: RbObject[] = [];
  currentlyLoading: number = -1;
  objectnames: string[];

  constructor(
    @Inject(CONTAINER_DATA) public config: GlobalSearchConfig, 
    private dataService: DataService,
    private configService: ConfigService,
    private filterService: FilterService
  ) {
    super();
  }

  ngOnInit() {
    this.objectnames = Object.keys(this.configService.objectsConfig).filter(o => this.configService.objectsConfig[o].searchfilter != null);
    this.objectnames.sort((a, b) => this.configService.objectsConfig[a].searchpriority - this.configService.objectsConfig[b].searchpriority);
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
    if(this.currentlyLoading >= 0 && this.currentlyLoading < this.objectnames.length) {
      let objectname = this.objectnames[this.currentlyLoading];
      let filter = this.filterService.resolveFilter(this.configService.objectsConfig[objectname].searchfilter, null);
      this.dataService.fetchList(objectname, filter, this.config.search, null, 0, 50, true).subscribe(
        data => {
          this.searchResult = this.searchResult.concat(data);
          this.currentlyLoading++;
          this.searchNext();
        }
      ); 
    } else {
      this.currentlyLoading = -1;
    }
  }

  getObjectIcon(obj: RbObject): string {
    let objectConfig = this.configService.objectsConfig[obj.objectname];
    if(objectConfig != null) {
      return objectConfig.icon;
    } else {
      return null;
    }
  }

  getObjectTypeText(obj: RbObject): string {
    let objectConfig = this.configService.objectsConfig[obj.objectname];
    if(objectConfig != null) {
      return objectConfig.objectlabel;
    } else {
      return null;
    }  
  }

  getObjectHeaderText(obj: RbObject): string {
    let objectConfig = this.configService.objectsConfig[obj.objectname];
    if(objectConfig != null) {
      return (objectConfig.labelprefix != null ? objectConfig.labelprefix + ' ' : '') + obj.get(objectConfig.labelattribute);
    } else {
      return obj.uid;
    }
  }

  getObjectSubText(obj: RbObject): string {
    let objectConfig = this.configService.objectsConfig[obj.objectname];
    if(objectConfig != null) {
      return obj.get(objectConfig.descriptionattribute);
    } else {
      return null;
    }
  }

  select(obj: RbObject) {
    this.selected.emit(obj);
  }
}
