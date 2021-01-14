import { Component, OnInit, Input, SimpleChange } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';

@Component({
  selector: 'rb-dynamicform',
  templateUrl: './rb-dynamicform.component.html',
  styleUrls: ['./rb-dynamicform.component.css']
})
export class RbDynamicformComponent extends RbDataObserverComponent {
  @Input('dataset') dataset: RbDatasetComponent;
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

  constructor() {
    super();
  }

  get list(): RbObject[] {
    return this.dataset != null ? this.dataset.list : null;
  }

  dataObserverInit() {
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
  }

  onActivationEvent(event: any) {
  }

  ngOnChanges(changes : SimpleChange) {
    if('list' in changes) {
      this.calcSortedVisibleList();
    }
  }

  getTypeOf(object: RbObject) : string {
    if(this.typeattribute != null) {
      let type = object.get(this.typeattribute);
      if(type == 'string' || type == 'textarea' || type == 'choice' || type == 'files' || type == 'checkbox' || type == 'signature' || type == 'number' || type == 'date') {
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
            let acat = a.get(this.categoryorderattribute);
            let bcat = b.get(this.categoryorderattribute);
            let c = (acat | 0) - (bcat | 0); 
            if(c != 0) {
              return c;
            }
          }
          let aord = a.get(this.orderattribute);
          let bord = b.get(this.orderattribute);
          return (aord | 0) - (bord | 0); 
        });
      }
    } else {
      this.sortedVisibleList = [];
    }
  }
}
