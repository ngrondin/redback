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
    console.log("Start drag " + i);
    if($event.which == 1 && this.data != null) {
      this.dragService.prepareDrag(this.el, this.data, $event, () => {
        console.log('End drag ' + i);
        this.droppedout({data: this.data, mouseEvent: $event});
        //this.droppedout.emit({data: this.data, mouseEvent: $event});
      }); 
    }
  }

  

}