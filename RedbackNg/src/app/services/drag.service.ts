import { Injectable, ElementRef } from '@angular/core';
import { RbObject, XY } from 'app/datamodel';
import { Observable } from 'rxjs';
import { Observer } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DragService {
  dragObservers: Observer<any>[] = [];
  isDragging: boolean = false;
  draggingElement: ElementRef;
  innerHTML: String;
  object: RbObject;
  droppedOnElement: ElementRef;
  mouseOrigin: XY;
  offset: XY;
  position: XY;
  size: XY;

  constructor() { }

  public prepareDrag(el: ElementRef, o: RbObject, event: any) {
    this.draggingElement = el;
    this.object = o;
    this.mouseOrigin = new XY(event.clientX, event.clientY);
    this.offset = new XY(event.offsetX, event.offsetY);
    this.position = new XY(event.clientX - this.offset.x, event.clientY - this.offset.y);
    this.size = new XY(el.nativeElement.clientWidth, el.nativeElement.clientHeight); 
  }

  public startDragging() {
    if(this.isDragging == false && this.object != null) {
      this.isDragging = true;
      this.draggingElement.nativeElement.style.opacity = "15%";
      this.innerHTML = "";
      this.publishEvent({type:"start", object: this.object});
    }
  }

  public move(event: any) {
    if(this.object != null) {
      this.position = new XY(event.clientX - this.offset.x, event.clientY - this.offset.y);
      if(this.isDragging == false && this.object != null && (Math.abs(event.clientX - this.mouseOrigin.x) > 5 || Math.abs(event.clientY - this.mouseOrigin.y) > 5)) {
        this.startDragging();
      }
    }
  }

  public drop(el: ElementRef) {
    if(this.isDragging == true) {
      this.droppedOnElement = el;
    }
  }

  public endDrag() {
    if(this.isDragging) {
      this.isDragging = false;
      if(this.draggingElement != null && this.draggingElement.nativeElement != null) {
        this.draggingElement.nativeElement.style.opacity = "";
      }
      this.publishEvent({type:"end"});
    }
    this.droppedOnElement = null;
    this.draggingElement = null;
    this.innerHTML = null;
    this.object = null;
    this.mouseOrigin = null;
    this.offset = null;
    this.position = null;
    this.size = null;    
  }

  getObservable() : Observable<any>  {
    return new Observable<any>((observer) => {
      this.dragObservers.push(observer);
    });
  }

  publishEvent(event: any) {
    this.dragObservers.forEach((observer) => {
      observer.next(event);
    });  
  }
}
