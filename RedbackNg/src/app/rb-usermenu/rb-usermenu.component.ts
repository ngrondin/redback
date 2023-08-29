import { Component, Input } from '@angular/core';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-usermenu',
  templateUrl: './rb-usermenu.component.html',
  styleUrls: ['./rb-usermenu.component.css', '../popups/rb-popup/rb-popup.component.css']
})
export class RbUsermenuComponent extends RbPopupComponent {
  
  constructor(
    public userprefService: UserprefService
  ) {
    super();
  }

  public get username() : string {
    return this.userprefService.username;
  }

  public get userdisplay() : string {
    return this.userprefService.userdisplay;
  }

  public click(type) {
    this.selected.emit(type);
    this.cancelled.emit();
  }
  
  public getHighlighted() {

  }
  
  public setSearch(val: String) {

  }
  
  public keyTyped(keyCode: number) {

  }

}
