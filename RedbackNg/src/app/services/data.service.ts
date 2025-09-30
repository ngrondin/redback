import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable, of, Observer } from 'rxjs';
import { RbObject, RbFile, RbAggregate } from '../datamodel';
import { ErrorService } from './error.service';
import { ClientWSService } from './clientws.service';
import { FilterService } from './filter.service';
import { Hasher } from '../helpers';
import { LogService } from './log.service';



@Injectable({
  providedIn: 'root'
})
export class DataService {
  allObjects: { [objectname: string]: { [uid: string]: RbObject } };
  saveImmediatly: boolean;
  fetchCount: number = 0;
  objectUpdateObservers: Observer<RbObject>[] = [];
  deferredFetchQueue: DeferredFetchQueue = new DeferredFetchQueue();
  runningFinalization: boolean = false;

  constructor(
    private apiService: ApiService,
    private clientWSService: ClientWSService,
    private errorService: ErrorService,
    private filterService: FilterService,
    private logService: LogService
  ) {
    this.allObjects = {};
    this.saveImmediatly = true;
    this.clientWSService.getObjectUpdateObservable().subscribe({
      next: (json) => {
        let list = Array.isArray(json) ? json : [json];
        const rbObjectArray = Object.values(list).map(json => this.receive(json));
        this.finalizeReceipt();
        this.objectUpdateObservers.forEach((observer) => {
          rbObjectArray.forEach(rbObject => observer.next(rbObject)) ;
        });              
      }
    });
  }


  clear() {
    this.allObjects = {};
    this.clientWSService.clearSubscriptions();
    this.objectUpdateObservers = []; 
  }

  get(objectname: string, uid: string) : RbObject {
    if(this.allObjects[objectname] != null) {
      return this.allObjects[objectname][uid];
    }
    return null;
  }

  put(rbObject: RbObject) {
    if(this.allObjects[rbObject.objectname] == null) this.allObjects[rbObject.objectname] = {};
    this.allObjects[rbObject.objectname][rbObject.uid] = rbObject;
  }

  list(objectname: string, filter: any) : RbObject[] {
    var ret = [];
    if(this.allObjects[objectname] != null) {  
      let objects = Object.values(this.allObjects[objectname]);
      for(const o of objects) {
        if(this.filterService.applies(filter, o)) {
          ret.push(o);
        }
      }
    }
    return ret;
  }

  findFirst(objectname: string, filter: any) : RbObject {
    if(this.allObjects[objectname] != null) {  
      let objects = Object.values(this.allObjects[objectname]);
      for(const o of objects) {
        if(this.filterService.applies(filter, o)) {
          return o;
        }
      }
    }
    return null;
  }

  fetch(objectname: string, uid: string) : Observable<RbObject> {
    return new Observable<RbObject>((observer) => {
      this.fetchCount++;
      this.apiService.getObject(objectname, uid).subscribe({
        next: resp => {
          const rbObject = this.receive(resp);
          observer.next(rbObject);
        },
        complete: () => {
          observer.complete();
          this.fetchCount--;
          this.finalizeReceipt();
        },
        error: error => {
          this.errorService.receiveHttpError(error)
          observer.error(error);
          this.fetchCount--;
        }
      });
    });
  }

  fetchFirst(name: string, filter: any, search: string, sort: any) : Observable<RbObject> {
    return new Observable((observer) => {
      this.fetchCount++;
      this.apiService.listObjects(name, filter, search, sort, 0, 1, false).subscribe({
        next: resp => {
          const rbObjectArray = Object.values(resp.list).map(json => this.receive(json));
          observer.next(rbObjectArray.length == 1 ? rbObjectArray[0] : null);
        }, 
        complete: () => {
          observer.complete();      
          this.fetchCount--;      
          this.finalizeReceipt();
        },
        error: error => {
          this.errorService.receiveHttpError(error)
          observer.error(error);
          this.fetchCount--;
        }
      })
    });
  }

  fetchList(name: string, filter: any, search: string, sort: any, page: number, pageSize: number, addRelated: boolean) : Observable<any> {
    return new Observable((observer) => {
      this.fetchCount++;
      this.apiService.listObjects(name, filter, search, sort, page, pageSize, false).subscribe({
        next: resp => {
          const rbObjectArray = Object.values(resp.list).map(json => this.receive(json));
          observer.next(rbObjectArray);
        }, 
        complete: () => {
          observer.complete();
          this.fetchCount--;
          this.finalizeReceipt();
        },
        error: error => {
          this.errorService.receiveHttpError(error)
          observer.error(error);
          this.fetchCount--;
        }
      })
    });
  }

