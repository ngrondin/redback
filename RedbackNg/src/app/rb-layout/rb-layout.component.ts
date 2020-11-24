import { Component, OnInit } from '@angular/core';
import { RbContainerComponent } from 'app/rb-container/rb-container.component';

@Component({
  selector: 'rb-layout',
  templateUrl: './rb-layout.component.html',
  styleUrls: ['./rb-layout.component.css']
})
export class RbLayoutComponent extends RbContainerComponent implements OnInit {

  constructor() {
    super();
  }

  ngOnInit(): void {
  }

}
