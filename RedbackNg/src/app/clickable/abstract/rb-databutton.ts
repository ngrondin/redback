import { Component, HostBinding, HostListener, Input, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';


@Component({template: ''})
export class RbDataButtonComponent extends RbDataObserverComponent {
  @Input('label') label: string;
  @Input('icon') icon: string;
  @Input('enabled') _enabled: boolean = true;
  @Input('focus') _focus: boolean = false;
  @Input('margin') margin: boolean = true;

  @ViewChild('button', { read: ViewContainerRef }) buttonContainerRef: ViewContainerRef;

  @HostBinding('class.rb-button-margin') get marginclass() { return this.margin }

  running: boolean = false;

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

  get enabled() : boolean {
    return this._enabled;
  }
  
  get focus() : boolean {
    return this._focus;
  }

  click() {}

  @HostListener('click', ['$event']) _onclick($event) {
    this.click();
  }
}


