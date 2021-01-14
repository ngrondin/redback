import { Injectable } from '@angular/core';
import { RbObject } from '../datamodel';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  public objectsConfig: any;

  constructor() { }

  setObjectsConfig(c: any) {
    this.objectsConfig = c;
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
