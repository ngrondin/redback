import { Directive, ElementRef, Output, EventEmitter, HostListener, Input } from '@angular/core';
import { DragService } from './drag.service';

@Directive({
  selector: '[rb-drag-dropzone]'
})
export class RbDragDropzoneDirective {

  constructor(
    private el: ElementRef,
    private dragService: DragService    
  ) { }

  //@Input('rb-drag-dropzone') inp: any;
  @Output('rb-drag-dropzone') dropped: EventEmitter<any> = new EventEmitter();

  @HostListener('mouseup', ['$event']) onMouseUp($event) {
    if(this.dragService.isDragging) {
      this.dropped.emit({
        object: this.dragService.object, 
        offset: this.dragService.offset,
        mouseEvent: $event
      });
      this.dragService.drop(this.el);
    }
  }

}
