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

  public popupReportList(category: String, selectedFilter: any, allFilter: any) {
    let dialogRef = this.dialog.open(RbReportlistComponent, {
      data: {
        category: category,
        selectedFilter: selectedFilter,
        allFilter: allFilter
      }
    });
  }

  public launchReport(name: String, domain: String, filter: any) {
    let query = 'report=' + name + '&filter=' + JSON.stringify(filter);
    if(domain != null) {
      query = query + '&domain=' + domain;
    }
    window.open(this.apiService.baseUrl + '/' + this.apiService.reportService + '?' + query);

  }

}
