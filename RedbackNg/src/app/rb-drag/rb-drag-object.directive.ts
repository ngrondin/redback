import { Directive, HostListener, ElementRef, Input, Output, EventEmitter } from '@angular/core';
import { DragService } from 'app/services/drag.service';

@Directive({
  selector: '[rb-drag-object]'
})
export class RbDragObjectDirective {
  @Input('rb-drag-object') object: any;
  @Input('rb-drag-getdata') getData: any;
  @Input('rb-drag-droppedout') droppedout: any;

  constructor(
    private el: ElementRef,
    private dragService: DragService
  ) { }
    
  @HostListener('mousedown', ['$event']) onMouseDown($event) {
    let i = Math.floor(Math.random() * 100);
    let data = this.object != null ? this.object : this.getData != null ? this.getData() : null;
    if($event.which == 1 && data != null) {
      this.dragService.prepareDrag(this.el, data, $event, () => {
        if(this.droppedout != null) {
          this.droppedout({data: data, mouseEvent: $event});
        }
      }); 
    }
  }

  

}