  fetchEntireList(name: string, filter: any, search: string, sort: any) : Observable<any> {
    return new Observable((observer) => {
      this.fetchCount++;
      if(this.apiService.canStream()) {
        this.apiService.streamObjects(name, filter, search, sort).subscribe({
          next: resp => {
            const rbObjectArray = Object.values(resp.result).map(json => this.receive(json));
            observer.next(rbObjectArray);
          },
          complete: () => {
            observer.complete();
            this.fetchCount--;
            this.finalizeReceipt();
          },
          error: error => {
            this.errorService.receiveHttpError(error)
            observer.error(error);
            this.fetchCount--;
          }
        });
      } else {
        this._fetchEntireList(observer, name, filter, search, sort, 0, 500);
      }
    });
  }

  _fetchEntireList(observer: any, name: string, filter: any, search: string, sort: any, page: number, pageSize: number)  {
    this.apiService.listObjects(name, filter, search, sort, page, pageSize, false).subscribe({
      next: resp => {
        const rbObjectArray = Object.values(resp.list).map(json => this.receive(json));
        observer.next(rbObjectArray);
        if(resp.list.length == pageSize) {
          this._fetchEntireList(observer, name, filter, search, sort, page + 1, pageSize);
        } else {
          observer.complete();
          this.fetchCount--;
          this.finalizeReceipt();
        }
      }, 
      error: error => {
        this.errorService.receiveHttpError(error)
        observer.error(error);
        this.fetchCount--;
      }
    })
  } 

  fetchRelatedList(name: string, uid: string, attribute: string, filter: any, search: string, sort: any, addRelated: boolean) : Observable<any> {
    return new Observable((observer) => {
      this.apiService.listRelatedObjects(name, uid, attribute, filter, search, sort, false).subscribe({
        next: resp => {
          const rbObjectArray = Object.values(resp.list).map(json => this.receive(json));
          observer.next(rbObjectArray);
        },
        complete: () => {
          observer.complete();
          this.fetchCount--;
          this.finalizeReceipt();
        },
        error: error => {
          this.errorService.receiveHttpError(error)
          observer.error(error);
          this.fetchCount--;
        }
      });
    });
  }

  enqueueDeferredFetch(name: string, uid: string, callback: DefferredCallback) {
    this.deferredFetchQueue.get(name).addUid(uid, callback);
  }

  enqueueDeferredFetchList(name: string, filter: any, callback: DefferredCallback) {
    this.deferredFetchQueue.get(name).addFilter(filter, callback);
  }

  receive(json: any) : RbObject {
    let rbObject : RbObject = this.get(json.objectname, json.uid);
    if(rbObject != null) {
      rbObject.updateFromJSON(json);
    } else {
      rbObject = new RbObject(json);
      this.put(rbObject);
      this.clientWSService.subscribeToUniqueObjectUpdate(rbObject.objectname, rbObject.uid);
    }
    return rbObject;
  }

  async finalizeReceipt() {
    if(!this.runningFinalization) {
      this.runningFinalization = true;
      let objectnames = Object.keys(this.deferredFetchQueue.objects);
      if(objectnames.length > 0) {
        let objectname = objectnames[0];
        let deferredObject = this.deferredFetchQueue.get(objectname);
        this.logService.debug("Executing deferred request " + objectname + ":" + deferredObject.id);
        let filter = null;
        let count = 0;
        let uids = Object.keys(deferredObject.uids);
        if(uids.length > 0) {
          filter = {uid:{$in: uids}};
          count += uids.length;
        }
        if(deferredObject.filters.length > 0) {
          let subfilter = deferredObject.filters.map(f => f.data);
          filter = {$or: (filter != null ? subfilter.concat(filter) : subfilter)};  
          count += deferredObject.filters.length * 2; //Times 2 in order to allow for domain overridden objects
        }
        this.deferredFetchQueue.clear(objectname);
        this.apiService.streamObjects(objectname, filter, null, null).subscribe(
          {
            next: resp => {
              resp.result.forEach(json => {
                let obj = this.receive(json);
                let callbacks = [];
                let deferredUid = deferredObject.uids[obj.uid];
                if(deferredUid != null) callbacks.push(...deferredUid.callbacks);
                let deferredFilters = deferredObject.filters.filter(f => this.filterService.applies(f.data, obj));
                deferredFilters.forEach(df => callbacks.push(...df.callbacks));
                callbacks.forEach(cb => cb(obj));
              });
            },
            complete: () => {
              this.logService.debug("Finalized for deferred object " + objectname + ":" + deferredObject.id);
              this.runningFinalization = false;
              this.finalizeReceipt();
            },
            error: error => {
              this.logService.error("Error finalizing receipt for " + objectname + " :" + error);
              this.runningFinalization = false;
            }
          }
        );
      } else {
        this.clientWSService.sendUnsentSubscriptionRequests(); 
        this.runningFinalization = false;
      } 
    }
  }

