import { AfterContentInit, Component, EventEmitter, HostListener, Output, ViewChild, ViewContainerRef } from '@angular/core';
import { RbContainerComponent } from 'app/abstract/rb-container';

@Component({
  selector: 'rb-scroll',
  templateUrl: './rb-scroll.component.html',
  styleUrls: ['./rb-scroll.component.css']
})
export class RbScrollComponent  extends RbContainerComponent implements AfterContentInit {
  contentInitiated: boolean = false;
  isOverVTrack: boolean = false;
  isOverHTrack: boolean = false;
  draggingVThumb: boolean = false;
  draggingHThumb: boolean = false;
  showVTrack: boolean = false;
  showHTrack: boolean = false;
  vThumbLength: number = 0;
  hThumbLength: number = 0;
  vThumbPosition: number = 0;
  hThumbPosition: number = 0;

  @ViewChild('scroller', { read: ViewContainerRef, static: true }) scroller: ViewContainerRef;
  @ViewChild('content', { read: ViewContainerRef, static: true }) content: ViewContainerRef;
  @Output() rbscroll: EventEmitter<any> = new EventEmitter();
  
  ngAfterContentInit() {
    this.contentInitiated = true;    
    (new ResizeObserver((entries) => this.onResize(entries))).observe(this.scroller.element.nativeElement);
    (new ResizeObserver((entries) => this.onResize(entries))).observe(this.content.element.nativeElement);
  }

  containerInit() {
  }

  containerDestroy() {
  }

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
  }

  get vThumbBig() {
    return this.isOverVTrack || this.draggingVThumb;
  }

  get hThumbBig() {
    return this.isOverHTrack || this.draggingHThumb;
  }

  onResize(event: any) {
    let scrollTop = this.scroller.element.nativeElement.scrollTop;
    let scrollLeft = this.scroller.element.nativeElement.scrollLeft;
    let scrollerH = this.scroller.element.nativeElement.clientHeight;
    let scrollerW = this.scroller.element.nativeElement.clientWidth;
    let contentH = this.content.element.nativeElement.clientHeight;
    let contentW = this.content.element.nativeElement.clientWidth;
    this.showVTrack = contentH > scrollerH;
    this.showHTrack = contentW > scrollerW;
    this.vThumbLength = Math.max(40, scrollerH * (scrollerH / contentH));
    this.hThumbLength = Math.max(40, scrollerW * (scrollerW / contentW));
    this.calcThumbPositions();
  }

  overVTrack() {
    this.isOverVTrack = true;
  }

  overHTrack() {
    this.isOverHTrack = true;
  }

  outVTrack() {
    this.isOverVTrack = false;
  }

  outHTrack() {
    this.isOverHTrack = false;
  }

  startVThumbDrag(event) {
    this.draggingVThumb = true;
    let controller = this;
    let startClientY = event.clientY;
    let startScrollTop = this.scroller.element.nativeElement.scrollTop;
    let scrollerH = this.scroller.element.nativeElement.clientHeight;
    let contentH = this.content.element.nativeElement.clientHeight;
    let scrollerNativeElement = this.scroller.element.nativeElement;
    var whileMove = function(event) {
      let clientDelta = event.clientY - startClientY;
      let scrollDelta = clientDelta * (contentH / scrollerH);
      scrollerNativeElement.scrollTop = Math.round(startScrollTop + scrollDelta);
    }
    var endMove = function () {
      window.removeEventListener('mousemove', whileMove);
      window.removeEventListener('mouseup', endMove);
      controller.draggingVThumb = false;
    };
    event.stopPropagation(); 
    window.addEventListener('mousemove', whileMove);
    window.addEventListener('mouseup', endMove);   
  }

  startHThumbDrag(event) {
    this.draggingHThumb = true;
    let controller = this;
    let startClientX = event.clientX;
    let startScrollLeft = this.scroller.element.nativeElement.scrollLeft;
    let scrollerW = this.scroller.element.nativeElement.clientWidth;
    let contentW = this.content.element.nativeElement.clientWidth;
    let scrollerNativeElement = this.scroller.element.nativeElement;
    var whileMove = function(event) {
      let clientDelta = event.clientX - startClientX;
      let scrollDelta = clientDelta * (contentW / scrollerW);
      scrollerNativeElement.scrollLeft = Math.round(startScrollLeft + scrollDelta);
    }
    var endMove = function () {
      window.removeEventListener('mousemove', whileMove);
      window.removeEventListener('mouseup', endMove);
      controller.draggingHThumb = false;
    };
    event.stopPropagation(); 
    window.addEventListener('mousemove', whileMove);
    window.addEventListener('mouseup', endMove);   
  }


  onScroll(event) {
    this.rbscroll.emit(event);
    this.calcThumbPositions();
  }

  calcThumbPositions() {
    let scrollTop = this.scroller.element.nativeElement.scrollTop;
    let scrollLeft = this.scroller.element.nativeElement.scrollLeft;
    let scrollerH = this.scroller.element.nativeElement.clientHeight;
    let scrollerW = this.scroller.element.nativeElement.clientWidth;
    let contentH = this.content.element.nativeElement.clientHeight;
    let contentW = this.content.element.nativeElement.clientWidth;
    this.vThumbPosition = scrollerH * (scrollTop / contentH);
    this.hThumbPosition = scrollerW * (scrollLeft / contentW);
  }
}


