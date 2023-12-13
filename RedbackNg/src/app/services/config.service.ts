import { Injectable } from '@angular/core';
import { RbObject } from '../datamodel';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  public objectsConfig: any;
  public nlCommandModel: string;
  public personalViews: any[];

  constructor() { }

  setObjectsConfig(c: any) {
    this.objectsConfig = c;
  }

  setNLCommandModel(m: string) {
    this.nlCommandModel = m;
  }

  setPersonalViews(list: any[]) {
    if(list != null) {
      this.personalViews = list.map(i => ({
        view: i.view,
        label: i.label
      }));  
    } else {
      this.personalViews = [];
    }
  }

  getLabel(obj: RbObject) : string {
    if(this.objectsConfig != null && this.objectsConfig[obj.objectname] != null) {
      let config = this.objectsConfig[obj.objectname];
      return config.labelprefix + " " + obj.get(config.labelattribute);
    } else {
      return null;
    }
  }

  getDescription(obj: RbObject) : string {
    if(this.objectsConfig != null && this.objectsConfig[obj.objectname] != null) {
      let config = this.objectsConfig[obj.objectname];
      return obj.get(config.descriptionattribute);
    } else {
      return null;
    }
  }

  getView(obj: RbObject) : string {
    if(this.objectsConfig != null && this.objectsConfig[obj.objectname] != null) {
      let config = this.objectsConfig[obj.objectname];
      return config.view;
    } else {
      return null;
    }
  }


}
