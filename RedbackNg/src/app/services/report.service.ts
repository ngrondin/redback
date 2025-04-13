import { Injectable, Injector } from '@angular/core';
import { ApiService } from './api.service';
import { MatDialog } from '@angular/material/dialog';
import { RbReportlistComponent } from '../rb-reportlist/rb-reportlist.component';

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

  public popupReportList(category: String, objectname: String, selectedFilter: any, allFilter: any, search: string) {
    let dialogRef = this.dialog.open(RbReportlistComponent, {
      data: {
        category: category,
        objectname: objectname,
        selectedFilter: selectedFilter,
        allFilter: allFilter,
        search: search
      }
    });
  }

  public launchReport(name: String, domain: String, object: String, filter: any, search: string) {
    let query = 'report=' + name + '&object=' + object + '&filter=' + JSON.stringify(filter);
    if(search != null) {
      query = query + '$search=' + search;
    }
    if(domain != null) {
      query = query + '&domain=' + domain;
    }
    query = query + "&timezone=" + Intl.DateTimeFormat().resolvedOptions().timeZone; 
    window.open(this.apiService.baseUrl + '/' + this.apiService.reportService + '?' + query);

  }

}
