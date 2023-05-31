import { Inject } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { MatLegacyDialogRef as MatDialogRef, MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA } from '@angular/material/legacy-dialog';
import { DialogOption } from 'app/services/dialog.service';

@Component({
  selector: 'rb-dialog',
  templateUrl: './rb-dialog.component.html',
  styleUrls: ['./rb-dialog.component.css']
})
export class RbDialogComponent implements OnInit {
  text: string;
  options: DialogOption[];

  constructor(
    public dialogRef: MatDialogRef<RbDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit(): void {
    this.text = this.data.text;
    this.options = this.data.options;
  }

  clickOption(option: DialogOption) {
    this.dialogRef.close();
    option.callback();
  }

}
