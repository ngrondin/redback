import { Component, OnInit, Input, Output, EventEmitter, ViewChild } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { LatLon, MapCircle, MapDot, MapObject, MapPin, MapPolygon, MapSeriesConfig } from './rb-map-models';
import { InitialsMaker } from 'app/helpers';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { ElementRef } from '@angular/core';
import { RbDataCalcComponent, SeriesConfig } from 'app/abstract/rb-datacalc';



@Component({
  selector: 'rb-map',
  templateUrl: './rb-map.component.html',
  styleUrls: ['./rb-map.component.css'],
  exportAs: 'rbMap'
})
export class RbMapComponent extends RbDataCalcComponent<MapSeriesConfig> {

  @Input('geoattribute') geoattribute: string;
  @Input('labelattribute') labelattribute: string;
  @Input('descriptionattribute') descriptionattribute: string;
  @Input('dateattribute') dateattribute: string;
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  @ViewChild('map', {static: true}) map: ElementRef;

  
  mapPins: MapPin[] = [];
  mapDots: MapDot[] = [];
  mapCircles: MapCircle[] = [];
  mapPolygons: MapPolygon[] = [];
  mapCurrentDatePin: MapPin = null;
  //selectedLatLon: LatLon;
  //selectedMapObject: MapObject;

  minLat: number;
  maxLat: number;
  minLon: number;
  maxLon: number;
  desiredZoom: number;
  desiredMapCenter: LatLon = new LatLon(0, 0);
  actualZoom: number;
  actualMapCenter: LatLon = new LatLon(0, 0);
  actualMperPX: number;
  preventReframe: boolean = false;
  //userMovedOrZoomed: boolean = false;
  //preventNextMoveOrZoom: boolean = false;
  
  showContextMenu: boolean = false;
  contextMenuPosition: any = {x: 0, y: 0};
  showLabel: boolean = false;
  labellatLon: LatLon = new LatLon(0, 0);
  labelText: string = "";
  labelLink: any = null;
  mousePosition: any = {x: 0, y: 0};
  clickCoords: any = {x: 0, y: 0};

