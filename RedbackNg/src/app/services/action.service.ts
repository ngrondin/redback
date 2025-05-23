import { Injectable } from '@angular/core';
import { ObserverProxy } from 'app/helpers';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { Observable } from 'rxjs/internal/Observable';
import { ApiService } from './api.service';
import { DataService } from './data.service';
import { DialogService } from './dialog.service';
import { ErrorService } from './error.service';
import { FilterService } from './filter.service';
import { ModalService } from './modal.service';
import { ReportService } from './report.service';
import { RbObject } from 'app/datamodel';

@Injectable({
  providedIn: 'root'
})
export class ActionService {

  constructor(
    private dataService: DataService,
    private apiService: ApiService,
    private filterService: FilterService,    
    private reportService: ReportService,
    private modalService: ModalService,
    private errorService: ErrorService,
    private dialogService: DialogService
  ) { }


  public action(dataset: RbDatasetComponent, action: string, target: string, param: any, extraContext?: any, confirm?: string, timeout?: number) : Observable<null> {
    if(confirm == null) {
      let _action: string = action.toLowerCase();
      if(_action == 'create') {
        return this.create(dataset, param, extraContext);
      } else if(_action == 'createinmemory') {
        return this.createInMemory(dataset, param, extraContext);
      } else if(_action == 'delete') {
        return this.deleteSelected(dataset);
      } else if(_action == 'exportall') {
        return this.exportAll(dataset);
      } else if(_action == 'report') {
        return this.report(dataset, (target ?? param));
      } else if(_action == 'reportall') {
        return this.reportAll(dataset, (target ?? param));
      } else if(_action == 'reportlist') {
        return this.reportList(dataset, (target ?? param));
      } else if(_action == 'execute') {
        return this.execute(dataset, (target ?? param), (target != null ? param : null), extraContext, timeout);
      } else if(_action == 'executeall') {
        return this.executeAll(dataset, (target ?? param), (target != null ? param : null), extraContext, timeout);
      } else if(_action == 'executemaster') {
        return this.executeMaster(dataset, (target ?? param), (target != null ? param : null), extraContext, timeout);
      } else if(_action == 'executeglobal') {
        return this.executeGlobal(dataset, (target ?? param), (target != null ? param : null), extraContext, timeout);
      } else if(_action == 'clientscript') {
        return this.executeClientScript(dataset, param);
      } else if(_action == 'modal') {
        return this.showModal(target ?? param);
      } else if(_action == 'externallink') {
        return this.launchExternalLink(dataset, (target ?? param));
      } else if(_action == 'refresh') {
        return this.refresh(dataset);
      } else if(dataset != null && dataset.selectedObject != null) {
        return this.execute(dataset, _action, param, extraContext, timeout);
      } else {
        return new Observable((observer) => {observer.complete()});
      }
    } else {
      return new Observable((observer) => {
        this.dialogService.openDialog(confirm, [
          {label:"Ok", callback:() => {
            this.action(dataset, action, target, param, extraContext, null, timeout).subscribe(() => observer.complete());
          }}, 
          {label:"Cancel", callback:() => {
            observer.complete();
          }}
        ]);
      })
    }
  }

  public create(dataset: RbDatasetComponent, param: any, extraContext?: any) : Observable<null> {
    return new Observable((observer) => {
      let data = this.calcCreateData(dataset, param, extraContext);
      this.dataService.create(dataset.objectname, null, data).subscribe(new ObserverProxy(observer, newObject => dataset.addObjectAndSelect(newObject)));
    });
  }

  public createInMemory(dataset: RbDatasetComponent, param: any, extraContext?: any) : Observable<null> {
    return new Observable((observer) => {
      let data = this.calcCreateData(dataset, param, extraContext);
      this.dataService.createInMemory(dataset.objectname, null, data).subscribe(new ObserverProxy(observer, newObject => dataset.addObjectAndSelect(newObject)));
    });
  }

  private calcCreateData(dataset: RbDatasetComponent, param: any, extraContext: any) : any {
    let data = dataset.resolvedFilter;
    if(param != null) {
      let paramResolvedFilter: any = this.filterService.resolveFilter(param, dataset.selectedObject, dataset.selectedObject, dataset.relatedObject, extraContext);
      data = this.filterService.mergeFilters(data, paramResolvedFilter)
    }
    data = this.filterService.convertToData(data);
    return data;
  }

  public deleteSelected(dataset: RbDatasetComponent) : Observable<null> {
    return new Observable((observer) => {
      if(dataset.selectedObject != null) {
        this.dialogService.openDialog(
          "Delete this record?", 
          [
            {
              label: "Yes", 
              callback: () => {
                this.dataService.delete(dataset.selectedObject).subscribe(new ObserverProxy(observer, () => dataset.removeSelected()));
              }
            }, 
            {
              label: "No", 
              callback: () => {
                observer.complete();
              }
            }
          ]
        );
      } else {
        observer.complete();
      }
    });
  }

  public exportAll(dataset: RbDatasetComponent) : Observable<null> {
    return new Observable((observer) => {
      this.dataService.export(dataset.objectname, dataset.resolvedFilter, dataset.userSearch).subscribe(new ObserverProxy(observer));
    });
  }

