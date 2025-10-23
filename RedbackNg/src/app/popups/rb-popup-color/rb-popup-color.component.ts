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
    // Neutral Tones (6 shades)
    '#FFFFFF', '#F7F7F7', '#E0E0E0', '#9E9E9E', '#424242', '#1E1E1E',
    // Warm Hues (6 shades)
    '#FF5733', '#FFC300', '#F1963B', '#D58D8D', '#9A6324', '#B44446',
    // Cool Hues (6 shades)
    '#007BFF', '#20C997', '#42d4f4', '#6F42C1', '#A78BFA', '#0E0E0E',
    // Muted & Earthy Tones (6 shades)
    '#A47864', '#7D8A74', '#B3CED6', '#C0C5CE', '#D0D3D4', '#E2725B'
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