  minDate: Date = new Date();
  maxDate: Date = new Date((new Date()).getTime() + 3600000);
  _currentDate: Date = null;
  
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
  ) {
    super();
  }

  dataCalcInit() {
    if(this.series == null && this.geoattribute != null) {
      this.seriesConfigs.push(new MapSeriesConfig({
        geometryattribute: this.geoattribute,
        labelattribute: this.labelattribute,
        dateattribute: this.dateattribute
      }));
    }
  }

  dataCalcDestroy() {
  }

  onMapReady(event: any) {

  }

  onBoundsChange(event: any) {
    //console.log("bounds change");
  }

  onZoomChange(event: any) {
    this.actualMperPX = 156543.03392 * Math.cos(this.desiredMapCenter.latitude * Math.PI / 180) / Math.pow(2, event);
    //console.log("zoom change, mperpx=" + mperpx);
    this.redraw();
  }


  public get currentDate() : Date {
    return this._currentDate;
  }

  public set currentDate(cd: Date) {
    this._currentDate = cd;
    if(this.dateattribute != null && this.list != null) {
      let closestObject = null;
      let closestTimeDiff = 86400000;
      for(let obj of this.list) {
        let dt = new Date(obj.get(this.dateattribute));
        let timeDiff = cd.getTime() - dt.getTime();
        if(timeDiff < closestTimeDiff && timeDiff >= 0) {
          closestObject = obj;
          closestTimeDiff = timeDiff;
        }
      }
      if(closestObject != null) {
        let latLon = this.getObjectLatLon(closestObject, this.seriesConfigs[0]);
        this.mapCurrentDatePin = new MapPin(closestObject, latLon, "darkred", null, null, null, 10);
      }
    }
  }

  calcParams() {
    //console.log("calcData");
    if(this.active) {
      this.reframe();
    }
  }

  reframe() {
    //console.log("recalc");
    if(!this.preventReframe) {
      let selectedLatLon = this.selectedObject != null ? this.getObjectLatLon(this.selectedObject, this.getSeriesConfigForObject(this.selectedObject)) : null;
      if(selectedLatLon != null) {
        this.desiredMapCenter = selectedLatLon;
        this.minLat = selectedLatLon.latitude - 0.01;
        this.maxLat = selectedLatLon.latitude + 0.01;
        this.minLon = selectedLatLon.longitude - 0.01;
        this.maxLon = selectedLatLon.longitude + 0.01;
      } else {
        this.minLat = 90;
        this.maxLat = -90;
        this.minLon = 180;
        this.maxLon = -180;
        this.iterateAllLists((object, config) => {
          let latLon = this.getObjectLatLon(object, config);
          if(latLon != null) {
            if(latLon.latitude < this.minLat) this.minLat = latLon.latitude;
            if(latLon.latitude > this.maxLat) this.maxLat = latLon.latitude;
            if(latLon.longitude < this.minLon) this.minLon = latLon.longitude;
            if(latLon.longitude > this.maxLon) this.maxLon = latLon.longitude;
          }
        });  
        if(this.minLat > this.maxLat || this.minLon > this.maxLon) {
          this.minLat = -90;
          this.maxLat = 90;
          this.minLon = -180;
          this.maxLon = 180;
        }
        this.desiredMapCenter = new LatLon(((this.maxLat + this.minLat) / 2), ((this.maxLon + this.minLon) / 2));
      }

      let pxWidth = this.map['_elem'].nativeElement.clientWidth;
      let pxHeight = this.map['_elem'].nativeElement.clientHeight;
      let mWidth = Math.floor(this.distance(this.desiredMapCenter.latitude, this.minLon, this.desiredMapCenter.latitude, this.maxLon));
      let mHeight = Math.floor(this.distance(this.minLat, this.desiredMapCenter.longitude, this.maxLat, this.desiredMapCenter.longitude));
      let vertmperpx = (mHeight / pxHeight);
      let horimperpx = (mWidth / pxWidth);
      this.desiredZoom = Math.floor(Math.log2(156543.03392 * Math.cos(this.desiredMapCenter.latitude * Math.PI / 180) / Math.max(vertmperpx, horimperpx)));
    } else {
      this.preventReframe = false;
    }
  }

  redraw() {
    //console.log("redraw");
    this.mapPins = [];
    this.mapDots = [];
    this.mapCircles = [];
    this.mapPolygons = [];
    this.mapCurrentDatePin = null;

    for(let config of this.seriesConfigs) {
      if(config.dateAttribute == null) {
        this.iterateList(config, (object, config) => this.calcMapObject(object, config));
      } else {
        this.calcPolygon(config);
      }
    }
    //console.log("pins " + this.mapPins.length + " dots " + this.mapDots.length + " circles " + this.mapCircles.length + " poly " + this.mapPolygons.length);
  }

  calcMapObject(object: RbObject, cfg: MapSeriesConfig) {
    let latLon = this.getObjectLatLon(object, cfg);
    if(latLon != null) { 
      let geometry: any = object.get(cfg.geometryAttribute);
      let visible: boolean = cfg.visibleAttribute != null ? object.get(cfg.visibleAttribute) : true;
      if(visible) {
        let selected = object == this.selectedObject;
        let color = cfg.colorAttribute != null ? cfg.colorMap.get(object.get(cfg.colorAttribute), selected) : (selected ? 'red' : '#3f51b5');
        let animate = cfg.animateAttribute != null ? cfg.animateMap.get(object.get(cfg.animateAttribute), selected) : null;
        let label = cfg.labelAttribute != null ? object.get(cfg.labelAttribute) : null;
        let link = null;
        if(cfg.linkAttribute != null) {
          link = {
            object: cfg.linkAttribute == 'uid' ? object.objectname : object.related[cfg.linkAttribute].objectname,
            filter: {uid: "'" + (cfg.linkAttribute == 'uid' ? object.uid : object.get(cfg.linkAttribute)) + "'"}
          };
        }
        let zIndex = object == this.selectedObject ? 10 : 5;

        if(geometry["type"] == "point") {
          let icon = cfg.icon != null ? cfg.icon : cfg.iconAttribute != null ? cfg.iconMap.get(object.get(cfg.iconAttribute), selected) : 'pin';
          if(icon == "pin") {
            this.mapPins.push(new MapPin(object, latLon, color, animate, label, link, zIndex));
          } else if(icon == "dot") {
            let initials = null;
            if(cfg.initialsAttribute != null) {
              initials = new InitialsMaker().get(object.get(cfg.initialsAttribute));
            }
            this.mapDots.push(new MapDot(object, latLon, color, initials, animate, label, link, zIndex));
          }
        } else if(geometry["type"] == "circle") {
          let radius = parseInt(geometry["radius"].toString());
          let radiusPx = radius / this.actualMperPX;
          if(radiusPx < 10) {
            this.mapPins.push(new MapPin(object, latLon, color, animate, label, link, zIndex));
          } else {
            this.mapCircles.push(new MapCircle(object, latLon, radius, color, animate, label, link, zIndex));
          }
        }
      }
    }
  }

  calcPolygon(cfg: MapSeriesConfig) {
    let mapPolygon = new MapPolygon("red", null, null);
    let sortedList = this.getList(cfg).sort((a: RbObject, b: RbObject) => (new Date(a.get(cfg.dateAttribute))).getTime() - (new Date(b.get(cfg.dateAttribute))).getTime());
    if(sortedList.length > 0) {
      this.minDate = new Date(sortedList[0].get(cfg.dateAttribute));
      this.maxDate = new Date(sortedList[sortedList.length - 1].get(cfg.dateAttribute));
      if(sortedList.length == 1) {
        let latLon = this.getObjectLatLon(sortedList[0], cfg);
        this.mapCurrentDatePin = new MapPin(sortedList[0], latLon, "darkred", null, null, null, 10);
      } else {
        for(let object of sortedList) {
          let latLon = this.getObjectLatLon(object, cfg);
          if(latLon != null) {
            mapPolygon.addObject(object, latLon);
          }
        }
        this.mapPolygons.push(mapPolygon);
      }
    }

  }

  private getObjectLatLon(object: RbObject, cfg: MapSeriesConfig) {
    if(cfg.geometryAttribute != null ) {
      let geometry: any = object.get(cfg.geometryAttribute);
      if(geometry != null) {
        if(geometry.coords != null) {
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
  

  objectClick(mapObject: MapObject) {
    this.preventReframe = true;
    if(this.dataset != null) {
      this.dataset.select(mapObject.getRbObject());
    } else if (this.datasetgroup != null) {
      this.datasetgroup.select(mapObject.getRbObject());
    }
    if(mapObject.label != null) {
      this.labellatLon = mapObject.getLatLon();
      this.labelText = mapObject.label;
      this.labelLink = mapObject.link;
      this.showLabel = true;
    }
  }

  objectRightClick(mapPoint: MapPin) {
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

  /*zoomChange(event: any) {
    this.userMovedOrZoomed = true;
  }*/

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

  createSeriesConfig(json: any): MapSeriesConfig {
    return new MapSeriesConfig(json);
  }
}
