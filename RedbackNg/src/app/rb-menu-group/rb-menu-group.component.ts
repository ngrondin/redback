import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NavigateService } from 'app/services/navigate.service';
//import { threadId } from 'worker_threads';

@Component({
  selector: 'rb-menu-group',
  templateUrl: './rb-menu-group.component.html',
  styleUrls: ['./rb-menu-group.component.css']
})
export class RbMenuGroupComponent implements OnInit {
  @Input('config') config: any;
  @Input('mode') mode: any;
  @Input('look') look: any = 'primary';
  //@Output('navigate') navigate: EventEmitter<any> = new EventEmitter();


  _isOpen: boolean = false;

  constructor(
    private navigateService: NavigateService
  ) { }

  ngOnInit(): void {
  }

  public get icon(): any {
    return this.config != null ? this.config.icon : null;
  }

  public get label(): any {
    return this.config != null ? this.config.label : null;
  }

  public get content(): any {
    if(this.config != null) {
      return this.config.content;
    } else {
      return null;
    }
  }

  public get isOpen() : boolean {
    return this._isOpen;
  }

  getTooltip() : string {
    return this.mode != 'large' ? this.config.label : null;
  }

  click() {
    if(this._isOpen == true) {
      this._isOpen = false;
    } else {
      this._isOpen = true;
    }
  }

  expand() {
    this._isOpen = true;
  }

  collapse() {
    this._isOpen = false;
  }

  /*navigateTo(event: any) {
    this.navigateService.navigateTo(event);
  }*/
}
