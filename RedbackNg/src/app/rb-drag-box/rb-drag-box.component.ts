import { Component } from '@angular/core';
import { DragService } from 'app/services/drag.service';

/**
 * The box that follows the mouse while something is dragged.
 * - One main box sized by the drag service (what snaps on drop).
 * - One faded box per part in dragService.parts, placed relative to the main box, so a
 *   multi-selection keeps its formation while it moves. Parts are set by the drop targets
 *   (see rb-drag-changeform) and cleared when the drag ends.
 */
@Component({
  selector: 'rb-drag-box',
  templateUrl: './rb-drag-box.component.html',
  styleUrls: ['./rb-drag-box.component.css']
})
export class RbDragBoxComponent {
  constructor(public dragService: DragService) { }
}
