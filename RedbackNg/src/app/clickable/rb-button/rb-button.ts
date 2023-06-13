import { Component, HostBinding, HostListener, Input, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { RbComponent } from 'app/abstract/rb-component';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';


@Component({
  selector: 'rb-button',
  templateUrl: './rb-button.html',
  styleUrls: ['./rb-button.css']
})
export class RbButtonComponent extends RbComponent {
  @Input('label') label: string;
  @Input('icon') icon: string;
  @Input('enabled') enabled: boolean = true;
  @Input('focus') focus: boolean = false;
  @Input('margin') margin: boolean = true;

  @HostBinding('class.rb-button-margin') get marginclass() { return this.margin }

  running: boolean = false;

  constructor(
  ) {
    super();
  }

  componentInit() {

  }
  componentDestroy() {

  }
  onActivationEvent(state: boolean) {

  }
}


