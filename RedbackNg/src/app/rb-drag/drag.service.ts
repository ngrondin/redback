import { Injectable, ElementRef } from '@angular/core';
import { RbObject, XY } from 'app/datamodel';

@Injectable({
  providedIn: 'root'
})
export class DragService {
  isDragging: boolean = false;
  sourceElement: ElementRef;
  object: RbObject;
  mouseOrigin: XY;
  offset: XY;
  position: XY;
  size: XY;

  constructor() { }

  public startDrag(el: ElementRef, o: RbObject, event: any) {
    this.sourceElement = el;
    this.object = o;
    this.mouseOrigin = new XY(event.clientX, event.clientY);
    this.offset = new XY(event.offsetX, event.offsetY);
    this.position = new XY(event.clientX - this.offset.x, event.clientY - this.offset.y);
    this.size = new XY(el.nativeElement.clientWidth, el.nativeElement.clientHeight);
  }

  public move(event: any) {
    if(this.object != null) {
      this.position = new XY(event.clientX - this.offset.x, event.clientY - this.offset.y);
      if(this.isDragging == false && this.object != null && (Math.abs(event.clientX - this.mouseOrigin.x) > 5 || Math.abs(event.clientY - this.mouseOrigin.y) > 5)) {
        this.isDragging = true;
      }
    }
  }

  public drop(el: ElementRef) {
    //this.endDrag();
  }

  public endDrag() {
    this.isDragging = false;
    this.object = null;
    this.mouseOrigin = null;
    this.offset = null;
    this.position = null;
    this.size = null;
  }
}
