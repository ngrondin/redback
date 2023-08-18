import { Component, Input } from '@angular/core';

@Component({
  selector: 'rb-iconbutton-badge',
  templateUrl: './rb-iconbutton-badge.component.html',
  styleUrls: ['./rb-iconbutton-badge.component.css']
})
export class RbIconbuttonBadgeComponent {
  @Input('count') count = 0;
  @Input('icon') icon = "add";
}
