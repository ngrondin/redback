import { Component, EventEmitter, HostListener, Inject, Output, VERSION } from '@angular/core';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import packageJson from '../../../package.json'; 
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { RbDialogComponent } from 'app/rb-dialog/rb-dialog.component';

@Component({
  selector: 'rb-about',
  templateUrl: './rb-about.component.html',
  styleUrls: ['./rb-about.component.css']
})
export class RbAboutComponent  {
  @Output('close') closeClicked = new EventEmitter();
  @HostListener('click', ['$event']) backgroundClick($event) {this.close() }

  constructor(
    public dialogRef: MatDialogRef<RbDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  public get clientVersion() : string {
    return packageJson.version;
  }

  public get angularVersion(): string {
    return VERSION.full;
  }

  close() {
    this.dialogRef.close();
    this.closeClicked.emit();
  }

}
