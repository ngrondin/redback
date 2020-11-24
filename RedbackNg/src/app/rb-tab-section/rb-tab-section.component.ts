import { Input, ViewContainerRef } from '@angular/core';
import { Component, OnInit, ViewChild } from '@angular/core';
import { RbContainerComponent } from 'app/rb-container/rb-container.component';

@Component({
  selector: 'rb-tab-section-c',
  templateUrl: './rb-tab-section.component.html',
  styleUrls: ['./rb-tab-section.component.css']
})
export class RbTabSectionComponent extends RbContainerComponent implements OnInit {
  @Input('active') active : boolean;
  @Input('initiallyactive') initiallyActiveTabId: string;

  @ViewChild('container', { read: ViewContainerRef, static: true }) container: ViewContainerRef;
  
  constructor() {
    super();
  }

  ngOnInit(): void {
  }

}
