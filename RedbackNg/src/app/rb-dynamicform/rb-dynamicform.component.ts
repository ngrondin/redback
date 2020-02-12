import { Component, OnInit, Input } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { stringify } from 'querystring';

@Component({
  selector: 'rb-dynamicform',
  templateUrl: './rb-dynamicform.component.html',
  styleUrls: ['./rb-dynamicform.component.css']
})
export class RbDynamicformComponent implements OnInit {
  @Input() list : RbObject[];
  @Input() valueattribute : string;
  @Input() typeattribute : string;
  @Input() optionsattribute : string;
  @Input() titleattribute : string;
  @Input() detailattribute : string;
  @Input() labelattribute : string;
  @Input() orderattribute : string;
  @Input() categoryattribute : string;
  @Input() categoryorderattribute : string;
  @Input() dependencyattribute : string;
  @Input() dependencyvalueattribute : string;
  @Input() editable : boolean;

  constructor() { }

  ngOnInit() {
  }

  getTypeOf(object: RbObject) : string {
    if(this.typeattribute != null) {
      let type = object.get(this.typeattribute);
      if(type == 'string' || type == 'textarea' || type == 'choice' || type == 'files' || type == 'checkbox' || type == 'signature' || type == 'number') {
        return type;
      } else {
        return 'unknown';
      }
    } else {
      return 'string';
    }
  }

  isFirstInCategory(object: RbObject) : boolean {
    let index = this.list.indexOf(object);
    if(index == 0) {
      return true;
    } else {
      if(this.list[index - 1].get(this.categoryattribute) != object.get(this.categoryattribute)) {
        return true;
      } else {
        return false;
      }
    }
  }

  getOrderedList() {
    if(this.list != null) {
      if(this.orderattribute != null) {
        return this.list.sort((a, b) => {
          let c = a.get(this.categoryorderattribute) - b.get(this.categoryorderattribute);
          if(c != 0) {
            return c;
          } else {
            a.get(this.orderattribute) - b.get(this.orderattribute)
          }
        });
      } else {
        return this.list;
      }
    } else {
      return null;
    }
  }
}
