import { Component, HostBinding, HostListener, Input, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';


@Component({
  selector: 'rb-button',
  templateUrl: './rb-button.html',
  styleUrls: ['./rb-button.css']
})
export class RbButtonComponent extends RbDataObserverComponent {
  @Input('label') label: string;
  @Input('focus') _focus: boolean = false;
  @Input('margin') margin: boolean = true;

  @ViewChild('button', { read: ViewContainerRef }) buttonContainerRef: ViewContainerRef;

  @HostBinding('class.rb-button-margin') get marginclass() { return this.margin }

  actionning: boolean = false;

  constructor(
  ) {
    super();
  }

  dataObserverInit() {
  }
  dataObserverDestroy() {
  }
  onDatasetEvent(event: string) {
  }
  onActivationEvent(state: boolean) {
  }
  
  get focus() : boolean {
    return this._focus;
  }

  click() {}

  @HostListener('click', ['$event']) _onclick($event) {
    this.click();
  }
}


