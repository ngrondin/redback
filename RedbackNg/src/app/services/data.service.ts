import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable, of, Observer } from 'rxjs';
import { RbObject, RbFile, RbAggregate } from '../datamodel';
import { ErrorService } from './error.service';
import { ClientWSService } from './clientws.service';
import { FilterService } from './filter.service';
import { Hasher } from '../helpers';



@Injectable({
  providedIn: 'root'
})
export class DataService {
  allObjects: { [key: string]: RbObject[] };
  saveImmediatly: boolean;
  objectCreateObservers: Observer<RbObject>[] = [];
  deferredFetchQueue: DeferredFetchQueue = new DeferredFetchQueue();

  constructor(
    private apiService: ApiService,
    private clientWSService: ClientWSService,
    private errorService: ErrorService,
    private filterService: FilterService
  ) {
    this.allObjects = {};
    this.saveImmediatly = true;
    this.clientWSService.getObjectUpdateObservable().subscribe(
      json => {
        //console.log("Received pushed update");
        let rbObject: RbObject = this.receive(json);
        this.finalizeReceipt();
        this.objectCreateObservers.forEach((observer) => {
          observer.next(rbObject);
        });              
      }
    );
  }


  clear() {
    this.allObjects = {};
    this.clientWSService.clearSubscriptions();
    this.objectCreateObservers = []; 
  }

  get(objectname: string, uid: string) : RbObject {
    if(this.allObjects[objectname] != null) {
      for(const o of this.allObjects[objectname]) {
        if(o.uid == uid) {
          return o;
        }
      }
    }
    return null;
  }

  findFirst(objectname: string, filter: any) : RbObject {
    if(this.allObjects[objectname] != null) {  
      for(const o of this.allObjects[objectname]) {
        if(this.filterService.applies(filter, o)) {
          return o;
        }
      }
    }
    return null;
  }

  fetch(objectname: string, uid: string) : Observable<RbObject> {
    return new Observable<RbObject>((observer) => {
      this.apiService.getObject(objectname, uid).subscribe(
        resp => {
          const rbObject = this.receive(resp);
          this.finalizeReceipt();
          observer.next(rbObject);
          observer.complete();
        },
        error => {
          this.errorService.receiveHttpError(error)
          observer.error(error);
        }
      );
    });
  }

  fetchList(name: string, filter: any, search: string, sort: any, page: number, pageSize: number, addRelated: boolean) : Observable<any> {
    return new Observable((observer) => {
      //console.log((new Date()).getTime() + " Requesting list " + name);
      this.apiService.listObjects(name, filter, search, sort, page, pageSize, false).subscribe(
        resp => {
          //console.log((new Date()).getTime() + " Received list");
          const rbObjectArray = Object.values(resp.list).map(json => this.receive(json));
          this.finalizeReceipt();
          observer.next(rbObjectArray);
          observer.complete();
        }, 
        error => {
          this.errorService.receiveHttpError(error)
          observer.error(error);
        }
      )
    });
  }

  fetchEntireList(name: string, filter: any, search: string, sort: any) : Observable<any> {
    return new Observable((observer) => {
      //console.log((new Date()).getTime() + " Requesting entire list " + name);
      if(this.apiService.canStream()) {
        this.apiService.streamObjects(name, filter, search, sort, false).subscribe(
          resp => {
            const rbObjectArray = Object.values(resp.result).map(json => this.receive(json));
            observer.next(rbObjectArray);
          },
          error => {
            this.errorService.receiveHttpError(error)
            observer.error(error);
          },
          () => {
            this.finalizeReceipt();
            observer.complete();
          }
        );
      } else {
        this._fetchEntireList(observer, name, filter, search, sort, 0, 500);
      }
    });
  }

  _fetchEntireList(observer: any, name: string, filter: any, search: string, sort: any, page: number, pageSize: number)  {
    this.apiService.listObjects(name, filter, search, sort, page, pageSize, false).subscribe(
      resp => {
        const rbObjectArray = Object.values(resp.list).map(json => this.receive(json));
        observer.next(rbObjectArray);
        if(resp.list.length == pageSize) {
          this._fetchEntireList(observer, name, filter, search, sort, page + 1, pageSize);
        } else {
          //console.log((new Date()).getTime() + " Received entire list");
          this.finalizeReceipt();
          observer.complete();
        }
      }, 
      error => {
        this.errorService.receiveHttpError(error)
        observer.error(error);
      }
    )
  } 

  fetchRelatedList(name: string, uid: string, attribute: string, filter: any, search: string, sort: any, addRelated: boolean) : Observable<any> {
    return new Observable((observer) => {
      this.apiService.listRelatedObjects(name, uid, attribute, filter, search, sort, false).subscribe(
        resp => {
          const rbObjectArray = Object.values(resp.list).map(json => this.receive(json));
          this.finalizeReceipt();
          observer.next(rbObjectArray);
          observer.complete();
        },
        error => {
          this.errorService.receiveHttpError(error)
          observer.error(error);
        }
      );
    });
  }

