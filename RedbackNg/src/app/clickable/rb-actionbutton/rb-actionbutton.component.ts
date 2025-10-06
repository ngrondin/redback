import { Component, Input } from '@angular/core';
import { RbButtonComponent } from 'app/clickable/rb-button/rb-button';
import { ActionService } from 'app/services/action.service';
import { RbDataButtonComponent } from '../abstract/rb-databutton';

@Component({
  selector: 'rb-actionbutton',
  templateUrl: '../rb-button/rb-button.html',
  styleUrls: ['../rb-button/rb-button.css']
})
export class RbActionButtonComponent extends RbDataButtonComponent {
  @Input('action') action: string;
  @Input('target') target: string;
  @Input('param') param: string;
  @Input('confirm') confirm: string;
  @Input('timeout') timeout: number;
  
  constructor(
    public actionService: ActionService
  ) {
    super();
  }

  click() {
    this.running = true;
    this.actionService.action(this.dataset, this.datasetgroup, this.action, this.target, this.param, null, this.confirm, this.timeout).subscribe((rbObject) => {
      this.running = false;
    })
  }
}


