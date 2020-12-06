import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'rb-menu-group',
  templateUrl: './rb-menu-group.component.html',
  styleUrls: ['./rb-menu-group.component.css']
})
export class RbMenuGroupComponent implements OnInit {
  @Input('config') config: any;
  @Input('mode') mode: any;
  @Output('navigate') navigate: EventEmitter<any> = new EventEmitter();


  _isOpen: boolean = false;

  constructor() { }

  ngOnInit(): void {
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

  navigateTo(event: any) {
    this.navigate.emit(event);
  }
}
