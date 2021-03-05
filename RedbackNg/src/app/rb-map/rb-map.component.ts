import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ViewContainerRef, SimpleChange } from '@angular/core';
import { RbObject } from 'app/datamodel';

import { AgmCoreModule, AgmMap } from '@agm/core';
import { ApiService } from 'app/services/api.service';
import { ElementSchemaRegistry } from '@angular/compiler';
import { MatMenuTrigger, MatMenu, MatMenuPanel } from '@angular/material';
import { isError } from 'util';
import { Translator, InitialsMaker } from 'app/helpers';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { ElementRef } from '@angular/core';


class MapSeriesConfig {
  dataset: string;
  geometryAttribute: string;
  labelAttribute: string;
  initialsAttribute: string;
  icon: string;
  iconAttribute: string;
  iconMap: Translator;
  colorAttribute: string;
  colorMap: Translator;
  animateAttribute: string;
  animateMap: Translator;
  visibleAttribute: string;
  linkAttribute: string;
  dateAttribute: string;

  constructor(json: any) {
    this.dataset = json.dataset;
    this.geometryAttribute = json.geometryattribute;
    this.labelAttribute = json.labelattribute;
    this.initialsAttribute = json.initialsattribute;
    this.iconAttribute = json.iconattribute;
    this.icon = json.icon;
    this.iconMap = new Translator(json.iconmap);
    this.colorAttribute = json.colorattribute;
    this.colorMap = new Translator(json.colormap);
    this.animateAttribute = json.animateattribute;
    this.animateMap = new Translator(json.animatemap);
    this.visibleAttribute = json.visibleattribute;
    this.linkAttribute = json.linkattribute;
    this.dateAttribute = json.dateattribute;
  }
}

class MapPoint {
  object: RbObject;
  latitude: number;
  longitude: number;
  label: string;
  initials: string;
  icon: string;
  color: string;
  animate: string;
  link: any;
  zindex: number;

  constructor(obj: RbObject, lat: number, lon: number, lbl: string, i: string, ic: string, c: string, a: string, l: any, z: number) {
    this.object = obj;
    this.latitude = lat;
    this.longitude = lon;
    this.label = lbl;
    this.initials = i;
    this.icon = ic;
    this.color = c;
    this.animate = a;
    this.link = l;
    this.zindex = z;
  }
}

class MapPolygon {
  objects: RbObject[];
  path: any[];
  zindex: number;

  constructor() {
    this.objects = [];
    this.path = [];
  }

  public addObject(object: RbObject, lat: number, lng: number) {
    this.objects.push(object);
    this.path.push({lat: lat, lng: lng});
  }
}

@Component({
  selector: 'rb-map',
  templateUrl: './rb-map.component.html',
  styleUrls: ['./rb-map.component.css'],
  exportAs: 'rbMap'
})
export class RbMapComponent extends RbDataObserverComponent {
  @Input('series') series: any[];
  @Input('geoattribute') geoattribute: string;
  @Input('labelattribute') labelattribute: string;
  @Input('descriptionattribute') descriptionattribute: string;
  @Input('dateattribute') dateattribute: string;
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  @ViewChild('map', { read: AgmMap }) map: AgmMap;
  @ViewChild('map') elementView: ElementRef;

  seriesConfigs: MapSeriesConfig[] = [];
  mapPoints: MapPoint[] = [];
  mapPolygons: MapPolygon[] = [];
  selectedMapPoint: MapPoint;

  minLat: number;
  maxLat: number;
  minLon: number;
  maxLon: number;
  zoomOfMap: number;
  mapLatitude: number;
  mapLongitude: number;
  userMovedOrZoomed: boolean = false;
  preventNextMoveOrZoom: boolean = false;
  
  showContextMenu: boolean = false;
  contextMenuPosition: any = {x: 0, y: 0};
  showLabel: boolean = false;
  labellatLon: any = {latitude: 0, longitude: 0};
  labelText: string = "";
  labelLink: any = null;
  mousePosition: any = {x: 0, y: 0};
  clickCoords: any = {x: 0, y: 0};
  
  lastObjectCount: number = 0;
  lastObjectUpdate: number = 0;

  defaultStyles = [
    {
      featureType: "poi",
      elementType: "labels",
      stylers: [
        { visibility: "off" }
      ]
    }
  ];

  constructor(
    private elementRef:ElementRef
    //private apiService: ApiService
  ) {
    super();
  }

