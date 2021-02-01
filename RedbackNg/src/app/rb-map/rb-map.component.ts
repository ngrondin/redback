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
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  @ViewChild('map', { read: AgmMap }) map: AgmMap;

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
    private apiService: ApiService
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
    }
  }

  dataObserverDestroy() {
  }

  onActivationEvent(event: any) {
  }

  onDatasetEvent(event: any) {
    this.calcAll();
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

  haveListsChanged(): Boolean {
    let changed: Boolean = false;
    let count: number = 0;
    if(this.list != null) {
      for(let obj of this.list) {
        count += 1;
        if(obj['lastUpdated'] > this.lastObjectUpdate) {
          changed = true;
          this.lastObjectUpdate = obj['lastUpdated'];
        }
      }
    }
    if(this.lists != null) {
      for(let ser of this.seriesConfigs) {
        for(let obj of this.lists[ser.dataset]) {
          count += 1;
          if(obj['lastUpdated'] > this.lastObjectUpdate) {
            changed = true;
            this.lastObjectUpdate = obj['lastUpdated'];
          }
        }
      }
    }
    if(count != this.lastObjectCount) {
      changed = true;
      this.lastObjectCount = count;
    }
    return changed;
  }

  calcAll() {
    this.mapPoints = [];
    this.mapPolygons = [];
    this.minLat = 90;
    this.maxLat = -90;
    this.minLon = 180;
    this.maxLon = -180;

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
          if(latLon.latitude < this.minLat) this.minLat = latLon.latitude - 0.5;
          if(latLon.latitude > this.maxLat) this.maxLat = latLon.latitude + 0.5;
          if(latLon.longitude < this.minLon) this.minLon = latLon.longitude - 0.5;
          if(latLon.longitude > this.maxLon) this.maxLon = latLon.longitude + 0.5;
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
        mapPolygon.addObject(object, latLon.latitude, latLon.longitude);
      }
    }
    this.mapPolygons.push(mapPolygon);
  }

  calcMapCoords() : any {
    if(this.preventNextMoveOrZoom == false) {
      if(this.selectedMapPoint != null) {
        this.zoomOfMap = 12;
        this.mapLatitude = this.selectedMapPoint.latitude;
        this.mapLongitude = this.selectedMapPoint.longitude;
      } else if(this.mapPoints.length > 0) {
        this.mapLatitude = ((this.maxLat + this.minLat) / 2);
        this.mapLongitude = ((this.maxLon + this.minLon) / 2);
        if(this.maxLat - this.minLat < 0 || this.maxLon - this.minLon < 0) {
          this.zoomOfMap = 2;
        } else {
          this.zoomOfMap = (1.0 / (60.0 + (this.maxLon - this.minLon)) * 500.0);
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

}
