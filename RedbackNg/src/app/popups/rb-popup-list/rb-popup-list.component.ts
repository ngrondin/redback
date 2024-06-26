import { Component, OnInit, Inject } from '@angular/core';
import { RbObject } from '../../datamodel';
import { CONTAINER_DATA } from '../../tokens';
import { DataService } from 'app/services/data.service';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';

@Component({
  selector: 'rb-popup-list',
  templateUrl: './rb-popup-list.component.html',
  styleUrls: ['./rb-popup-list.component.css', '../rb-popup/rb-popup.component.css']
})
export class RbPopupListComponent extends RbPopupComponent implements OnInit {

  public hierarchy: RbObject[] = [];
  public list: RbObject[] = [];
  public search: string;
  public isLoading: boolean;
  public highlightIndex: number = -1;

  constructor(
    @Inject(CONTAINER_DATA) public config: any, 
    private dataService: DataService
  ) {
    super();
  }

  ngOnInit() {
    this.search = "";
    this.getData();
  }

  public get isHierarchical(): boolean {
    return (this.config.parentattribute != null && this.config.childattribute != null);
  }

  public getData() {
    let filter: any = {...this.config.filter};
    if(this.config.parentattribute != null) {
      if(this.hierarchy.length > 0) {
        const lastObject = this.hierarchy[this.hierarchy.length - 1];
        filter[this.config.parentattribute] = this.config.childattribute == 'uid' ? lastObject.uid : lastObject.data[this.config.childattribute];
      } else {
        filter[this.config.parentattribute] = null;
      }
    }
    let sort = {
      "0":{
        attribute: this.config.sortattribute ?? this.config.displayattribute,
        dir: 1
      }
    }
    this.isLoading = true;
    if(this.config.rbObject != null && this.config.attribute != null) {
      this.dataService.fetchRelatedList(this.config.rbObject.objectname, this.config.rbObject.uid, this.config.attribute, filter, this.search, sort, true).subscribe(data => this.setData(data));
    } else if(this.config.objectname != null) {
      this.dataService.fetchList(this.config.objectname, filter, this.search, sort, 0, 50, false).subscribe(data => this.setData(data));
    }
  }

  public setData(objects: RbObject[]) {
    this.list = objects;
    this.isLoading = false;
  }

  public setSearch(str: string) {
    this.search = str;
    this.getData();
  }

  public keyTyped(keyCode: number) {
    if(keyCode == 40) { // Down
      if(this.highlightIndex < this.list.length - 1) {
        this.highlightIndex++;
      }
    } else if(keyCode == 38) { // Up
      if(this.highlightIndex > 0) {
        this.highlightIndex--;
      }
    } else if(keyCode == 13) {
      if(this.highlightIndex > -1 && this.highlightIndex < this.list.length) {
        this.select(this.list[this.highlightIndex]);
      }
    }
  }

  public select(object: RbObject) {
    this.selected.emit(object);
  }

  public expand(object: RbObject) {
    this.list = [];
    this.hierarchy.push(object);
    this.getData();
  }

  public colapse(object: RbObject) {
    let i = this.hierarchy.indexOf(object);
    this.list = [];
    this.hierarchy.splice(i);
    this.getData();
  }

  public getHighlighted() : any {
    if(this.highlightIndex > -1 && this.highlightIndex < this.list.length) {
      return this.list[this.highlightIndex];
    } else {
      return null;
    }
  }  
}
