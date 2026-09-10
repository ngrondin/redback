import { Directive, ElementRef, HostListener, Input } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { DragService } from 'app/services/drag.service';

@Directive({
  selector: '[rb-drag-changeform]'
})
export class RbDragChangeformDirective {
  @Input('rb-drag-changeform') changeFormFunc: (data) => {};

  constructor(
    private el: ElementRef,
    private dragService: DragService    
  ) { }


  @HostListener('mouseenter', ['$event']) onMouseEnter($event) {
    if(this.dragService.isDragging && this.changeFormFunc != null) {
      let newForm: any = this.changeFormFunc(this.dragService.data);
      this.dragService.setNewForm(newForm);
    }
  }

}
