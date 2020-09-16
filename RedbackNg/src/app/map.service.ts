import { Injectable } from '@angular/core';
import { RbObject } from './datamodel';

@Injectable({
  providedIn: 'root'
})
export class MapService {

  constructor() { }


  public mergeMaps(map1: any, map2: any) : any {
    let map: any = {};
    for (const key in map1) {
      let value = map1[key];
      map[key] = value;
    }
    for (const key in map2) {
      let value = map2[key];
      map[key] = value;
    }
    return map;
  }



  public resolveMap(__inMap: any, obj: RbObject, selectedObject: RbObject, relatedObject: RbObject) : any {
    try {
      let __outMap: any = {};
      let uid = null;
      let __varString = "";
      if(obj != null && typeof obj != 'undefined') {
        __varString = __varString + "var uid = '" + obj.uid + "';\r\n"
        for(const attr in obj.data) {
          let val = obj.data[attr];
          if(typeof val == 'object') {
            val = JSON.stringify(val);
          } 
          if(typeof val == 'string') {
            val = "'" + val.replace(/\'/g, "\\'").replace(/\"/g, "\\\"").replace(/\n/g, "\\n").replace(/\r/g, "\\r") + "'";
          } 
          __varString = __varString + "var " + attr + " = " + val + ";\r\n"
        }
      } 

      for (const __key in __inMap) {
        let __value = __inMap[__key];
        if(__value == null) {
          __value = null;
        } else if(typeof __value == "string") {
          __value = eval(__varString + __value);
        } else if(typeof __value == "object" ) {
          if(Array.isArray(__value)) {
            let __outArray = [];
            for(const __valueItem of __value) {
              __outArray.push(eval(__varString + __valueItem));
            }
            __value = __outArray;
          } else {
            __value = this.resolveMap(__value, obj, selectedObject, relatedObject);
          }
        }
        __outMap[__key] = __value;
      }
      return __outMap;
    } catch(e) {
      return __inMap;
    }
  }
  
}
