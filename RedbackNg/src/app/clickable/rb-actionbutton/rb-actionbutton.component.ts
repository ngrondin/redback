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
  @Input('param') param: string;
  @Input('timeout') timeout: number;
  @Input('confirm') confirm: string;
  
  constructor(
    private actionService: ActionService
  ) {
    super();
  }

  click() {
    if(this.dataset != null) {
      this.running = true;
      this.actionService.action(this.dataset, this.action, this.param, this.timeout, this.confirm).subscribe((rbObject) => {
        this.running = false;
      })
    }
  }
}


