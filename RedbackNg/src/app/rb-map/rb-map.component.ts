import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ViewContainerRef } from '@angular/core';
import { RbObject } from 'app/datamodel';

import { AgmCoreModule, AgmMap } from '@agm/core';
import { ApiService } from 'app/api.service';
import { ElementSchemaRegistry } from '@angular/compiler';
import { MatMenuTrigger, MatMenu, MatMenuPanel } from '@angular/material';

@Component({
  selector: 'rb-map',
  templateUrl: './rb-map.component.html',
  styleUrls: ['./rb-map.component.css'],
  exportAs: 'rbMap'
})
export class RbMapComponent implements OnInit {
  @Input('selectedObject') selectedObject: RbObject;
  @Input('list') list: RbObject[];
  @Input('geoattribute') geoattribute: string;
  @Input('labelattribute') labelattribute: string;
  @Input('descriptionattribute') descriptionattribute: string;
  @Output() selectObject: EventEmitter<any> = new EventEmitter();
  @ViewChild('map', { read: AgmMap, static: false }) map: AgmMap;

  zoomOfMap: number;
  showContextMenu: boolean = false;
  contextMenuPosition: any = {x: 0, y: 0};
  mousePosition: any = {x: 0, y: 0};
  clickCoords: any = {x: 0, y: 0};

  constructor(
    private apiService: ApiService
  ) { }

  ngOnInit() {
    this.zoomOfMap = 2;
  }


  get latLonOfMap() : any {
    let latLon : any = this.latLonOf(this.selectedObject);
    if(latLon != null) {
      this.zoomOfMap = 12;
      return latLon;
    } else if(this.list != null) {
      let minLat = 90;
      let maxLat = -90;
      let minLon = 180;
      let maxLon = -180;
      for(let obj of this.list) {
        let ll = this.latLonOf(obj);
        if(ll != null) {
          if(ll.latitude < minLat) minLat = ll.latitude - 0.5;
          if(ll.latitude > maxLat) maxLat = ll.latitude + 0.5;
          if(ll.longitude < minLon) minLon = ll.longitude - 0.5;
          if(ll.longitude > maxLon) maxLon = ll.longitude + 0.5;
        }
      }
      latLon = {latitude: ((maxLat + minLat) / 2), longitude: ((maxLon + minLon) / 2)};
      if(maxLat - minLat < 0 || maxLon - minLon < 0) {
        this.zoomOfMap = 2;
      } else {
        this.zoomOfMap = (1.0 / (60.0 + (maxLon - minLon)) * 500.0);
      }
      return latLon;
    } else {
      latLon = {latitude: 0, longitude: 0};
      this.zoomOfMap = 2;
      return latLon;
    }
  }

  latitudeOf(object: RbObject, def: number) : number {
    let latLon = this.latLonOf(object);
    return latLon != null ? latLon.latitude : def;
  }

  longitudeOf(object: RbObject, def: number) : number {
    let latLon = this.latLonOf(object);
    return latLon != null ? latLon.longitude : def;
  }

  iconUrlOf(object: RbObject) : string {
    let url = null;
    let geometry = this.geometryOf(object);
    if(geometry != null) {
      if(object == this.selectedObject) {
        url = this.apiService.baseUrl + '/' + this.apiService.uiService + '/resource/pin10.png';
      } else {
        url = this.apiService.baseUrl + '/' + this.apiService.uiService + '/resource/pin11.png';
      }
    } 
    return url;
  }

  zIndexOf(object: RbObject) : number {
    if(object == this.selectedObject)
      return 10;
    else
      return 1;
  }

  labelOf(object: RbObject) : string {
    let label = "";
    if(object != null && this.labelattribute != null) {
      label = object.data[this.labelattribute];
    }
    return label;    
  }


  latLonOf(object: RbObject) : any {
    let geometry = this.geometryOf(object);
    if(geometry != null) {
      if(geometry.type == 'point' && geometry.coords != null) {
        if(!isNaN(geometry.coords.latitude) && !isNaN(geometry.coords.longitude)) {
          return geometry.coords;
        }
      }
    }
    return null;
  }

  geometryOf(object: RbObject) : any {
    if(object != null && this.geoattribute != null ) {
      return object.get(this.geoattribute);
    } else {
      return null;
    }
  }

  setGeometry(event: any) {
    this.showContextMenu = false;
    if(this.selectedObject != null && this.geoattribute != null) {
      if(this.selectedObject.validation[this.geoattribute].editable == true) {
        this.selectedObject.setValue(this.geoattribute, {
          type: 'point',
          coords: {
            latitude: this.clickCoords.lat,
            longitude: this.clickCoords.lng
          }
        });
      }
    }
  }

  objectClick(object: RbObject) {
    this.selectObject.emit(object);
  }

  objectRightClick(object: RbObject) {
  }

  mapClick(event: any) {
    this.showContextMenu = false;
  }

  mapRightClick(event: any) {
    this.clickCoords = event.coords;
    this.showContextMenu = true;
    this.contextMenuPosition = {x: this.mousePosition.x, y: this.mousePosition.y};
  }

  mouseMove(event: any) {
    this.mousePosition = {x: event.offsetX, y: event.offsetY};
  }

}
