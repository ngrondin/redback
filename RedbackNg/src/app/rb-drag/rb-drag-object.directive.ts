import { Directive, HostListener, ElementRef, Input, Output, EventEmitter } from '@angular/core';
import { DragService } from 'app/services/drag.service';

@Directive({
  selector: '[rb-drag-object]'
})
export class RbDragObjectDirective {
  @Input('rb-drag-object') data: any;
  @Input('rb-drag-droppedout') droppedout: any;

  constructor(
    private el: ElementRef,
    private dragService: DragService
  ) { }
    
  @HostListener('mousedown', ['$event']) onMouseDown($event) {
    let i = Math.floor(Math.random() * 100);
    if($event.which == 1 && this.data != null) {
      this.dragService.prepareDrag(this.el, this.data, $event, () => {
        this.droppedout({data: this.data, mouseEvent: $event});
      }); 
    }
  }

  

}