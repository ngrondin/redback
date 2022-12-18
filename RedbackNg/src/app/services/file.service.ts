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
export class FileService {

  constructor(
    private apiService: ApiService,
    private clientWSService: ClientWSService,
    private errorService: ErrorService,
    private filterService: FilterService
  ) {

  }

  list(object: string, uid: any) : Observable<RbFile[]> {
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

}
