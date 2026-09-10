import { Directive, HostListener, ElementRef, Input, Output, EventEmitter } from '@angular/core';
import { DragService } from 'app/services/drag.service';

@Directive({
  selector: '[rb-drag-object]'
})
export class RbDragObjectDirective {
  @Input('rb-drag-object') object: any;
  @Input('rb-drag-enhancedata') enhanceData: any;
  @Input('rb-drag-getform') getForm: any;
  @Input('rb-drag-droppedout') droppedout: any;

  constructor(
    private el: ElementRef,
    private dragService: DragService
  ) { }
    
  @HostListener('mousedown', ['$event']) onMouseDown($event) {
    let i = Math.floor(Math.random() * 100);
    let data = this.enhanceData != null ? this.enhanceData(this.object) : this.object;
    if($event.which == 1 && data != null) {
      this.dragService.prepareDrag(this.el, data, $event, () => {
        if(this.droppedout != null) {
          this.droppedout({data: data, mouseEvent: $event});
        }
      });
     if(this.getForm != null) {
       let newForm: any = this.getForm(data);
       this.dragService.setNewForm(newForm);
     } 
    }
    $event.stopPropagation();
  }
}
