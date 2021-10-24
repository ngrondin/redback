import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable, of, Observer } from 'rxjs';
import { RbObject, RbFile, RbAggregate } from '../datamodel';
import { ErrorService } from './error.service';
import { ClientWSService } from './clientws.service';
import { FilterService } from './filter.service';



@Injectable({
  providedIn: 'root'
})
export class DataService {
  allObjects: RbObject[];
  saveImmediatly: boolean;
  objectCreateObservers: Observer<RbObject>[] = [];

  constructor(
    private apiService: ApiService,
    private clientWSService: ClientWSService,
    private errorService: ErrorService,
    private filterService: FilterService
  ) {
    this.allObjects = [];
    this.saveImmediatly = true;
    this.clientWSService.getObjectUpdateObservable().subscribe(
      json => {
        let rbObject: RbObject = this.updateObjectFromServer(json);
        this.objectCreateObservers.forEach((observer) => {
          observer.next(rbObject);
        });              
      }
    );
  }

  getObjectCreateObservable() : Observable<any>  {
    return new Observable<any>((observer) => {
      this.objectCreateObservers.push(observer);
    });
  }

  clearAllLocalObject() {
    this.allObjects = [];
    this.clientWSService.clearSubscriptions();
    this.objectCreateObservers = []; 
  }

  getLocalObject(objectname: string, uid: string) : RbObject {
   for(const o of this.allObjects) {
      if(o.objectname == objectname && o.uid == uid) {
        return o;
      }
    }
  }

  findFirstLocalObject(objectname: string, filter: any) : RbObject {
    for(const o of this.allObjects) {
      if(o.objectname == objectname && this.filterService.applies(filter, o)) {
        return o;
      }
    }
  }

  
  getServerObject(objectname: string, uid: string) : Observable<RbObject> {
    const getObs =  this.apiService.getObject(objectname, uid);
    const dataObservable = new Observable<RbObject>((observer) => {
      getObs.subscribe(
        resp => {
          const rbObject = this.updateObjectFromServer(resp);
          observer.next(rbObject);
          observer.complete();
        },
        error => this.errorService.receiveHttpError(error)
      );
    })
    return dataObservable; 
  }

  listServerObjects(name: string, filter: any, search: string, sort: any, page: number, pageSize: number, addRelated: boolean) : Observable<any> {
    const apiObservable = this.apiService.listObjects(name, filter, search, sort, page, pageSize, addRelated);
    const dataObservable = new Observable((observer) => {
      apiObservable.subscribe(
        resp => {
          const rbObjectArray = Object.values(resp.list).map(json => this.updateObjectFromServer(json));
          observer.next(rbObjectArray);
          observer.complete();
        }, 
        error => this.errorService.receiveHttpError(error)
      )
    })
    return dataObservable; 
  }

  listServerRelatedObjects(name: string, uid: string, attribute: string, filter: any, search: string, sort: any, addRelated: boolean) : Observable<any> {
    const apiObservable = this.apiService.listRelatedObjects(name, uid, attribute, filter, search, sort, addRelated);
    const dataObservable = new Observable((observer) => {
      apiObservable.subscribe(
        resp => {
          const rbObjectArray = Object.values(resp.list).map(json => this.updateObjectFromServer(json));
          observer.next(rbObjectArray);
          observer.complete();
        },
        error => this.errorService.receiveHttpError(error)
      );
    })
    return dataObservable; 
  }

  loadMissingRelatedObjects(list: RbObject[]) {
    console.log("Add missing related");
    let reqMap = {}
    for(let o of list) {
      for(let attr in o.validation) {
        if(o.data[attr] != null && o.validation[attr].related != null && o.related[attr] == null) {
          let relObjectName = o.validation[attr].related.object;
          let relObjectLink = o.validation[attr].related.link;
          let linkVal = o.data[attr];
          if(reqMap[relObjectName] == null) reqMap[relObjectName] = {};
          if(reqMap[relObjectName][relObjectLink] == null) reqMap[relObjectName][relObjectLink] = [];
          if(reqMap[relObjectName][relObjectLink].indexOf(linkVal) == -1) {
            reqMap[relObjectName][relObjectLink].push(linkVal)
          }
        }
      }
    }
    let multi = [];
    for(let objectname in reqMap) {
      let orList = [];
      for(let link in reqMap[objectname]) {
        let filter = {};
        filter[link] = {"$in": reqMap[objectname][link]};
        orList.push(filter);
      }
      multi.push({
        key: objectname,
        action: "list",
        object: objectname,
        filter: orList.length > 1 ? {"$or": orList} : orList[0],
        options: {addvalidation: false, addrelated: false},
        page: 0,
        pagesize: 500
      })
    }
    if(multi.length > 0) {
      this.apiService.objectMulti(multi).subscribe(
        resp => {
          for(let objectname in resp) {
            for(let objectjson of resp[objectname].list) {
              this.updateObjectFromServer(objectjson);
            }
          }
          for(let object of list) {
            for(let attr in object.validation) {
              if(object.data[attr] != null && object.validation[attr].related != null && object.related[attr] == null) {
                if(object.validation[attr].related.link == 'uid') {
                  object.related[attr] = this.getLocalObject(object.validation[attr].related.object, object.data[attr]);
                } else {
                    let filter = {};
                    filter[object.validation[attr].related.link] = object.data[attr];
                    object.related[attr] = this.findFirstLocalObject(object.validation[attr].related.object, filter);
                }
              }
            }
          }
        },
        error => this.errorService.receiveHttpError(error)
      )
    }
  }


