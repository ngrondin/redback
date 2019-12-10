import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'rb-search',
  templateUrl: './rb-search.component.html',
  styleUrls: ['./rb-search.component.css']
})
export class RbSearchComponent implements OnInit {

  @Input('icon') icon: string;
  @Input('size') size: Number;

  constructor() { }

  ngOnInit() {
  }

}
