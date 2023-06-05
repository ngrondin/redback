import { Component, Inject, OnInit } from '@angular/core';
import { RbPopupComponent } from '../rb-popup/rb-popup.component';
import { DataService } from 'app/services/data.service';
import { CONTAINER_DATA } from 'app/tokens';

@Component({
  selector: 'app-rb-popup-actions',
  templateUrl: './rb-popup-actions.component.html',
  styleUrls: ['./rb-popup-actions.component.css']
})
export class RbPopupActionsComponent extends RbPopupComponent implements OnInit {
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

  public clickAction(action: any) {
    this.selected.emit(action);
  }

}
