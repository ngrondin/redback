import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable, of } from 'rxjs';
import { ObjectResp, RbObject } from './datamodel';



@Injectable({
  providedIn: 'root'
})
export class DataService {
  allObjects: RbObject[];
  saveImmediatly: boolean;

  constructor(
    private apiService: ApiService
  ) {
    this.allObjects = [];
    this.saveImmediatly = true;
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

  listObjects(name: string, filter: any, search: string) : Observable<any> {
    const listObs = this.apiService.listObjects(name, filter, search);
    const dataObservable = new Observable((observer) => {
      listObs.subscribe(resp => {
        const rbObjectArray = Object.values(resp.list).map(json => this.updateObjectFromServer(json));
        observer.next(rbObjectArray);
        observer.complete();
      });
    })
    return dataObservable; 
  }

  listRelatedObjects(name: string, uid: string, attribute: string, filter: any, search: string) : Observable<any> {
    const listObs = this.apiService.listRelatedObjects(name, uid, attribute, filter, search);
    const dataObservable = new Observable((observer) => {
      listObs.subscribe(resp => {
        const rbObjectArray = Object.values(resp.list).map(json => this.updateObjectFromServer(json));
        observer.next(rbObjectArray);
        observer.complete();
      });
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
    }
    return rbObject;
  }

  updateObjectToServer(rbObject: RbObject) {
    let upd: any = {};
    for(const attribute of rbObject.changed) {
        upd[attribute] = rbObject.data[attribute];
    }
    this.apiService.updateObject(rbObject.objectname, rbObject.uid, upd).subscribe(resp => this.updateObjectFromServer(resp));
  }
}
