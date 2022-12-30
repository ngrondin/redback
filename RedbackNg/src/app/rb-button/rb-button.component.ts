import { Component, Input, OnInit } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { ActionService } from 'app/services/action.service';

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
  @Input('timeout') timeout: number;
  @Input('confirm') confirm: string;

  constructor(
    private actionService: ActionService
  ) {
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
      this.actionService.action(this.dataset, this.action, this.param, this.timeout, this.confirm).subscribe((rbObject) => {
      })
    }
  }

}
