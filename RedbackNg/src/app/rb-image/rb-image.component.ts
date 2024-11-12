import { Component, HostBinding, Input } from '@angular/core';

@Component({
  selector: 'rb-image',
  templateUrl: './rb-image.component.html',
  styleUrls: ['./rb-image.component.css']
})
export class RbImageComponent {
  @Input("name") name: string;
  @Input("size") size: number;

  @HostBinding('style.width') 
  get styleWidth() { 
    return this.size != null ? ((0.88 * this.size) + 'vw') : "";
  }

  
  get src() : string {
    return "/rbui/img/" + this.name;
  }

}
