import { Component, OnInit, Input } from '@angular/core';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'rb-map',
  templateUrl: './rb-map.component.html',
  styleUrls: ['./rb-map.component.css'],
  exportAs: 'rbMap'
})
export class RbMapComponent implements OnInit {
  @Input('object') object: RbObject;
  @Input('list') list: RbObject[];

  constructor() { }

  ngOnInit() {
  }

}
