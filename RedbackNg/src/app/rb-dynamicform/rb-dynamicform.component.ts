import { Component, Input } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';

@Component({
  selector: 'rb-dynamicform',
  templateUrl: './rb-dynamicform.component.html',
  styleUrls: ['./rb-dynamicform.component.css']
})
export class RbDynamicformComponent extends RbDataObserverComponent {
  @Input('valueattribute') valueattribute : string;
  @Input('typeattribute') typeattribute : string;
  @Input('optionsattribute') optionsattribute : string;
  @Input('titleattribute') titleattribute : string;
  @Input('detailattribute') detailattribute : string;
  @Input('labelattribute') labelattribute : string;
  @Input('orderattribute') orderattribute : string;
  @Input('categoryattribute') categoryattribute : string;
  @Input('categoryorderattribute') categoryorderattribute : string;
  @Input('dependencyattribute') dependencyattribute : string;
  @Input('dependencyoperatorattribute') dependencyoperatorattribute : string;
  @Input('dependencyvalueattribute') dependencyvalueattribute : string;
  @Input('editable') editable : string;

  sortedVisibleList: RbObject[];
  isEditable: boolean;

  constructor() {
    super();
  }

  dataObserverInit() {
    this.evalEditable();
    this.calcSortedVisibleList();
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
    this.evalEditable();
    this.calcSortedVisibleList();
  }

  onActivationEvent(event: any) {
  }

  public get isLoading() : boolean {
    return this.dataset != null ? this.dataset.isLoading : false;
  }

  getTypeOf(object: RbObject) : string {
    if(this.typeattribute != null) {
      let type = object.get(this.typeattribute);
      const validTypes = ['string', 'textarea', 'choice', 'files', 'checkbox', 'signature', 'number', 'date', 'phone', 'address', 'email', 'currency', 'url', 'photos', 'videos', 'infoonly'];
      if(validTypes.indexOf(type) > -1) {
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
      if(this.sortedVisibleList[index - 1].get(this.categoryorderattribute) != object.get(this.categoryorderattribute)) {
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
            let depObj = obj.getRelated(this.dependencyattribute);
            if(depObj != null) {
              let dependentValue: any = obj.get(this.dependencyvalueattribute);
              let depObjVal: any = depObj.get(this.valueattribute);
              let depOperator = obj.get(this.dependencyoperatorattribute) || 'eq';
              if(depOperator == 'eq' && Array.isArray(dependentValue)) {
                for(let v of dependentValue) {
                  if(v == depObjVal) {
                    return true;
                  }
                }
              } else if(depOperator == 'eq') {
                if(dependentValue == depObjVal) {
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
            let acat = (a.get(this.categoryorderattribute) || '0').toString();
            let bcat = (b.get(this.categoryorderattribute) || '0').toString();
            let c = acat.localeCompare(bcat);
            if(c != 0) {
              return c;
            }
          }
          let aord = (a.get(this.orderattribute) || '0').toString();
          let bord = (b.get(this.orderattribute) || '0').toString();
          return aord.localeCompare(bord);
        });
      }
    } else {
      this.sortedVisibleList = [];
    }
  }

  public evalEditable() {
    if(this.editable == null) {
      this.isEditable = true;
    } else if(this.editable == 'true') {
      this.isEditable = true;
    } else if(this.editable == 'false') {
        this.isEditable = false;
    } else {
        let relatedObject = this.dataset != null ? this.dataset.relatedObject : null;
        if(!(this.editable.indexOf("relatedObject.") > -1 && relatedObject == null)) {
            this.isEditable = eval(this.editable);            
        } else {
            this.isEditable = false;
        }
    }
  }
}