  public report(dataset: RbDatasetComponent, reportName: string) : Observable<null> {
    return new Observable((observer) => {
      if(dataset.selectedObject != null) {
        this.reportService.launchReport(reportName, null, dataset.objectname, {"uid": dataset.selectedObject.uid}, null);
      }
      observer.next();
      observer.complete(); 
    });
  }

  public reportAll(dataset: RbDatasetComponent, reportName: string) : Observable<null> {
    return new Observable((observer) => {
      this.reportService.launchReport(reportName, null, dataset.objectname, dataset.resolvedFilter, dataset.userSearch);
      observer.next();
      observer.complete();   
    });
  }

  public reportList(dataset: RbDatasetComponent, reportName: string) : Observable<null> {
    return new Observable((observer) => {
      const selectedFilter = dataset.selectedObject != null ? {"uid": dataset.selectedObject.uid} : null;
      this.reportService.popupReportList(reportName, dataset.objectname, selectedFilter, dataset.resolvedFilter, dataset.userSearch);
      observer.next();
      observer.complete();
    });
  }

  public execute(dataset: RbDatasetComponent, functionName: string, functionParams: string, extraContext: any, timeout: number) : Observable<null> {
    return new Observable((observer) => {
      if(dataset != null && dataset.selectedObject != null) {
        let paramResolved: any = this.filterService.resolveFilter(functionParams, dataset.selectedObject, dataset.selectedObject, dataset.relatedObject, extraContext);
        this.dataService.executeObjectFunction(dataset.selectedObject, functionName, paramResolved, timeout).subscribe(new ObserverProxy(observer));  
      } else {
        observer.complete();
      }
    });
  }

  public executeAll(dataset: RbDatasetComponent, functionName: string, functionParams: string, extraContext: any, timeout: number) : Observable<null> {
    return new Observable((observer) => {
      let delay: number = 0;
      let doneCount: number = 0;
      dataset.list.forEach((object) => {
        setTimeout(() => {
          let paramResolved: any = this.filterService.resolveFilter(functionParams, object, dataset.selectedObject, dataset.relatedObject, extraContext);
          this.dataService.executeObjectFunction(object, functionName, paramResolved, timeout).subscribe(
            resp => {
              doneCount++;
              if(doneCount == dataset.list.length) {
                observer.next();
                observer.complete();
              }
            },
            error => {
              observer.error(error);
            })
        }, delay);
        delay += 50;
      });
    });
  }

  public executeMaster(dataset: RbDatasetComponent, functionName: string, functionParams: string, extraContext: any, timeout: number) : Observable<null> {
    return new Observable((observer) => {
      if(dataset != null && dataset.relatedObject != null) {
        let paramResolved: any = this.filterService.resolveFilter(functionParams, dataset.relatedObject, dataset.relatedObject, null, extraContext);
        this.dataService.executeObjectFunction(dataset.relatedObject, functionName, paramResolved, timeout).subscribe(new ObserverProxy(observer));
      } else {
        observer.complete()
      }
    });
  }

  public executeGlobal(dataset: RbDatasetComponent, functionName: string, functionParams: any, extraContext: any, timeout: number) : Observable<null> {
    return new Observable((observer) => {
      let paramResolved = {};
      if(functionParams != null) {
        if(dataset != null) {
          paramResolved = this.filterService.resolveFilter(functionParams, dataset.selectedObject, dataset.selectedObject, dataset.relatedObject, extraContext);  
        } else {
          paramResolved = functionParams;
        }
      } else if(dataset != null) {
        paramResolved = {
          "filter": dataset.resolvedFilter,
          "selecteduid": (dataset.selectedObject != null ? dataset.selectedObject.uid : null),
          "selecteduids": (dataset.selectedObjects.map(o => o.uid))
        }
      }
      this.dataService.executeGlobalFunction(functionName, paramResolved, timeout).subscribe(new ObserverProxy(observer));
    });
  }

  public executeClientScript(dataset:RbDatasetComponent, script: string) : Observable<null> {
    return new Observable((observer) => {
      let func = Function("dataset", "obj", "selectedObject", "relatedObject", script);
      func.call(window.redback, dataset, dataset.selectedObject, dataset.selectedObject, dataset.relatedObject);
      observer.next();
      observer.complete();  
    });    
  }

  public showModal(modalName: string) : Observable<null> {
    return new Observable((observer) => {
      this.modalService.open(modalName);
      observer.next();
      observer.complete();  
    });
  }

  public refresh(dataset: RbDatasetComponent): Observable<null> {
    return new Observable((observer) => {
      if(dataset != null) {
        dataset.refreshData();
      }
      observer.next();
      observer.complete();
    });
  }

  public launchExternalLink(dataset: RbDatasetComponent, linkExpression: string) : Observable<null> {
    return new Observable((observer) => {
      let object = dataset.selectedObject;
      let url = eval(linkExpression);
      window.open(url);
      observer.next();
      observer.complete();  
    });
  }
}
