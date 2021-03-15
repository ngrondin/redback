import { Component, OnInit, Input, Output, EventEmitter, HostBinding } from '@angular/core';

@Component({
  selector: 'rb-breadcrumb',
  templateUrl: './rb-breadcrumb.component.html',
  styleUrls: ['./rb-breadcrumb.component.css']
})
export class RbBreadcrumbComponent implements OnInit {
  @Input('targetStack') targetStack : any[];
  @Input('color') color: string;
  @Output('backTo') back: EventEmitter<any> = new EventEmitter();
  @HostBinding('style.color') get foreColor() { return this.color != null ? this.color : "grey";}
  

  constructor() { }

  ngOnInit() {
  }

  backTo(target: any) {
    this.back.emit(target);
  }
}
