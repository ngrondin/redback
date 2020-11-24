import { Component, OnInit } from '@angular/core';
import { RbContainerComponent } from 'app/rb-container/rb-container.component';

@Component({
  selector: 'rb-hsection',
  templateUrl: './rb-hsection.component.html',
  styleUrls: ['./rb-hsection.component.css']
})
export class RbHsectionComponent extends RbContainerComponent implements OnInit {

  constructor() {
    super();
  }

  ngOnInit(): void {
  }

}
