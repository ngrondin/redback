import { Injectable, Injector } from '@angular/core';
import { ApiService } from './api.service';
import { MatDialog } from '@angular/material/dialog';
import { RbReportlistComponent } from '../rb-reportlist/rb-reportlist.component';
import { filter } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  constructor(
    private apiService: ApiService,
    public injector: Injector,
    //public overlay: Overlay,
    public dialog: MatDialog
  ) { }

  public popupReportList(category: String, filterData: any) {
    let dialogRef = this.dialog.open(RbReportlistComponent, {
      data: {
        category: category,
        filterData: filterData
      }
    });
  }

  public launchReport(name: String, domain: String, filterData: any) {
    let query = 'report=' + name;
    if(Array.isArray(filterData)) {
      query = query + "&sets=" + JSON.stringify(filterData);
    } else {
      query = query + "&object=" + filterData.object;
      if(filterData.uid != null) {
        query = query + "&uid=" + filterData.uid;
      } 
      if(filterData.filter != null) {
        query = query + "&filter=" + JSON.stringify(filterData.filter);
      }
          if(filterData.search != null) {
        query = query + '&search=' + filterData.search;
      }
    }
    if(domain != null) {
      query = query + '&domain=' + domain;
    }
    query = query + "&timezone=" + Intl.DateTimeFormat().resolvedOptions().timeZone; 
    window.open(this.apiService.baseUrl + '/' + this.apiService.reportService + '?' + query);
  }


  /*public launchReport(name: String, domain: String, object: String, filter: any, search: string) {
    let query = 'report=' + name + '&object=' + object + '&filter=' + JSON.stringify(filter);
    if(search != null) {
      query = query + '$search=' + search;
    }
    if(domain != null) {
      query = query + '&domain=' + domain;
    }
    query = query + "&timezone=" + Intl.DateTimeFormat().resolvedOptions().timeZone; 
    window.open(this.apiService.baseUrl + '/' + this.apiService.reportService + '?' + query);

  }*/

}
