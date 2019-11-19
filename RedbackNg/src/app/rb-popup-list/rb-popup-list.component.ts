import { Component, OnInit, Inject, InjectionToken } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { RbObject } from '../datamodel';
import { OverlayRef } from '@angular/cdk/overlay';
import { CONTAINER_DATA } from '../tokens';

@Component({
  selector: 'rb-popup-list',
  templateUrl: './rb-popup-list.component.html',
  styleUrls: ['./rb-popup-list.component.css']
})
export class RbPopupListComponent implements OnInit {

  constructor(
    @Inject(CONTAINER_DATA) public data: any, 
    public overlayRef: OverlayRef
  ) { }

  ngOnInit() {
  }

}
