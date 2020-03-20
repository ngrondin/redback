import { Component, OnInit, Input, SimpleChange } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { stringify } from 'querystring';

@Component({
  selector: 'rb-dynamicform',
  templateUrl: './rb-dynamicform.component.html',
  styleUrls: ['./rb-dynamicform.component.css']
})
export class RbDynamicformComponent implements OnInit {
  @Input() list : RbObject[];
  @Input('isLoading') isLoading: any;
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

  sortedVisibleList: RbObject[];

  constructor() { }

  ngOnInit() {
  }

  ngOnChanges(changes : SimpleChange) {
    if('list' in changes) {
      this.calcSortedVisibleList();
    }
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
    let index = this.sortedVisibleList.indexOf(object);
    if(index == 0) {
      return true;
    } else {
      if(this.sortedVisibleList[index - 1].get(this.categoryattribute) != object.get(this.categoryattribute)) {
        return true;
      } else {
        return false;
      }
    }
  }

  lookupObjectFromList(attribute: string, value: string) : RbObject {
    for(let obj of this.list) {
      if(obj.get(attribute) == value) {
        return obj;
      }
    }
    return null;
  }

  calcSortedVisibleList() {
    if(this.list != null) {
      this.sortedVisibleList = this.list;
      if(this.dependencyattribute != null) {
        this.sortedVisibleList = this.list.filter(obj => {
          let dependentLinkValue = obj.get(this.dependencyattribute);
          if(dependentLinkValue != null) {
            let dependentLinkAttribute = (this.dependencyattribute.indexOf(".") > -1 ? this.dependencyattribute.substr(0, this.dependencyattribute.indexOf(".")) : "uid");
            let depObj = this.lookupObjectFromList(dependentLinkAttribute, dependentLinkValue);
            if(depObj != null) {
              let dependentValues: any[] = obj.get(this.dependencyvalueattribute);
              let depObjVal: any = depObj.get(this.valueattribute);
              for(let v of dependentValues) {
                if(v == depObjVal) {
                  return true;
                }
              }
              return false;
            } else {
              return false;
            }
          } else {
            return true;
          }
        });
      }

      if(this.orderattribute != null) {
        this.sortedVisibleList = this.sortedVisibleList.sort((a, b) => {
          if(this.categoryattribute != null) {
            let c = a.get(this.categoryorderattribute) - b.get(this.categoryorderattribute);
            if(c != 0) {
              return c;
            }
          }
          return a.get(this.orderattribute) - b.get(this.orderattribute);
        });
      }
    } else {
      this.sortedVisibleList = [];
    }
  }
}
