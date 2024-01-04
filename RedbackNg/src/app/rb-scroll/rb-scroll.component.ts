import { AfterContentInit, Component, EventEmitter, HostListener, Output, ViewChild, ViewContainerRef } from '@angular/core';

@Component({
  selector: 'rb-scroll',
  templateUrl: './rb-scroll.component.html',
  styleUrls: ['./rb-scroll.component.css']
})
export class RbScrollComponent implements AfterContentInit {
  contentInitiated: boolean = false;
  isOverVTrack: boolean = false;
  isOverHTrack: boolean = false;
  draggingVThumb: boolean = false;
  draggingHThumb: boolean = false;

  @ViewChild('scroller', { read: ViewContainerRef, static: true }) scroller: ViewContainerRef;
  @ViewChild('content', { read: ViewContainerRef, static: true }) content: ViewContainerRef;
  @Output() rbscroll: EventEmitter<any> = new EventEmitter();
  
  ngAfterContentInit() {
    this.contentInitiated = true;    
  }

  get showVTrack() {
    if(!this.contentInitiated) return false;
    let scrollerH = this.scroller.element.nativeElement.clientHeight;
    let contentH = this.content.element.nativeElement.clientHeight;
    return contentH > scrollerH;
  }

  get showHTrack() {
    if(!this.contentInitiated) return false;
    let scrollerW = this.scroller.element.nativeElement.clientWidth;
    let contentW = this.content.element.nativeElement.clientWidth;
    return contentW > scrollerW;
  }

  get vThumbLength() {
    let scrollerH = this.scroller.element.nativeElement.clientHeight;
    let contentH = this.content.element.nativeElement.clientHeight;
    let res = scrollerH * (scrollerH / contentH);
    if(res < 40) res = 40;
    return res;
  }

  get hThumbLength() {
    let scrollerW = this.scroller.element.nativeElement.clientWidth;
    let contentW = this.content.element.nativeElement.clientWidth;
    let res = scrollerW * (scrollerW / contentW);
    if(res < 40) res = 40;
    return res;
  }

  get vThumbPosition() {
    if(!this.contentInitiated) return 0;
    let scrollerH = this.scroller.element.nativeElement.clientHeight;
    let contentH = this.content.element.nativeElement.clientHeight;
    let scrollTop = this.scroller.element.nativeElement.scrollTop;
    return scrollerH * (scrollTop / contentH);
  }

  get hThumbPosition() {
    if(!this.contentInitiated) return 0;
    let scrollerW = this.scroller.element.nativeElement.clientWidth;
    let contentW = this.content.element.nativeElement.clientWidth;
    let scrollLeft = this.scroller.element.nativeElement.scrollLeft;
    return scrollerW * (scrollLeft / contentW);
  }

  get vThumbBig() {
    return this.isOverVTrack || this.draggingVThumb;
  }

  get hThumbBig() {
    return this.isOverHTrack || this.draggingHThumb;
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
  }
}


