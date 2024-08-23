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
  data: any;
  droppedOutCallback: Function;
  mouseOrigin: XY;
  offset: XY;
  position: XY;
  size: XY;
  droppedOnElement: ElementRef;

  constructor() { }

  public prepareDrag(el: ElementRef, d: any, event: any, doCB: Function) {
    this.draggingElement = el;
    this.data = d;
    this.droppedOutCallback = doCB;
    this.mouseOrigin = new XY(event.clientX, event.clientY);
    this.offset = new XY(event.offsetX, event.offsetY);
    this.position = new XY(event.clientX - this.offset.x, event.clientY - this.offset.y);
    this.size = new XY(el.nativeElement.clientWidth, el.nativeElement.clientHeight); 
  }

  public startDragging() {
    if(this.isDragging == false && this.data != null) {
      this.isDragging = true;
      this.draggingElement.nativeElement.style.opacity = "15%";
      this.innerHTML = "";
      this.publishEvent({type:"start", data: this.data});
    }
  }

  public move(event: any) {
    if(this.data != null) {
      this.position = new XY(event.clientX - this.offset.x, event.clientY - this.offset.y);
      if(this.isDragging == false && (Math.abs(event.clientX - this.mouseOrigin.x) > 5 || Math.abs(event.clientY - this.mouseOrigin.y) > 5)) {
        this.startDragging();
      }
    }
  }

  public dropOn(el: ElementRef) {
    if(this.isDragging == true) {
      this.droppedOnElement = el;
    }
  }

  public endDrag() {
    this.data = null;
    this.innerHTML = null;
    this.mouseOrigin = null;
    this.offset = null;
    this.position = null;
    this.size = null;       
    if(this.isDragging) {
      this.isDragging = false;
      if(this.draggingElement != null && this.draggingElement.nativeElement != null) {
        this.draggingElement.nativeElement.style.opacity = "";
      }
      if(this.droppedOnElement == null && this.droppedOutCallback != null) {
        this.droppedOutCallback();
      }
      this.publishEvent({type:"end"});
    }
    this.droppedOnElement = null;
    this.draggingElement = null;
    this.droppedOutCallback = null;
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
