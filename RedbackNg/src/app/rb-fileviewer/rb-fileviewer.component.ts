import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DomSanitizer } from '@angular/platform-browser';
import { RbDialogComponent } from 'app/rb-dialog/rb-dialog.component';
import { ApiService } from 'app/services/api.service';

@Component({
  selector: 'rb-fileviewer',
  templateUrl: './rb-fileviewer.component.html',
  styleUrls: ['./rb-fileviewer.component.css']
})
export class RbFileviewerComponent implements OnInit {
  fileUid: string;
  zoom: boolean = false;

 constructor(
    public dialogRef: MatDialogRef<RbDialogComponent>,
    private apiService: ApiService,
    private domSanitizer: DomSanitizer,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit(): void {
    this.fileUid = this.data.fileUid;
  }

  public get url() : string {
    return this.apiService.baseUrl + '/' + this.apiService.fileService + '?fileuid=' + this.fileUid
  }

  public get maxWidth() : string {
    return this.zoom ? "100%" : "90vw";
  }

  public get maxHeight() : string {
    return this.zoom ? "100%" : "90vh";
  }

  public toggleZoom() {
    this.zoom = !this.zoom;
  }
}