  updateObjectFromServer(json: any) : RbObject {
    let rbObject : RbObject = this.getLocalObject(json.objectname, json.uid);
    if(rbObject != null) {
      rbObject.updateFromServer(json);
    } else {
      rbObject = new RbObject(json, this);
      this.allObjects.push(rbObject);
      this.clientWSService.subscribeToUniqueObjectUpdate(rbObject.objectname, rbObject.uid);
    }
    return rbObject;
  }

  updateObjectToServer(rbObject: RbObject) {
    let upd: any = {};
    for(const attribute of rbObject.changed) {
        upd[attribute] = rbObject.data[attribute];
    }
    this.apiService.updateObject(rbObject.objectname, rbObject.uid, upd).subscribe(
      resp => {
        this.updateObjectFromServer(resp)
      },
      error => {
        this.errorService.receiveHttpError(error);
        rbObject.refresh();
      }
    );
  }

  createObject(name: string, uid: string, data: any) : Observable<RbObject> {
    const apiObservable = this.apiService.createObject(name, uid, data);
    const dataObservable = new Observable<RbObject>((observer) => {
      apiObservable.subscribe(
        resp => {
          const newObj: RbObject = this.updateObjectFromServer(resp);
          observer.next(newObj);
          observer.complete();
        },
        error => this.errorService.receiveHttpError(error)
      );
    })
    return dataObservable;     
  }

  deleteObject(rbObject: RbObject) : Observable<any> {
    const apiObservable = this.apiService.deleteObject(rbObject.objectname, rbObject.uid);
    const dataObservable = new Observable<RbObject>((observer) => {
      apiObservable.subscribe(
        resp => {
          observer.next(resp);
          observer.complete();
        },
        error => this.errorService.receiveHttpError(error)
      );
    })
    return dataObservable;     
  }

  createObjectInMemory(name: string, uid: string, data: any) : Observable<RbObject> {
    return new Observable<RbObject>((observer) => {
      let newObj: RbObject = new RbObject({objectname: name, uid: uid, data: data}, this);
      this.allObjects.push(newObj);
      observer.next(newObj);
      observer.complete();
    });
  }

  executeObjectFunction(rbObject: RbObject, func: string, param: string) : Observable<null> {
    return new Observable<null>((observer) => {
      this.apiService.executeObject(rbObject.objectname, rbObject.uid, func).subscribe(
        resp => {
          if(!this.clientWSService.isConnected()) this.updateObjectFromServer(resp);
          observer.next();
          observer.complete();
        },
        error => {
          this.errorService.receiveHttpError(error);
          observer.error(error);
        }
      );
    });
  }

  
  executeGlobalFunction(func: string, param: any) : Observable<null> {
    return new Observable<null>((observer) => {
      this.apiService.executeGlobal(func, param).subscribe(
        resp => {
          observer.next();
          observer.complete();
        },
        error => {
          this.errorService.receiveHttpError(error);
          observer.error(error);
        }
      );
    });
  }

  aggregateObjects(name: string, filter: any, search: string, tuple: any, metrics: any, base: any, page: number = 0, pageSize: number = 50) : Observable<RbAggregate[]> {
    const apiObservable = this.apiService.aggregateObjects(name, filter, search, tuple, metrics, base, page, pageSize);
    const dataObservable = new Observable<RbAggregate[]>((observer) => {
      apiObservable.subscribe(
        resp => {
          const rbAggregateArray = Object.values(resp.list).map(json => new RbAggregate(json, this));
          observer.next(rbAggregateArray);
          observer.complete();
        }, 
        error => this.errorService.receiveHttpError(error)
      )
    })
    return dataObservable; 
  }

  exportObjects(name: string, filter: any, search: string) : Observable<any> {
    return new Observable<null>((observer) => {
      this.apiService.exportObjects(name, filter, search).subscribe(
        resp => {
          const url = window.URL.createObjectURL(new Blob([...resp]));
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', 'export.csv');
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          observer.next();
          observer.complete();
        },
        error => {
          this.errorService.receiveHttpError(error);
          observer.error(error);
        }
      );
    });
  }


  listFiles(object: string, uid: any) : Observable<RbFile[]> {
    const apiObservable = this.apiService.listFiles(object, uid);
    const fileObservable = new Observable<RbFile[]>((observer) => {
      apiObservable.subscribe(
        resp => {
          const rbFileArray = Object.values(resp.list).map(json => new RbFile(json, this));
          observer.next(rbFileArray);
          observer.complete();
        }, 
        error => this.errorService.receiveHttpError(error)
      )
    })
    return fileObservable; 
  }

  subscribeObjectCreation(id: string, object: String, filter: any) {
    this.clientWSService.subscribeToFilterObjectUpdate(object, filter, id);
  }
}
