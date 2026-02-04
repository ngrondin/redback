import { OverlayRef } from '@angular/cdk/overlay';
import { Inject } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { CONTAINER_DATA } from 'app/tokens';
import { RbPopupComponent } from '../rb-popup/rb-popup.component';

@Component({
  selector: 'rb-popup-hardlist',
  templateUrl: './rb-popup-hardlist.component.html',
  styleUrls: ['./rb-popup-hardlist.component.css', '../rb-popup/rb-popup.component.css']
})
export class RbPopupHardlistComponent extends RbPopupComponent implements OnInit {

  constructor(
    @Inject(CONTAINER_DATA) public config: any, 
    public overlayRef: OverlayRef
  ) {
    super();
  }

  ngOnInit(): void {
  }

  public get list() {
    return this.config;
  }

  public getHighlighted() {
    
  }
  
  public setSearch(val: String) {
    
  }
  
  public keyTyped(keyCode: number) {
    
  }

  public select(item: any) {
    let val = item;
    if(item['value'] != null) {
      val = item['value'];
    }
    this.selected.emit(val);
  }
}
