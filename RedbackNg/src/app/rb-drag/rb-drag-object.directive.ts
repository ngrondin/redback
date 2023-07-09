import { Directive, HostListener, ElementRef, Input } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { DragService } from 'app/services/drag.service';

@Directive({
  selector: '[rb-drag-object]'
})
export class RbDragObjectDirective {
  @Input('rb-drag-object') data: any;

  constructor(
    private el: ElementRef,
    private dragService: DragService
  ) { }
    
  @HostListener('mousedown', ['$event']) onMouseDown($event) {
    if($event.which == 1 && this.data != null) {
      this.dragService.prepareDrag(this.el, this.data, $event); 
    }
  }

}