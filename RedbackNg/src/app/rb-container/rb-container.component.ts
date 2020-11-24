import { AfterViewInit } from '@angular/core';
import { Component, OnInit, ViewChild, ViewContainerRef } from '@angular/core';

@Component({
  selector: 'app-rb-container',
  templateUrl: './rb-container.component.html',
  styleUrls: ['./rb-container.component.css']
})
export class RbContainerComponent implements OnInit, AfterViewInit {
  private afterViewInitCallback;
  public container: ViewContainerRef;

  constructor() { }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    if(this.afterViewInitCallback != null) {
      this.afterViewInitCallback();
    }
  }

  public afterViewInit(cb) {
    this.afterViewInitCallback = cb;
  }


  public getContainer() : ViewContainerRef {
    return this.container;
  }

}
