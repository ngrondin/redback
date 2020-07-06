import { Component, OnInit, Inject, InjectionToken, Output, EventEmitter } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { RbObject } from '../datamodel';
import { OverlayRef } from '@angular/cdk/overlay';
import { CONTAINER_DATA } from '../tokens';
import { DataService } from 'app/data.service';
import { RbPopupComponent } from 'app/rb-popup/rb-popup.component';

@Component({
  selector: 'rb-popup-list',
  templateUrl: './rb-popup-list.component.html',
  styleUrls: ['./rb-popup-list.component.css']
})
export class RbPopupListComponent extends RbPopupComponent implements OnInit {

  public hierarchy: RbObject[] = [];
  public list: RbObject[] = [];
  public search: string;
  public isLoading: boolean;

  constructor(
    @Inject(CONTAINER_DATA) public config: any, 
    public overlayRef: OverlayRef,
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
    let filter: any = {};
    if(this.config.parentattribute != null) {
      if(this.hierarchy.length > 0) {
        const lastObject = this.hierarchy[this.hierarchy.length - 1];
        filter[this.config.parentattribute] = this.config.childattribute == 'uid' ? lastObject.uid : lastObject.data[this.config.childattribute];
      } else {
        filter[this.config.parentattribute] = null;
      }
    }
    this.isLoading = true;
    this.dataService.listRelatedObjects(this.config.rbObject.objectname, this.config.rbObject.uid, this.config.attribute, filter, this.search).subscribe(data => this.setData(data));
  }

  public setData(objects: RbObject[]) {
    this.list = objects;
    this.isLoading = false;
  }

  public setSearch(str: string) {
    this.search = str;
    this.getData();
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

  public getHighlighted() {
    throw new Error("Method not implemented.");
  }  
}
