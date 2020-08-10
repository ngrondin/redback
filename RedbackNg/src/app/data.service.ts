import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable, of, Observer } from 'rxjs';
import { ObjectResp, RbObject, RbFile, RbAggregate } from './datamodel';
import { ToastrService } from 'ngx-toastr';



@Injectable({
  providedIn: 'root'
})
export class DataService {
  allObjects: RbObject[];
  saveImmediatly: boolean;
  objectCreateObservers: Observer<RbObject>[] = [];

  constructor(
    private apiService: ApiService,
    private toastr: ToastrService
  ) {
    this.allObjects = [];
    this.saveImmediatly = true;
    this.apiService.getSignalObservable().subscribe(
      json => {
        if(json.type == 'objectcreate') {
          let rbObject: RbObject = this.updateObjectFromServer(json.object);
          this.objectCreateObservers.forEach((observer) => {
            observer.next(rbObject);
          });      
        } else if(json.type == 'objectupdate') {
          this.updateObjectFromServer(json.object);
        }
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
    this.apiService.clearSubscriptions();
    this.objectCreateObservers = []; 
  }

  getLocalObject(objectname: string, uid: string) : RbObject {
    let rbObject: RbObject = null;
    for(const o of this.allObjects) {
      if(o.objectname == objectname && o.uid == uid) {
        rbObject = o;
      }
    }
    return rbObject;
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
        error => {
          this.toastr.error(error.headers.status, error.error.error, {disableTimeOut: true});
        }
      );
    })
    return dataObservable; 
  }

  listObjects(name: string, filter: any, search: string, sort: any, page: number, pageSize: number) : Observable<any> {
    const apiObservable = this.apiService.listObjects(name, filter, search, sort, page, pageSize);
    const dataObservable = new Observable((observer) => {
      apiObservable.subscribe(
        resp => {
          const rbObjectArray = Object.values(resp.list).map(json => this.updateObjectFromServer(json));
          observer.next(rbObjectArray);
          observer.complete();
        }, 
        error => {
          this.toastr.error(error.headers.status, error.error.error, {disableTimeOut: true});
        }
      )
    })
    return dataObservable; 
  }

  listRelatedObjects(name: string, uid: string, attribute: string, filter: any, search: string) : Observable<any> {
    const apiObservable = this.apiService.listRelatedObjects(name, uid, attribute, filter, search);
    const dataObservable = new Observable((observer) => {
      apiObservable.subscribe(
        resp => {
          const rbObjectArray = Object.values(resp.list).map(json => this.updateObjectFromServer(json));
          observer.next(rbObjectArray);
          observer.complete();
        },
        error => {
          this.toastr.error(error.headers.status, error.error.error, {disableTimeOut: true});
        }
      );
    })
    return dataObservable; 
  }


  updateObjectFromServer(json: any) : RbObject {
    if(json.related != null) {
      for(const a in json.related) {
        const relatedJson = json.related[a];
        this.updateObjectFromServer(relatedJson);
      }
    }

    let rbObject : RbObject = this.getLocalObject(json.objectname, json.uid);
    if(rbObject != null) {
      rbObject.updateFromServer(json);
    } else {
      rbObject = new RbObject(json, this);
      this.allObjects.push(rbObject);
      this.apiService.subscribeToSignal({
        "type":"objectupdate",
        "objectname": rbObject.objectname,
        "uid": rbObject.uid
      });
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
        this.toastr.error(error.headers.status, error.error.error, {disableTimeOut: true});
      });
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
        error => {
          this.toastr.error(error.headers.status, error.error.error, {disableTimeOut: true});
        }
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
        error => {
          this.toastr.error(error.headers.status, error.error.error, {disableTimeOut: true});
        }
      );
    })
    return dataObservable;     
  }

  createObjectInMemory(name: string, uid: string, data: any) : Observable<RbObject> {
    const dataObservable = new Observable<RbObject>((observer) => {
      let newObj: RbObject = new RbObject({objectname: name, uid: uid, data: data}, this);
      this.allObjects.push(newObj);
      observer.next(newObj);
      observer.complete();
    });
    return dataObservable;  
  }

  executeObject(rbObject: RbObject, func: string, param: string) {
    this.apiService.executeObject(rbObject.objectname, rbObject.uid, func).subscribe(
      resp => {
        !this.apiService.SignalWebsocketConnected() ? this.updateObjectFromServer(resp) : null
      },
      error => {
        this.toastr.error(error.headers.status, error.error.error, {disableTimeOut: true});
      }      
    );
  }

  
  executeGlobal(func: string, param: string) {
    this.apiService.executeGlobal(func).subscribe(
      resp => {
        null
      },
      error => {
        this.toastr.error(error.headers.status, error.error.error, {disableTimeOut: true});
      }      
    );
  }

  aggregateObjects(name: string, filter: any, tuple: any, metrics: any) : Observable<RbAggregate[]> {
    const apiObservable = this.apiService.aggregateObjects(name, filter, tuple, metrics);
    const dataObservable = new Observable<RbAggregate[]>((observer) => {
      apiObservable.subscribe(
        resp => {
          const rbAggregateArray = Object.values(resp.list).map(json => new RbAggregate(json, this));
          observer.next(rbAggregateArray);
          observer.complete();
        }, 
        error => {
          this.toastr.error(error.headers.status, error.error.error, {disableTimeOut: true});
        }
      )
    })
    return dataObservable; 
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
        error => {
          this.toastr.error(error.headers.status, error.error.error, {disableTimeOut: true});
        }
      )
    })
    return fileObservable; 
  }

  subscribeObjectCreation(id: String, object: String, filter: any) {
    this.apiService.subscribeToSignal({
      "type":"objectcreate",
      "id": id,
      "objectname": object,
      "filter": filter
    });
  }
}
