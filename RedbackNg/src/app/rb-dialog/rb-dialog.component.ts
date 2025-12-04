import { Inject } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DialogOption } from 'app/services/dialog.service';

@Component({
  selector: 'rb-dialog',
  templateUrl: './rb-dialog.component.html',
  styleUrls: ['./rb-dialog.component.css']
})
export class RbDialogComponent implements OnInit {
  text: string;
  options: DialogOption[];
  optionselected: boolean = false;

  constructor(
    public dialogRef: MatDialogRef<RbDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit(): void {
    this.text = this.data.text;
    this.options = this.data.options;
  }

  ngOnDestroy(): void {
    if(this.optionselected == false && this.data.onclose != null) {
      this.data.onclose();
    }
  }

  clickOption(option: DialogOption) {
    this.optionselected = true;
    this.dialogRef.close();
    option.callback();
  }

}
