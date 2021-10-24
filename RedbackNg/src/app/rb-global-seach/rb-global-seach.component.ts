import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { DataService } from 'app/services/data.service';
import { ConfigService } from 'app/services/config.service';

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
  currentlyLoading: number = -1;

  constructor(
    private dataService: DataService,
    private configService: ConfigService
  ) { }

  ngOnInit() {
  }

  globalSearch() {
    this.searchResult = [];
    this.showResults = true;
    this.currentlyLoading = 0;
    this.search();
  }

  search() {
    let objectnames = Object.keys(this.configService.objectsConfig);
    if(this.currentlyLoading >= 0 && this.currentlyLoading < objectnames.length) {
      let objectname = objectnames[this.currentlyLoading];
      this.dataService.listServerObjects(objectname, null, this.searchString, null, 0, 50, true).subscribe(
        data => {
          this.searchResult = this.searchResult.concat(data);
          this.currentlyLoading++;
          this.search();
        }
      ); 
    } else {
      this.currentlyLoading = -1;
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
