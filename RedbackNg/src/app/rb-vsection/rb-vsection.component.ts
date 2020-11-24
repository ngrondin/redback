import { Component, OnInit } from '@angular/core';
import { RbContainerComponent } from 'app/rb-container/rb-container.component';

@Component({
  selector: 'rb-vsection',
  templateUrl: './rb-vsection.component.html',
  styleUrls: ['./rb-vsection.component.css']
})
export class RbVsectionComponent extends RbContainerComponent implements OnInit {

  constructor() {
    super();
  }

  ngOnInit(): void {
  }

}
