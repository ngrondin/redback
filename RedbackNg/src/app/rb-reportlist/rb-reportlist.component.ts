import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { ApiService } from 'app/services/api.service';
import { ReportService } from 'app/services/report.service';

@Component({
  selector: 'rb-reportlist',
  templateUrl: './rb-reportlist.component.html',
  styleUrls: ['./rb-reportlist.component.css']
})
export class RbReportlistComponent implements OnInit {
  reports: any = [];

  constructor(
    private apiService: ApiService,
    private reportService: ReportService,
    private dialogRef: MatDialogRef<RbReportlistComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit(): void {
    this.apiService.listReports(this.data.category).subscribe((json) => {
      this.reports = json.result;
    });
  }

  public launch(report: any) {
    if(this.data.selectedFilter != null) {
      this.reportService.launchReport(report.name, report.domain, this.data.selectedFilter);
    } else if(this.data.allFilter != null) {
      this.reportService.launchReport(report.name, report.domain, this.data.allFilter);
    }
    this.dialogRef.close();
  }

}
