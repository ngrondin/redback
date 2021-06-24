import { LatLng } from "@agm/core";
import { SeriesConfig } from "app/abstract/rb-datacalc";
import { RbObject } from "app/datamodel";
import { Translator } from "app/helpers";

export class MapSeriesConfig extends SeriesConfig {
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
        super(json);
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
  
  export class LatLon {
    latitude: number;
    longitude: number;
    
    constructor(lat: number, lon: number) {
        this.latitude = lat;
        this.longitude = lon;
    }
  }

  export abstract class MapObject {
    color: string;
    label: string;
    animate: string;
    link: any;
    zindex: number;

    constructor(c: string, a: string, lbl: string, l: any, z: number) {
      this.color = c;
      this.label = lbl;
      this.animate = a;
      this.link = l;
      this.zindex = z;
    }

    abstract getRbObject() : RbObject;
    abstract getLatLon() : LatLon;
  }
  
  export class MapPin extends MapObject {
    object: RbObject;
    latLon: LatLon;
  
    constructor(obj: RbObject, ll: LatLon, c: string, a: string, lbl: string, l: any, z: number) {
      super(c, a, lbl, l, z);
      this.object = obj;
      this.latLon = ll;
    }

    getRbObject() { return this.object; }
    getLatLon() { return this.latLon; }
  }
  

  export class MapDot extends MapObject {
    object: RbObject;
    latLon: LatLon;
    initials: string;
  
    constructor(obj: RbObject, ll: LatLon, c: string,  i: string, a: string, lbl: string, l: any, z: number) {
      super(c, a, lbl, l, z);
      this.object = obj;
      this.latLon = ll;
      this.initials = i;
      this.animate = a;
    }

    getRbObject() { return this.object; }
    getLatLon() { return this.latLon; }
  }

  export class MapCircle extends MapObject {
    object: RbObject;
    latLon: LatLon;
    radius: number;
    color: string;
  
    constructor(obj: RbObject, ll: LatLon, rad: number, c: string, a:string, lbl: string, l: any, z: number) {
      super(c, a, lbl, l, z);
      this.object = obj;
      this.latLon = ll;
      this.radius = rad;
      this.color = c;
    }

    getRbObject() { return this.object; }
    getLatLon() { return this.latLon; }
  }
  
  export class MapPolygon extends MapObject {
    objects: RbObject[];
    path: LatLon[];
    zindex: number;
  
    constructor(c: string, a: string, lbl: string) {
        super(c, a, lbl, null, 100);
      this.objects = [];
      this.path = [];
    }
  
    public addObject(object: RbObject, ll: LatLon) {
      this.objects.push(object);
      this.path.push(ll);
    }

    getRbObject() { return this.objects.length > 0 ? this.objects[0] : null; }
    getLatLon() { return this.path.length > 0 ? this.path[0] : null; }
  }
  