  enqueueDeferredFetch(name: string, uid: string, forRelatedObject: RbObject) {
    if(uid != null && this.deferredFetchQueue.get(name).uids.indexOf(uid) == -1) {
      this.deferredFetchQueue.get(name).uids.push(uid);
    }
    this.deferredFetchQueue.addObject(forRelatedObject);
  }

  enqueueDeferredFetchList(name: string, filter: any, forRelatedObject: RbObject) {
    let filterHash = Hasher.hash(filter);
    if(filter != null && this.deferredFetchQueue.get(name).filters.find(f => f.hash == filterHash) == null) {
      this.deferredFetchQueue.get(name).filters.push(new DeferredFilter(filter, filterHash));
    }
    this.deferredFetchQueue.addObject(forRelatedObject);
  }

  finalizeReceipt() {
    let multi = [];
    for(let objectname of Object.keys(this.deferredFetchQueue.items)) {
      let fetchRequest = this.deferredFetchQueue.get(objectname);
      let filter = null;
      let count = 0;
      if(fetchRequest.uids.length > 0) {
        filter = {uid:{$in: fetchRequest.uids}};
        count += fetchRequest.uids.length;
      }
      if(fetchRequest.filters.length > 0) {
        let subfilter = fetchRequest.filters.map(f => f.data);
        filter = {$or: (filter != null ? subfilter.concat(filter) : subfilter)};  
        count += fetchRequest.filters.length;
      }
      multi.push({
        key: objectname,
        action: "list",
        object: objectname,
        filter: filter,
        page: 0,
        pagesize: count,
        options: {addvalidation: true, addrelated: false}
      });
    }
    let objects = [...this.deferredFetchQueue.objects];
    this.deferredFetchQueue.clear();
    if(multi.length > 0) {
      //console.log((new Date()).getTime() + " Fetching enqueued: " + multi.map(i => i.key).join(','));
      this.apiService.objectMulti(multi).subscribe(
        resp => {
          //console.log((new Date()).getTime() + " Received related multifetch");
          for(var objectname of Object.keys(resp)) {
            const rbObjectArray = Object.values(resp[objectname].list).map(json => this.receive(json));
          }
          //console.log((new Date()).getTime() + " Re-running linkMissingRelated");
          for(var object of objects) {
            object.linkMissingRelated();
          }
          //console.log((new Date()).getTime() + " Finished linkMissingRelated");
          this.finalizeReceipt();

        }
      );
    }
  }

  receive(json: any) : RbObject {
    let rbObject : RbObject = this.get(json.objectname, json.uid);
    if(rbObject != null) {
      rbObject.updateFromJSON(json);
    } else {
      rbObject = new RbObject(json, this);
      if(this.allObjects[rbObject.objectname] == null) this.allObjects[rbObject.objectname] = [];
      this.allObjects[rbObject.objectname].push(rbObject);
      this.clientWSService.subscribeToUniqueObjectUpdate(rbObject.objectname, rbObject.uid);
    }
    return rbObject;
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
      let newObj: RbObject = new RbObject({objectname: name, uid: uid, data: data}, this);
      if(this.allObjects[name] == null) this.allObjects[name] = [];
      this.allObjects[name].push(newObj);
      observer.next(newObj);
      observer.complete();
    });
  }

  executeObjectFunction(rbObject: RbObject, func: string, param: string) : Observable<null> {
    return new Observable<null>((observer) => {
      this.apiService.executeObject(rbObject.objectname, rbObject.uid, func).subscribe(
        resp => {
          if(!this.clientWSService.isConnected()) this.receive(resp);
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
  
  executeGlobalFunction(func: string, param: any, timeout: number) : Observable<null> {
    return new Observable<null>((observer) => {
      this.apiService.executeGlobal(func, param, timeout).subscribe(
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

  aggregate(name: string, filter: any, search: string, tuple: any, metrics: any, base: any, page: number = 0, pageSize: number = 50) : Observable<RbAggregate[]> {
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

  subscribeToCreation(id: string, object: String, filter: any) {
    this.clientWSService.subscribeToFilterObjectUpdate(object, filter, id);
  }

  getCreationObservable() : Observable<any>  {
    return new Observable<any>((observer) => {
      this.objectCreateObservers.push(observer);
    });
  }


}



class DeferredFilter {
  data: any;
  hash: number;

  constructor(d: string, h: number) {
    this.data = d;
    this.hash = h;
  }
}

class DeferredFetchQueueItem {
  uids: string[] = []; 
  filters: DeferredFilter[] = []; 
}

class DeferredFetchQueue {
  items: {[key: string]: DeferredFetchQueueItem} = {};
  objects: RbObject[] = [];

  get(name: string): DeferredFetchQueueItem {
    if(this.items[name] == null) this.items[name] = new DeferredFetchQueueItem();
    return this.items[name];
  }

  addObject(obj: RbObject) {
    if(this.objects.indexOf(obj) == -1) {
      this.objects.push(obj);
    }
  }

  clear() {
    this.items = {};
    this.objects = [];
  }
}