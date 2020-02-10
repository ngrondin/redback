import { Component, OnInit, Input } from '@angular/core';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'rb-choice-input',
  templateUrl: './rb-choice-input.component.html',
  styleUrls: ['./rb-choice-input.component.css']
})
export class RbChoiceInputComponent implements OnInit {
  @Input('label') label: string;
  @Input('icon') icon: string;
  @Input('size') size: number;
  @Input('editable') editable: boolean;
  @Input('object') rbObject: RbObject;
  @Input('attribute') attribute: string;
  @Input('choicelist') choicelist: any;

  constructor() { }

  ngOnInit() {
  }

  public get widthString() : string {
    if(this.size != null)
      return (15*this.size) + 'px';
    else
      return '100%';
  }
}
