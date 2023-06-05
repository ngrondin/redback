import { Component, HostListener, Input } from '@angular/core';

@Component({
  selector: 'rb-clickable',
  templateUrl: './rb-clickable.component.html',
  styleUrls: ['./rb-clickable.component.css']
})
export class RbClickableComponent {
  @Input('focus') focus = false;
  @Input('running') running = false;
  hovering: boolean = false;
  clicked: boolean = false;
  clickTop: number = 0;
  clickLeft: number = 0;

  constructor(
  ) {
  }

  @HostListener('click', ['$event']) _onclick($event) {
    let tgt = $event.target;
    this.clickTop = $event.offsetY;
    this.clickLeft = $event.offsetX;
    while(tgt.className.indexOf("rb-clickable-container") == -1) {
      this.clickLeft = this.clickLeft + tgt.offsetLeft;
      this.clickTop = this.clickTop + tgt.offsetTop;
      tgt = tgt.offsetParent;
    }
    this.clicked = true;
    this.hovering = false;
    setTimeout(() => this.clicked = false, 500);
  }

  @HostListener('mouseenter', ['$event']) _mouseenter($event) {
    this.hovering = true;  
  }

  @HostListener('mouseleave', ['$event']) _mouseleave($event) {
    this.hovering = false;  
  }

}
