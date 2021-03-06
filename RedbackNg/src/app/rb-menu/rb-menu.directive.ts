import { Directive, Input } from '@angular/core';

@Directive({
  selector: 'rb-menu-old',
  exportAs: 'menu',
})
export class RbMenuDirective {
  @Input('mode') mode = 'large';
  public largemenu : boolean;
  public groupOpen : boolean[];

  constructor() { 
    this.largemenu = true;
    this.groupOpen = [];
  }

  get isLarge() : boolean {
    return this.mode == 'large';
  }

  public toggleGroup(grp : string) {
    if(this.groupOpen[grp] == null)
      this.groupOpen[grp] = true;
    else 
      this.groupOpen[grp] = !this.groupOpen[grp];
  }

  public isGroupOpen(grp : string) : boolean {
    if(this.groupOpen[grp] != null)
      return this.groupOpen[grp];
    else
      return false;
  }
}