  pushToServer(rbObject: RbObject) {
    let upd: any = {};
    for(const attribute of rbObject.updatedAttributes) {
        upd[attribute] = rbObject.data[attribute];
    }
    this.apiService.updateObject(rbObject.objectname, rbObject.uid, upd).subscribe(
      resp => {
        this.receive(resp)
      },
      error => {
        this.errorService.receiveHttpError(error);
        rbObject.refresh();
      }
    );
  }

  create(name: string, uid: string, data: any) : Observable<RbObject> {
    const apiObservable = this.apiService.createObject(name, uid, data);
    const dataObservable = new Observable<RbObject>((observer) => {
      apiObservable.subscribe(
        resp => {
          const newObj: RbObject = this.receive(resp);
          observer.next(newObj);
          observer.complete();
        },
        error => this.errorService.receiveHttpError(error)
      );
    })
    return dataObservable;     
  }

  delete(rbObject: RbObject) : Observable<any> {
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

  createInMemory(name: string, uid: string, data: any) : Observable<RbObject> {
    return new Observable<RbObject>((observer) => {
      let newObj: RbObject = new RbObject({objectname: name, uid: uid, data: data});
      this.put(newObj);
      observer.next(newObj);
      observer.complete();
    });
  }

  executeObjectFunction(rbObject: RbObject, func: string, param: string, timeout: number) : Observable<null> {
    return new Observable<null>((observer) => {
      this.apiService.executeObject(rbObject.objectname, rbObject.uid, func, param, timeout).subscribe(
        resp => {
          if(!this.clientWSService.isConnected()) this.receive(resp);
          observer.next(null);
          observer.complete();
        },
        error => {
          this.errorService.receiveHttpError(error);
          observer.error(error);
        }
      );
    });
  }
  
  executeGlobalFunction(func: string, param: any, timeout: number) : Observable<null> {
    return new Observable<null>((observer) => {
      this.apiService.executeGlobal(func, param, timeout).subscribe(
        resp => {
          observer.next(null);
          observer.complete();
        },
        error => {
          this.errorService.receiveHttpError(error);
          observer.error(error);
        }
      );
    });
  }

  aggregate(name: string, filter: any, search: string, tuple: any, metrics: any, base: any, page: number = 0, pageSize: number = 50) : Observable<RbAggregate[]> {
    return new Observable<RbAggregate[]>((observer) => {
      this.fetchCount++;
      this.apiService.aggregateObjects(name, filter, search, tuple, metrics, base, page, pageSize).subscribe({
        next: (resp) => {
          const rbAggregateArray = Object.values(resp.list).map(json => new RbAggregate(json, this));
          observer.next(rbAggregateArray);
        }, 
        complete: () => {
          observer.complete();
          this.fetchCount--;
          this.finalizeReceipt();
        },
        error: (error) => {
          this.errorService.receiveHttpError(error);
          observer.error(error);
          this.fetchCount--;
        }
      })
    })
  }

  export(name: string, filter: any, search: string) : Observable<any> {
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
          observer.next(null);
          observer.complete();
        },
        error => {
          this.errorService.receiveHttpError(error);
          observer.error(error);
        }
      );
    });
  }

  subscribeToCreation(id: string, object: String, filter: any) {
    this.clientWSService.subscribeToFilterObjectUpdate(object, filter, id);
  }

  getObjectUpdateObservable() : Observable<any>  {
    return new Observable<any>((observer) => {
      this.objectUpdateObservers.push(observer);
    });
  }


}

type DefferredCallback = (object: RbObject) => void;
let objId = 0;

class DeferredUid {
  callbacks: DefferredCallback[] = [];

  constructor() {
  }
}

class DeferredFilter {
  data: any;
  hash: number;
  callbacks: DefferredCallback[] = [];

  constructor(d: string, h: number) {
    this.data = d;
    this.hash = h;
  }
}

class DefferedObject {
  id: number = objId++;
  uids: {[key: string]: DeferredUid} = {};
  filters: DeferredFilter[] = []; 

  addUid(uid: string, callback: DefferredCallback) {
    let deferredUid = this.uids[uid];
    if(deferredUid == null) {
      deferredUid = new DeferredUid();
      this.uids[uid] = deferredUid;
    }
    deferredUid.callbacks.push(callback);
  }

  addFilter(filter: any, callback: DefferredCallback) {
    let filterHash = Hasher.hash(filter);
    let deferredFilter = this.filters.find(f => f.hash == filterHash);
    if(deferredFilter == null) {
      deferredFilter = new DeferredFilter(filter, filterHash);
      this.filters.push(deferredFilter);
    }
    deferredFilter.callbacks.push(callback);
  }


}

class DeferredFetchQueue {
  objects: {[key: string]: DefferedObject} = {};
  
  get(name: string): DefferedObject {
    if(this.objects[name] == null) this.objects[name] = new DefferedObject();
    return this.objects[name];
  }

  clear(name: string) {
    if(this.objects[name] != null) {
      delete this.objects[name];
    }
  }
}