import { Component, Inject, OnInit } from '@angular/core';
import { CONTAINER_DATA } from 'app/tokens';
import { RbPopupComponent } from '../rb-popup/rb-popup.component';

@Component({
  selector: 'rb-popup-color',
  templateUrl: './rb-popup-color.component.html',
  styleUrls: ['./rb-popup-color.component.css', '../rb-popup/rb-popup.component.css']
})
export class RbPopupColorComponent extends RbPopupComponent implements OnInit {
  colors = [

    ["#990000", "#cc0000", "#e06666", "#ea9999", "#f4cccc"],
    ["#b45f06", "#e69138", "#f6b26b", "#f9cb9c", "#fce5cd"],    
    ["#bf9000", "#f1c232", "#ffd966", "#ffe599", "#fff2cc"],
    ["#38761d", "#6aa84f", "#93c47d", "#b6d7a8", "#d9ead3"],
    ["#00a8ee", "#31b6ee", "#37b8ee", "#8ed2ee", "#d9e8ee"],
    ["#1155cc", "#3c78d8", "#6d9eeb", "#a4c2f4", "#c9daf8"],
    ["#351c75", "#674ea7", "#8e7cc3", "#b4a7d6", "#d9d2e9"],
    ["#741b47", "#a64d79", "#c27ba0", "#d5a6bd", "#ead1dc"],
    ["#000000", "#666666", "#b7b7b7", "#efefef", "#ffffff"]

  ];

  constructor(
    @Inject(CONTAINER_DATA) public config: any, 
  ) {
    super();
  }
  
  public getHighlighted() {

  }

  public setSearch(val: String) {

  }

  public keyTyped(keyCode: number) {

  }

  public clickedColor(color: string) {
    this.selected.emit(color);
  }
}
