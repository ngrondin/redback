import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable, of, Observer } from 'rxjs';
import { RbObject, RbFile, RbAggregate } from '../datamodel';
import { ErrorService } from './error.service';
import { ClientWSService } from './clientws.service';



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
    private errorService: ErrorService
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
        error => this.errorService.receiveHttpError(error)
      );
    })
    return dataObservable; 
  }

  listObjects(name: string, filter: any, search: string, sort: any, page: number, pageSize: number, addRelated: boolean) : Observable<any> {
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

  listRelatedObjects(name: string, uid: string, attribute: string, filter: any, search: string, sort: any, addRelated: boolean) : Observable<any> {
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
        !this.clientWSService.isConnected() ? this.updateObjectFromServer(resp) : null
      },
      error => this.errorService.receiveHttpError(error)
    );
  }

  
  executeGlobal(func: string, param: any) {
    this.apiService.executeGlobal(func, param).subscribe(
      resp => {
        null
      },
      error => this.errorService.receiveHttpError(error)
    );
  }

  aggregateObjects(name: string, filter: any, search: string, tuple: any, metrics: any, page: number = 0, pageSize: number = 50) : Observable<RbAggregate[]> {
    const apiObservable = this.apiService.aggregateObjects(name, filter, search, tuple, metrics, page, pageSize);
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

  exportObjects(name: string, filter: any, search: string) {
    const apiObservable = this.apiService.exportObjects(name, filter, search);
    apiObservable.subscribe(
      resp => {
        const url = window.URL.createObjectURL(new Blob([...resp]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'export.csv');
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      },
      error => this.errorService.receiveHttpError(error)
    );
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
