import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DomSanitizer } from '@angular/platform-browser';
import { ApiService } from 'app/services/api.service';
import { ReportService } from 'app/services/report.service';
import { csvicon, excelicon, pdficon, txticon } from './reporticons.const';

@Component({
  selector: 'rb-reportlist',
  templateUrl: './rb-reportlist.component.html',
  styleUrls: ['./rb-reportlist.component.css']
})
export class RbReportlistComponent implements OnInit {
  reports: any = [];
  isLoading: boolean = false;

  constructor(
    private apiService: ApiService,
    private reportService: ReportService,
    private dialogRef: MatDialogRef<RbReportlistComponent>,
    private sanitizer: DomSanitizer,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit(): void {
    this.apiService.listReports(this.data.category).subscribe((json) => {
      this.reports = json.result;
      this.isLoading = false;
    });
    this.isLoading = true;
  }

  public launch(report: any) {
    if(this.data.selectedFilter != null) {
      this.reportService.launchReport(report.name, report.domain, this.data.selectedFilter);
    } else if(this.data.allFilter != null) {
      this.reportService.launchReport(report.name, report.domain, this.data.allFilter);
    }
    this.dialogRef.close();
  }

  public getIcon(type: string) : any {
    let image: string = type == 'excel' ? excelicon : type == 'csv' ? csvicon : type == 'pdf' ? pdficon : type == 'txt' ? txticon : '';
    return 'data:image/png;base64,' + image;
  }

}