  dataObserverInit() {
    this.zoomOfMap = 2;
    if(this.series != null) {
      this.seriesConfigs = [];
      for(let item of this.series) {
        this.seriesConfigs.push(new MapSeriesConfig(item));
      }
    } else if(this.geoattribute != null) {
      if(this.seriesConfigs.length == 0) {
        this.seriesConfigs.push(new MapSeriesConfig({}));
      }
      this.seriesConfigs[0].geometryAttribute = this.geoattribute;
      this.seriesConfigs[0].labelAttribute = this.labelattribute;
      this.seriesConfigs[0].dateAttribute = this.dateattribute;
    }
  }

  dataObserverDestroy() {
  }

  onActivationEvent(event: any) {
    setTimeout(() => this.calcAll(), 300);
  }

  onDatasetEvent(event: any) {
    this.calcAll();
  }

  onMapReady(event: any) {
  }

  get selectedObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : this.datasetgroup != null ? this.datasetgroup.selectedObject : null;
  }

  get list(): RbObject[] {
    return this.dataset != null ? this.dataset.list : null;
  }

  get lists(): any {
    return this.datasetgroup != null ? this.datasetgroup.lists : null;
  }

  calcAll() {
    if(this.active) {
      console.log("calc all");
      this.minLat = 90;
      this.maxLat = -90;
      this.minLon = 180;
      this.maxLon = -180;
      this.mapPoints = [];
      this.mapPolygons = [];
      this.selectedMapPoint = null;
  
      if(this.list != null && this.seriesConfigs[0] != null) {
        this.calcList(this.list, this.seriesConfigs[0]);
      } else if(this.lists != null && this.seriesConfigs.length > 0) {
        for(let seriesConfig of this.seriesConfigs) {
          let list = this.lists[seriesConfig.dataset];
          if(list != null) {
            this.calcList(list, seriesConfig);
          }
        }
      }
      this.calcMapCoords();
    }
  }

  calcList(list: RbObject[], cfg: MapSeriesConfig) {
    if(cfg.dateAttribute == null) {
      this.calcMapPoints(list, cfg);
    } else {
      this.calcPolygon(list, cfg);
    }
  }

  calcMapPoints(list: RbObject[], cfg: MapSeriesConfig) {
    for(let object of list) {
      let latLon = this.getObjectLatLon(object, cfg);
      if(latLon != null) {
        let label = null;
        if(cfg.labelAttribute != null) {
          label = object.get(cfg.labelAttribute);
        }
  
        let initials = null;
        if(cfg.initialsAttribute != null) {
          initials = new InitialsMaker().get(object.get(cfg.initialsAttribute));
        }
  
        let icon = "pin";
        if(cfg.icon != null) {
          icon = cfg.icon;
        } else if(cfg.iconAttribute != null) {
          icon = cfg.iconMap.get(object.get(cfg.iconAttribute), object == this.selectedObject);
        } 
  
        let color = (object == this.selectedObject ? 'red' : '#3f51b5');
        if(cfg.colorAttribute != null) {
          color = cfg.colorMap.get(object.get(cfg.colorAttribute), object == this.selectedObject);
        } 
  
        let animate: string = null;
        if(cfg.animateAttribute != null) {
          animate = cfg.animateMap.get(object.get(cfg.animateAttribute), object == this.selectedObject);
        }
  
        let link = null;
        if(cfg.linkAttribute != null) {
          link = {
            object: cfg.linkAttribute == 'uid' ? object.objectname : object.related[cfg.linkAttribute].objectname,
            filter: {uid: "'" + (cfg.linkAttribute == 'uid' ? object.uid : object.get(cfg.linkAttribute)) + "'"}
          };
        }
  
        let visible: boolean = true;
        if(cfg.visibleAttribute != null) {
          visible = object.get(cfg.visibleAttribute);
        }
  
        if(visible) {
          this.calcMinMaxLatLon(latLon);
          let mapPoint = new MapPoint(object, latLon.latitude, latLon.longitude, label, initials, icon, color, animate, link, object == this.selectedObject ? 10 : 5);
          this.mapPoints.push(mapPoint);
          if(object == this.selectedObject) {
            this.selectedMapPoint = mapPoint;
          }
        }        
      }
    }
  }

  calcPolygon(list: RbObject[], cfg: MapSeriesConfig) {
    let mapPolygon = new MapPolygon();
    let sortedList = list.sort((a: RbObject, b: RbObject) => (new Date(a.get(cfg.dateAttribute))).getTime() - (new Date(b.get(cfg.dateAttribute))).getTime());
    for(let object of sortedList) {
      let latLon = this.getObjectLatLon(object, cfg);
      if(latLon != null) {
        this.calcMinMaxLatLon(latLon);
        mapPolygon.addObject(object, latLon.latitude, latLon.longitude);
      }
    }
    this.mapPolygons.push(mapPolygon);
  }

  calcMinMaxLatLon(latLon: any) {
    if(latLon.latitude < this.minLat) this.minLat = latLon.latitude;
    if(latLon.latitude > this.maxLat) this.maxLat = latLon.latitude;
    if(latLon.longitude < this.minLon) this.minLon = latLon.longitude;
    if(latLon.longitude > this.maxLon) this.maxLon = latLon.longitude;

  }

  calcMapCoords() : any {
    if(this.preventNextMoveOrZoom == false) {
      if(this.selectedMapPoint != null) {
        this.zoomOfMap = 12;
        this.mapLatitude = this.selectedMapPoint.latitude;
        this.mapLongitude = this.selectedMapPoint.longitude;
      } else if(this.mapPoints.length > 0 || this.mapPolygons.length > 0) {
        this.mapLatitude = ((this.maxLat + this.minLat) / 2);
        this.mapLongitude = ((this.maxLon + this.minLon) / 2);
        if(this.maxLat - this.minLat < 0 || this.maxLon - this.minLon < 0) {
          this.zoomOfMap = 2;
        } else {
          let pxWidth = this.map['_elem'].nativeElement.clientWidth;
          let pxHeight = this.map['_elem'].nativeElement.clientHeight;
          let mWidth = Math.floor(this.distance(this.mapLatitude, this.minLon, this.mapLatitude, this.maxLon));
          let mHeight = Math.floor(this.distance(this.minLat, this.mapLongitude, this.maxLat, this.mapLongitude));
          let vertmperpx = Math.floor(mHeight / pxHeight);
          let horimperpx = Math.floor(mWidth / pxWidth);
          console.log(pxWidth + "x" + pxHeight + "  " + mWidth + "x" + mHeight + "  " + horimperpx + "x" + vertmperpx);
          this.zoomOfMap = Math.floor(Math.log2(156543.03392 * Math.cos(this.mapLatitude * Math.PI / 180) / Math.max(vertmperpx, horimperpx)));
          console.log('zoom is ' + this.zoomOfMap);
          //this.zoomOfMap = (350.0 / (60.0 + (Math.max(this.maxLon - this.minLon, this.maxLat - this.minLat))));
          //console.log("zoom is " + this.zoomOfMap);
        }
      } else {
        this.zoomOfMap = 2;
      }
    } else {
      this.preventNextMoveOrZoom = false;
    }
  }

  private getObjectLatLon(object: RbObject, cfg: MapSeriesConfig) {
    if(cfg.geometryAttribute != null ) {
      let geometry: any = object.get(cfg.geometryAttribute);
      if(geometry != null) {
        if(geometry.type == 'point' && geometry.coords != null) {
          if(!isNaN(geometry.coords.latitude) && !isNaN(geometry.coords.longitude)) {
            return {
              latitude: geometry.coords.latitude,
              longitude: geometry.coords.longitude  
            }
          }
        }
      }
    } 
    return null;
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
  

  objectClick(mapPoint: MapPoint) {
    this.preventNextMoveOrZoom = true;
    if(this.dataset != null) {
      this.dataset.select(mapPoint.object);
    } else if (this.datasetgroup != null) {
      this.datasetgroup.select(mapPoint.object);
    }
    if(mapPoint.label != null) {
      this.labellatLon.latitude = mapPoint.latitude;
      this.labellatLon.longitude = mapPoint.longitude;
      this.labelText = mapPoint.label;
      this.labelLink = mapPoint.link;
      this.showLabel = true;
    }
  }

  objectRightClick(mapPoint: MapPoint) {
  }

  mapClick(event: any) {
    this.showContextMenu = false;
    this.showLabel = false;
  }

  mapRightClick(event: any) {
    this.clickCoords = event.coords;
    if(this.selectedObject != null && this.geoattribute != null) {
      if(this.selectedObject.validation[this.geoattribute].editable == true) {
        this.showContextMenu = true;
        this.contextMenuPosition = {x: this.mousePosition.x, y: this.mousePosition.y};
      }
    }
  }

  zoomChange(event: any) {
    this.userMovedOrZoomed = true;
  }

  labelClick() {
    if(this.labelLink != null) {
      this.navigate.emit(this.labelLink);
    }
  }

  mouseMove(event: any) {
    this.mousePosition = {x: event.offsetX, y: event.offsetY};
  }

  distance(lat1: number, lon1: number, lat2: number, lon2: number) : number {
    const R = 6371e3; // metres
    const φ1 = lat1 * Math.PI/180; // φ, λ in radians
    const φ2 = lat2 * Math.PI/180;
    const Δφ = (lat2-lat1) * Math.PI/180;
    const Δλ = (lon2-lon1) * Math.PI/180;
    const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
              Math.cos(φ1) * Math.cos(φ2) *
              Math.sin(Δλ/2) * Math.sin(Δλ/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    const d = R * c; // in metres
    return d;
  }

}
