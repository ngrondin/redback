import { Directive, ElementRef, Output, EventEmitter, HostListener, Input } from '@angular/core';
import { DragService } from 'app/services/drag.service';

@Directive({
  selector: '[rb-drag-dropzone]'
})
export class RbDragDropzoneDirective {

  constructor(
    private el: ElementRef,
    private dragService: DragService    
  ) { }

  @Output('rb-drag-dropzone') dropped: EventEmitter<any> = new EventEmitter();

  @HostListener('mouseup', ['$event']) onMouseUp($event) {
    if(this.dragService.isDragging) {
      this.dropped.emit({
        data: this.dragService.data, 
        offset: this.dragService.offset,
        mouseEvent: $event
      });
      this.dragService.drop(this.el);
    }
  }

}
