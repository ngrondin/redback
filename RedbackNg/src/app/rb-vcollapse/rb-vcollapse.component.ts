import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'rb-vcollapse',
  templateUrl: './rb-vcollapse.component.html',
  styleUrls: ['./rb-vcollapse.component.css']
})
export class RbVcollapseComponent implements OnInit {
  open: boolean = false;

  constructor() { }

  ngOnInit(): void {
  }

  public get isOpen() {
    return this.open;
  }

  public toggle() {
    this.open = !this.open;
  }

}
