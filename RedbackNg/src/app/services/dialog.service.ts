import { Component } from '@angular/core';
import { Injectable, Injector } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { RbDialogComponent } from 'app/rb-dialog/rb-dialog.component';
import { RbReportlistComponent } from 'app/rb-reportlist/rb-reportlist.component';
import { Overlay } from 'ngx-toastr';

type DialogCallbackFunction = () => void;

export class DialogOption {
  label: string;
  callback: DialogCallbackFunction;
}

@Injectable({
  providedIn: 'root'
})
export class DialogService {

  constructor(
    public injector: Injector,
    public overlay: Overlay,
    public dialog: MatDialog
  ) { }

  public openDialog(text: string, options: DialogOption[] ) {
    let dialogRef = this.dialog.open(RbDialogComponent, {
      data: {
        text: text,
        options: options
      },
      autoFocus: false,
      restoreFocus: false
    });
  }


}
