import { Directive, ElementRef, HostListener, Input } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { DragService } from 'app/services/drag.service';

@Directive({
  selector: '[rb-drag-changeform]'
})
export class RbDragChangeformDirective {
  @Input('rb-drag-changeform') changeFormFunc: (object) => {};

  constructor(
    private el: ElementRef,
    private dragService: DragService    
  ) { }


  @HostListener('mouseenter', ['$event']) onMouseEnter($event) {
    if(this.dragService.isDragging && this.changeFormFunc != null) {
      let xProp = this.dragService.offset.x / this.dragService.size.x;
      let yProp = this.dragService.offset.y / this.dragService.size.y;
      let newForm: any = this.changeFormFunc(this.dragService.object);
      this.dragService.size.x = newForm.x;
      this.dragService.size.y = newForm.y;
      this.dragService.offset.x = xProp * newForm.x;
      this.dragService.offset.y = yProp * newForm.y;
    }
  }

}
