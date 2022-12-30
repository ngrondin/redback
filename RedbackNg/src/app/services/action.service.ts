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


  public action(dataset: RbDatasetComponent, actionName: string, param: any, timeout?: number, confirm?: string) : Observable<null> {
    if(confirm == null) {
      let _name: string = actionName.toLowerCase();
      if(_name == 'create') {
        return this.create(dataset, param);
      } else if(_name == 'createinmemory') {
        return this.createInMemory(dataset, param);
      } else if(_name == 'delete') {
        return this.deleteSelected(dataset);
      } else if(_name == 'exportall') {
        return this.exportAll(dataset);
      } else if(_name == 'report') {
        return this.report(dataset, param);
      } else if(_name == 'reportall') {
        return this.reportAll(dataset, param);
      } else if(_name == 'reportlist') {
        return this.reportList(dataset, param);
      } else if(_name == 'execute') {
        return this.execute(dataset, param, null);
      } else if(_name == 'executeall') {
        return this.executeAll(dataset, param);
      } else if(_name == 'executemaster') {
        return this.executeMaster(dataset, param);
      } else if(_name == 'executeglobal') {
        return this.executeGlobal(dataset, param);
      } else if(_name == 'executedomain') {
        return this.executeDomain(dataset, param, timeout);
      } else if(_name == 'modal') {
        return this.showModal(param);
      } else if(_name == 'externallink') {
        return this.launchExternalLink(dataset, param);
      } else if(dataset.selectedObject != null) {
        return this.execute(dataset, actionName, param);
      }
    } else {
      return new Observable((observer) => {
        this.dialogService.openDialog(confirm, [
          {label:"Ok", callback:() => {
            this.action(dataset, actionName, param, timeout, null).subscribe(() => observer.complete());
          }}, 
          {label:"Cancel", callback:() => {
            observer.complete();
          }}
        ]);
      })
    }
  }

  private calcCreateData(dataset: RbDatasetComponent, param: any) : any {
    let data = dataset.resolvedFilter;
    if(param != null) {
      let paramResolvedFilter: any = this.filterService.resolveFilter(param, dataset.selectedObject, dataset.selectedObject, dataset.relatedObject);
      data = this.filterService.mergeFilters(data, paramResolvedFilter)
    }
    data = this.filterService.convertToData(data);
    return data;
  }

  public create(dataset: RbDatasetComponent, param: any) : Observable<null> {
    return new Observable((observer) => {
      let data = this.calcCreateData(dataset, param);
      this.dataService.create(dataset.objectname, null, data).subscribe(new ObserverProxy(observer, newObject => dataset.addObjectAndSelect(newObject)));
    });
  }

  public createInMemory(dataset: RbDatasetComponent, param: any) : Observable<null> {
    return new Observable((observer) => {
      let data = this.calcCreateData(dataset, param);
      this.dataService.createInMemory(dataset.objectname, null, data).subscribe(new ObserverProxy(observer, newObject => dataset.addObjectAndSelect(newObject)));
    });
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
      this.dataService.export(dataset.objectname, dataset.resolvedFilter, dataset.searchString).subscribe(new ObserverProxy(observer));
    });
  }

  public report(dataset: RbDatasetComponent, reportName: string) : Observable<null> {
    return new Observable((observer) => {
      if(dataset.selectedObject != null) {
        this.reportService.launchReport(reportName, null, {"uid": dataset.selectedObject.uid});
      }
      observer.next();
      observer.complete(); 
    });
  }

  public reportAll(dataset: RbDatasetComponent, reportName: string) : Observable<null> {
    return new Observable((observer) => {
      this.reportService.launchReport(reportName, null, dataset.resolvedFilter);
      observer.next();
      observer.complete();   
    });
  }

  public reportList(dataset: RbDatasetComponent, reportName: string) : Observable<null> {
    return new Observable((observer) => {
      const selectedFilter = dataset.selectedObject != null ? {"uid": dataset.selectedObject.uid} : null;
      this.reportService.popupReportList(reportName, selectedFilter, dataset.resolvedFilter);
      observer.next();
      observer.complete();
    });
  }

  public execute(dataset: RbDatasetComponent, functionName: string, functionParams: string) : Observable<null> {
    return new Observable((observer) => {
      this.dataService.executeObjectFunction(dataset.selectedObject, functionName, functionParams).subscribe(new ObserverProxy(observer));
    });
  }

  public executeAll(dataset: RbDatasetComponent, functionName: string) : Observable<null> {
    return new Observable((observer) => {
      let delay: number = 0;
      let doneCount: number = 0;
      dataset.list.forEach((object) => {
        setTimeout(() => {
          this.dataService.executeObjectFunction(object, functionName, null).subscribe(
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

  public executeMaster(dataset: RbDatasetComponent, functionName: string) : Observable<null> {
    return new Observable((observer) => {
      if(dataset.relatedObject != null) {
        this.dataService.executeObjectFunction(dataset.relatedObject, functionName, null).subscribe(new ObserverProxy(observer));
      }
    });
  }

  public executeGlobal(dataset: RbDatasetComponent, functionName: string) : Observable<null> {
    return new Observable((observer) => {
      let funcParam = {
        "filter": dataset.resolvedFilter,
        "selecteduid": (dataset.selectedObject != null ? dataset.selectedObject.uid : null)
      }
      this.dataService.executeGlobalFunction(functionName, funcParam).subscribe(new ObserverProxy(observer));
    });
  }

  public executeDomain(dataset: RbDatasetComponent, functionName: string, timeout: number) : Observable<null> {
    return new Observable((observer) => {
      if(dataset.selectedObject != null) {
        this.apiService.executeDomain(functionName, dataset.selectedObject.domain, {"uid": dataset.selectedObject.uid}, timeout).subscribe(new ObserverProxy(observer, null, error => this.errorService.receiveHttpError(error)));
      } else {
        observer.error("No object selected");
      }
    });
  }

  public showModal(modalName: string) : Observable<null> {
    return new Observable((observer) => {
      this.modalService.open(modalName);
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
