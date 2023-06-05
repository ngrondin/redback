import { Component, OnInit, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'rb-popup',
  templateUrl: './rb-popup.component.html',
  styleUrls: ['./rb-popup.component.css']
})
export abstract class RbPopupComponent implements OnInit {
  
  @Output() selected: EventEmitter<any> = new EventEmitter();
  @Output() cancelled: EventEmitter<any> = new EventEmitter();

  constructor() { }

  ngOnInit(): void {
  }

  public abstract getHighlighted() : any;

  public abstract setSearch(val: String);

  public abstract keyTyped(keyCode: number);

  public onOverlayClick() {
    this.cancelled.emit();
  }

}
