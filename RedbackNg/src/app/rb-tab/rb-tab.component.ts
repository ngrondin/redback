import { Component, OnInit } from '@angular/core';
import { RbContainerComponent } from 'app/rb-container/rb-container.component';

@Component({
  selector: 'rb-tab-c',
  templateUrl: './rb-tab.component.html',
  styleUrls: ['./rb-tab.component.css']
})
export class RbTabComponent extends RbContainerComponent implements OnInit {

  constructor() {
    super();
  }

  ngOnInit(): void {
  }

}
