import { Injectable } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { UserprefService } from './userpref.service';
import { LogService } from './log.service';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';

@Injectable({
  providedIn: 'root'
})
export class FilterService {
  isoDateRegExp: RegExp = /^(?:[1-9]\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\d|2[0-3]):[0-5]\d:[0-5]\d(?:\.\d{1,9})?(?:Z|[+-][01]\d:[0-5]\d)$/;
  
  constructor(
    private userPrefService: UserprefService,
    private logService: LogService
  ) { }

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



  public resolveFilter(__inMap: any, obj: RbObject, dataset?: RbDatasetComponent, relatedObject?: RbObject, relatedDataset?: RbDatasetComponent, extraContext?: any) : any {
    try {
      let params: string[] = ["obj", "object", "selectedObject", "dataset", "relatedObject", "relatedDataset", "userpref", "uid"];
      let args: any[] = [obj, obj, obj, dataset, relatedObject, relatedDataset, this.userPrefService];

      if(obj != null && typeof obj != 'undefined') {
        args.push(obj.uid);
        for(const attr in obj.data) {
          params.push(attr);
          args.push(obj.data[attr]);
        }
      } else {
        args.push(null);
      }

      if(extraContext != null && typeof extraContext != 'undefined') {
        for(const key in extraContext) {
          params.push(key);
          args.push(extraContext[key]);
        }
      }

      return this._resolveFilter(__inMap, params, args);
    } catch(e) {
      return __inMap;
    }
  }

  private _resolveFilter(__inMap: any, params: string[], args: any[]) : any {
    let __outMap: any = {};
    for (const __key in __inMap) {
      let __value = __inMap[__key];
      if(__value == null) {
        __value = null;
      } else if(typeof __value == "string") {
        __value = this.evalExpression(__value, params, args);
      } else if(typeof __value == "object" ) {
        if(Array.isArray(__value)) {
          let __outArray = [];
          for(const __valueItem of __value) {
            if(typeof __valueItem == "object") {
              __outArray.push(this._resolveFilter(__valueItem, params, args));
            } else if(typeof __valueItem == "string") {
              __outArray.push(this.evalExpression(__valueItem, params, args));
            }
          }
          __value = __outArray;
        } else {
          __value = this._resolveFilter(__value, params, args);
        }
      }
      __outMap[__key] = __value;
    }
    return __outMap;
  }

public unresolveFilter(__inMap: any) : any {
  let __outMap: any = {};
  for (const __key in __inMap) {
    let __value = __inMap[__key];
    if(__value == null) {
      __value = null;
    } else if(typeof __value == "string") {
      __value = "'" + __value + "'";
    } else if(typeof __value == "object" ) {
      if(Array.isArray(__value)) {
        let __outArray = [];
        for(const __valueItem of __value) {
          if(typeof __valueItem == "object") {
            __outArray.push(this.unresolveFilter(__valueItem));
          } else if(typeof __valueItem == "string") {
            __outArray.push("'" + __valueItem + "'");
          }
        }
        __value = __outArray;
      } else {
        __value = this.unresolveFilter(__value);
      }
    }
    __outMap[__key] = __value;
  }
  return __outMap;
}
  
  private evalExpression(expr, params: string[], args: any[]): any {
    var ret = null;
    if(expr != null && expr != "") {
      try { 
        let source = "return (" + expr + ")"; 
        let func = Function(...[...params, source]);
        ret = func.call(window.redback, ...args); 
      } catch(err) {
        this.logService.error("Error evaluating expression :" + err);
      }  
    }
    return ret;
  }

  public applies(filter: any, object: RbObject) : boolean {
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
        let unresolvedRelated = (oVal == null && key.indexOf('.') > -1 && object.get(key.substring(0, key.indexOf('.'))) != null);
        if(!unresolvedRelated) { //Filter applies by default when a related is not yet resolved
          if(typeof fVal == 'object' && fVal !== null) {
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
              let oValNum = oVal != null && oVal.getTime != null ? oVal.getTime() : this.isoDateRegExp.test(oVal) ? new Date(oVal).getTime() : parseFloat(oVal);
              let fSubValNum = fSubVal.getTime != null ? fSubVal.getTime() : this.isoDateRegExp.test(fSubVal) ? new Date(fSubVal).getTime() : parseFloat(fSubVal);
              if(oValNum <= fSubValNum) ret = false;
            }
            if(fVal["$lt"] != null) {
              let fSubVal = fVal["$lt"];
              let oValNum = oVal != null && oVal.getTime != null ? oVal.getTime() : this.isoDateRegExp.test(oVal) ? new Date(oVal).getTime() : parseFloat(oVal);
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

  convertToData(filterPart: any) {
    if(typeof filterPart == "undefined") {
      return undefined;
    } else if(filterPart !== null && Array.isArray(filterPart)) {
      let out: any[] = [];
      for(let item of filterPart) {
        let val = this.convertToData(item);
        if(typeof val != "undefined") out.push(val);
      }
      return out;
    } else if(filterPart !== null && typeof filterPart == 'object') {
      let out: any = {};
      for(let key in filterPart) {
        if(!key.startsWith("$") && key != "uid") {
          let val = this.convertToData(filterPart[key]);
          if(typeof val != "undefined") out[key] = val;
        }
      }
      if(Object.keys(out).length == 0) return undefined;
      else return out;
    } else {
      return filterPart;
    }
  }

  prefixDollarSign(filterPart: any) {
    if(typeof filterPart == "undefined") {
      return undefined;
    } else if(filterPart !== null && Array.isArray(filterPart)) {
      let out: any[] = [];
      for(let item of filterPart) {
        out.push(this.prefixDollarSign(item));
      }
      return out;
    } else if(filterPart !== null && typeof filterPart == 'object') {
      let out: any = {};
      for(let key in filterPart) {
        let newKey = (key.startsWith("$") ? '_' : '') + key;
        out[newKey] = this.prefixDollarSign(filterPart[key]);
      }
      return out;
    } else {
      return filterPart;
    }
  }

  removePrefixDollarSign(filterPart: any) {
    if(typeof filterPart == "undefined") {
      return undefined;
    } else if(filterPart !== null && Array.isArray(filterPart)) {
      let out: any[] = [];
      for(let item of filterPart) {
        out.push(this.removePrefixDollarSign(item));
      }
      return out;
    } else if(filterPart !== null && typeof filterPart == 'object') {
      let out: any = {};
      for(let key in filterPart) {
        let newKey = key.startsWith("_$") ? key.substring(1): key;
        out[newKey] = this.removePrefixDollarSign(filterPart[key]);
      }
      return out;
    } else {
      return filterPart;
    }
  }


  
}