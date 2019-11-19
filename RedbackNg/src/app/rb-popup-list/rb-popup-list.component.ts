import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { RbObject } from '../datamodel';

export interface RbPopupDialogData {
  rbObject: RbObject;
  uid: string;
  attribute: string;
}

@Component({
  selector: 'rb-popup-list',
  templateUrl: './rb-popup-list.component.html',
  styleUrls: ['./rb-popup-list.component.css']
})
export class RbPopupListComponent implements OnInit {

  constructor(
  ) { }

  ngOnInit() {
  }

}
