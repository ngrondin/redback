import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ViewContainerRef, SimpleChange } from '@angular/core';
import { RbObject } from 'app/datamodel';

import { AgmCoreModule, AgmMap } from '@agm/core';
import { ApiService } from 'app/api.service';
import { ElementSchemaRegistry } from '@angular/compiler';
import { MatMenuTrigger, MatMenu, MatMenuPanel } from '@angular/material';
import { isError } from 'util';
import { Translator, InitialsMaker } from 'app/helpers';


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
  }
}

class MapObject {
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

@Component({
  selector: 'rb-map',
  templateUrl: './rb-map.component.html',
  styleUrls: ['./rb-map.component.css'],
  exportAs: 'rbMap'
})
export class RbMapComponent implements OnInit {
  @Input('lists') lists : any;
  @Input('list') list: RbObject[];
  @Input('series') series: any[];
  @Input('selectedObject') selectedObject: RbObject;
  @Input('geoattribute') geoattribute: string;
  @Input('labelattribute') labelattribute: string;
  @Input('descriptionattribute') descriptionattribute: string;
  @Output() selectObject: EventEmitter<any> = new EventEmitter();
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  @ViewChild('map', { read: AgmMap }) map: AgmMap;

  seriesConfigs: MapSeriesConfig[] = [];
  mapObjects: MapObject[] = [];
  selectedMapObject: MapObject;

  minLat: number;
  maxLat: number;
  minLon: number;
  maxLon: number;
  zoomOfMap: number;
  mapLatitude: number;
  mapLongitude: number;

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
  ) { }

  ngOnChanges(changes : SimpleChange) {
    if('series' in changes && this.series != null) {
      this.seriesConfigs = [];
      for(let item of this.series) {
        this.seriesConfigs.push(new MapSeriesConfig(item));
      }
    }
    if(('geoattribute' in changes || 'labelattribute' in changes)) {
      if(this.seriesConfigs.length == 0) {
        this.seriesConfigs.push(new MapSeriesConfig({}));
      }
      this.seriesConfigs[0].geometryAttribute = this.geoattribute;
      this.seriesConfigs[0].labelAttribute = this.labelattribute;
    }    
    if('lists' in changes || 'list' in changes) {
      if(this.haveListsChanged()) {
        this.calcAll();
      }
    }
    if('selectedObject' in changes) {
      this.calcAll();
    }
  }
  
  ngOnInit() {
    this.zoomOfMap = 2;
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
    this.mapObjects = [];
    this.minLat = 90;
    this.maxLat = -90;
    this.minLon = 180;
    this.maxLon = -180;

    if(this.list != null && this.seriesConfigs[0] != null) {
      this.calcList(this.list, this.seriesConfigs[0]);
    } else if(this.lists != null && this.seriesConfigs.length > 0) {
      for(let ser of this.seriesConfigs) {
        this.calcList(this.lists[ser.dataset], ser);
      }
    }
    this.calcMapCoords();
  }

  calcList(list: RbObject[], cfg: MapSeriesConfig) {
    for(let object of list) {
      let latitude = null;
      let longitude = null;   
      if(cfg.geometryAttribute != null ) {
        let geometry: any = object.get(cfg.geometryAttribute);
        if(geometry != null) {
          if(geometry.type == 'point' && geometry.coords != null) {
            if(!isNaN(geometry.coords.latitude) && !isNaN(geometry.coords.longitude)) {
              latitude = geometry.coords.latitude;
              longitude = geometry.coords.longitude;
            }
          }
        }
      } 

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

      if(latitude != null && longitude != null && visible) {
        if(latitude < this.minLat) this.minLat = latitude - 0.5;
        if(latitude > this.maxLat) this.maxLat = latitude + 0.5;
        if(longitude < this.minLon) this.minLon = longitude - 0.5;
        if(longitude > this.maxLon) this.maxLon = longitude + 0.5;
        let mapObject = new MapObject(object, latitude, longitude, label, initials, icon, color, animate, link, object == this.selectedObject ? 10 : 5);
        this.mapObjects.push(mapObject);
        if(object == this.selectedObject) {
          this.selectedMapObject = mapObject;
        }
      }
    }
  }

  calcMapCoords() : any {
    if(this.selectedMapObject != null) {
      this.zoomOfMap = 12;
      this.mapLatitude = this.selectedMapObject.latitude;
      this.mapLongitude = this.selectedMapObject.longitude;
    } else if(this.mapObjects.length > 0) {
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
  

  objectClick(mapObject: MapObject) {
    this.selectObject.emit(mapObject.object);
    if(mapObject.label != null) {
      this.labellatLon.latitude = mapObject.latitude;
      this.labellatLon.longitude = mapObject.longitude;
      this.labelText = mapObject.label;
      this.labelLink = mapObject.link;
      this.showLabel = true;
    }
  }

  objectRightClick(mapObject: MapObject) {
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

  labelClick() {
    if(this.labelLink != null) {
      this.navigate.emit(this.labelLink);
    }
  }

  mouseMove(event: any) {
    this.mousePosition = {x: event.offsetX, y: event.offsetY};
  }

}
