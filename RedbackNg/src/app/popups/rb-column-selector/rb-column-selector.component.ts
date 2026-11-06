import { Component, Inject, OnInit } from '@angular/core';
import { RbPopupComponent } from '../rb-popup/rb-popup.component';
import { CONTAINER_DATA } from 'app/tokens';

/**
 * Ticks which items of a list are shown, for anything made of columns or tabs.
 * - config.items: list of {key, label, shown}
 * - selected emits the items once, when the user clicks away; the opener saves them and closes the popup
 * - the changes wait for that click: the popup lives on its anchor, so hiding the anchor while open would remove the popup
 * - the last shown item stays, it is what opens the popup
 */
@Component({
  selector: 'rb-column-selector',
  templateUrl: './rb-column-selector.component.html',
  styleUrls: ['./rb-column-selector.component.css', '../rb-popup/rb-popup.component.css']
})
export class RbColumnSelectorComponent extends RbPopupComponent implements OnInit {
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

  public canToggle(item: any): boolean {
    return !item.shown || this.config.items.filter((i: any) => i.shown).length > 1;
  }

  public toggle(item: any) {
    item.shown = !item.shown;
  }

  public onOverlayClick() {
    this.selected.emit(this.config.items);
  }

}
