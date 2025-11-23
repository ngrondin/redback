import { Component, HostBinding, Input } from '@angular/core';

@Component({
  selector: 'rb-icon',
  templateUrl: './rb-icon.component.html',
  styleUrls: ['./rb-icon.component.css']
})
export class RbIconComponent {
  @Input('icon') icon ;
  @Input('color') color ;

  @HostBinding('class.material-symbols-outlined') msoClass: boolean = true;
  @HostBinding('style.color') get styleColor() { return this.color != null ? this.color : null;}

}
