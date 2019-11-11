import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable, of } from 'rxjs';
import { ObjectResp, RbObject } from './datamodel';



@Injectable({
  providedIn: 'root'
})
export class DataService {

  constructor(
    private apiService: ApiService
  ) { }


  listObjects(name: string, filter: Object) : Observable<any> {
    const listObs = this.apiService.listObjects(name, filter);
    const dataObservable = new Observable((observer) => {
      listObs.subscribe(resp => {
        const newData = this.processObjectList(resp.list);
        observer.next(newData);
        observer.complete();
      });
    })
    return dataObservable; 
  }

  processObjectList(list: any) : RbObject[] {
    //let newObjects : RbObject[] = [];
    //alert(Object.values(list));
    alert(list.length);
    const newObjects = Object.values(list).map(json => new RbObject(json));
    alert(newObjects);
    return newObjects;
  }
}
