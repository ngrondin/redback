import { Component, Input, OnInit } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';

@Component({
  selector: 'rb-button',
  templateUrl: './rb-button.component.html',
  styleUrls: ['./rb-button.component.css']
})
export class RbButtonComponent extends RbDataObserverComponent {
  @Input('dataset') dataset: RbDatasetComponent;
  @Input('label') label: string;
  @Input('action') action: string;
  @Input('param') param: string;

  constructor() {
    super();
  }

  dataObserverInit() {
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
  }

  onActivationEvent(event: any) {
  }

  public click() {
    if(this.dataset != null) {
      this.dataset.action(this.action, this.param);
    }
  }

}
