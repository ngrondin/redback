import { Directive, HostListener, ElementRef, Input } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { DragService } from './drag.service';

@Directive({
  selector: '[rb-drag-object]'
})
export class RbDragObjectDirective {
  @Input('rb-drag-object') object: RbObject;

  constructor(
    private el: ElementRef,
    private dragService: DragService
  ) { }
    
  @HostListener('mousedown', ['$event']) onMouseDown($event) {
    if(this.object != null) {
      this.dragService.startDrag(this.el, this.object, $event); 
    }
  }

}
