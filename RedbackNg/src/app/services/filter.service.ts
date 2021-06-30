import { Injectable } from '@angular/core';
import { RbObject } from 'app/datamodel';

@Injectable({
  providedIn: 'root'
})
export class FilterService {
  isoDateRegExp: RegExp = /^(?:[1-9]\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\d|2[0-3]):[0-5]\d:[0-5]\d(?:\.\d{1,9})?(?:Z|[+-][01]\d:[0-5]\d)$/;
  
  constructor() { }

  public mergeFilters(map1: any, map2: any) : any {
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



  public resolveFilter(__inMap: any, obj: RbObject, selectedObject: RbObject, relatedObject: RbObject) : any {
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

      function evalValue(__value) {
        var ret = null;
        try { ret = eval(__varString + __value); } catch(err) {}
        return ret;
      }

      for (const __key in __inMap) {
        let __value = __inMap[__key];
        if(__value == null) {
          __value = null;
        } else if(typeof __value == "string") {
          __value = evalValue(__value);
        } else if(typeof __value == "object" ) {
          if(Array.isArray(__value)) {
            let __outArray = [];
            for(const __valueItem of __value) {
              if(typeof __valueItem == "object") {
                __outArray.push(this.resolveFilter(__valueItem, obj, selectedObject, relatedObject));
              } else if(typeof __valueItem == "string") {
                __outArray.push(evalValue(__valueItem));
              }
            }
            __value = __outArray;
          } else {
            __value = this.resolveFilter(__value, obj, selectedObject, relatedObject);
          }
        }
        __outMap[__key] = __value;
      }
      return __outMap;
    } catch(e) {
      return __inMap;
    }
  }

  applies(filter: any, object: RbObject) : boolean {
    let ret: boolean = true;
    for(let key in filter) {
      let fVal = filter[key];
      if(key == '$or') {
        let subRet: boolean = false;
        for(let fSubVal of fVal) {
          if(this.applies(fSubVal, object)) subRet = true;
        }
        if(subRet == false) ret = false;
      } else if(key == '$and') {
        for(let fSubVal of fVal) {
          if(!this.applies(fSubVal, object)) ret = false;
        }
      } else {
        let oVal = object.get(key);
        if(typeof fVal == 'object') {
          if(fVal["$in"] != null) {
            let fSubVal = fVal["$in"];
            let subRet = false;
            for(let fSubValItem of fSubVal) {
              if(this.valueAreEqual(fSubValItem, oVal)) subRet = true;
            }
            if(subRet == false) ret = false;
          }
          if(fVal["$nin"] != null) {
            let fSubVal = fVal["$nin"];
            for(let fSubValItem of fSubVal) {
              if(this.valueAreEqual(fSubValItem, oVal)) ret = false;
            }
          }
          if(fVal["$eq"] != null) {
            if(!this.valueAreEqual(fVal["$eq"], oVal)) ret = false;
          }
          if(fVal["$ne"] != null) {
            if(this.valueAreEqual(fVal["$ne"], oVal)) ret = false;
          }
          if(fVal["$gt"] != null) {
            let fSubVal = fVal["$gt"];
            let oValNum = oVal.getTime != null ? oVal.getTime() : this.isoDateRegExp.test(oVal) ? new Date(oVal).getTime() : parseFloat(oVal);
            let fSubValNum = fSubVal.getTime != null ? fSubVal.getTime() : this.isoDateRegExp.test(fSubVal) ? new Date(fSubVal).getTime() : parseFloat(fSubVal);
            if(oValNum <= fSubValNum) ret = false;
          }
          if(fVal["$lt"] != null) {
            let fSubVal = fVal["$lt"];
            let oValNum = oVal.getTime != null ? oVal.getTime() : this.isoDateRegExp.test(oVal) ? new Date(oVal).getTime() : parseFloat(oVal);
            let fSubValNum = fSubVal.getTime != null ? fSubVal.getTime() : this.isoDateRegExp.test(fSubVal) ? new Date(fSubVal).getTime() : parseFloat(fSubVal);
            if(oValNum >= fSubValNum) ret = false;            
          }
          if(fVal["$regex"] != null) {
            let fSubVal = fVal["$regex"];
            let expr = eval(fSubVal);
            if(!expr.test(oVal)) ret = false;
          }
        } else {
          if(!this.valueAreEqual(fVal, oVal)) ret = false;
        }
      }
    }
    return ret;
  }

  valueAreEqual(v1: any, v2: any) : boolean {
    if(Array.isArray(v1) && Array.isArray(v2)) {
      return v1.length == v2.length && v1.every((val, index) => val === v2[index]);
    } else if(Array.isArray(v1)) {
      return v1.indexOf(v2) > -1;
    } else if(Array.isArray(v2)) {
      return v2.indexOf(v1) > -1;
    } else {
      return v1 == v2;
    }
  }

  convertToData(filter: any) {
    let data: any = {};
    for(let key in filter) {
      if(!key.startsWith("$") && key != "uid") {
        let val: any = filter[key];
        let dataval = (val !== null && typeof val == 'object' ? this.convertToData(val) : val);
        if(!(dataval !== null && typeof dataval == 'object' && Object.keys(dataval).length == 0)) {
          data[key] = dataval;
        } 
      }
    }
    return data;
  }
  
}
