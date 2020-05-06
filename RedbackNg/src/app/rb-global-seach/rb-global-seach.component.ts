import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { DataService } from 'app/data.service';
import { ConfigService } from 'app/config.service';

@Component({
  selector: 'rb-global-search',
  templateUrl: './rb-global-seach.component.html',
  styleUrls: ['./rb-global-seach.component.css']
})
export class RbGlobalSeachComponent implements OnInit {
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  searchString: string;
  searchResult: RbObject[] = [];
  showResults: boolean = false;
  isLoading: number = 0;

  constructor(
    private dataService: DataService,
    private configService: ConfigService
  ) { }

  ngOnInit() {
  }

  globalSearch() {
    this.searchResult = [];
    this.showResults = true;
    for(let o in this.configService.objectsConfig) {
      this.isLoading = this.isLoading + 1;
      this.dataService.listObjects(o, null, this.searchString, 0).subscribe(
        data => {
          this.searchResult = this.searchResult.concat(data);
          this.isLoading = this.isLoading - 1;
        }
      );
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

  keyDown(event: any) {
    if(event.keyCode == 13) {
      this.globalSearch();
    }
  }

  blur(event: any) {
    setTimeout(()=> {
      this.showResults = false;;
    }, 100);
  }

  navigateToObject(object: RbObject) {
    this.navigate.emit({
      object : object.objectname,
      filter : {
        uid : "'" + object.uid + "'"
      }
    });
  }
}
