import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'rb-breadcrumb',
  templateUrl: './rb-breadcrumb.component.html',
  styleUrls: ['./rb-breadcrumb.component.css']
})
export class RbBreadcrumbComponent implements OnInit {
  @Input() targetStack : any[];

  @Output('backTo') back: EventEmitter<any> = new EventEmitter();


  constructor() { }

  ngOnInit() {
  }

  backTo(target: any) {
    this.back.emit(target);
  }
}
