import { Component, Input, OnInit } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { ValueComparator } from 'app/helpers';
import { ModalService } from 'app/services/modal.service';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-tree',
  templateUrl: './rb-tree.component.html',
  styleUrls: ['./rb-tree.component.css']
})
export class RbTreeComponent  extends RbDataObserverComponent  {
  @Input('displayattribute') displayattribute: string;
  @Input('mainattribute') mainattribute: string;
  @Input('subattribute') subattribute: string;
  @Input('meta1attribute') meta1attribute: string;
  @Input('meta2attribute') meta2attribute: string;
  @Input('parentattribute') parentattribute: string;
  @Input('childattribute') childattribute: string;
  
  open: RbObject[] = [];
  treeData: any[] = [];

  constructor(
    public userprefService: UserprefService,
    public modalService: ModalService
  ) {
    super();
  }

  dataObserverInit() {
    
  }

  dataObserverDestroy() {
    
  }

  onDatasetEvent(event: string) {
    if(event == 'load' || event == 'removed' || event == 'clear' || event == 'update') {
      this.redraw();
    }
  }

  onActivationEvent(state: boolean) {
    this.redraw();
  }

  public redraw() {
    this.treeData = [];
    let rootlist = this.list.filter(item => this.list.filter(parentitem => item.get(this.parentattribute) == parentitem.get(this.childattribute)).length == 0);
    for(let obj of rootlist) {
      this.treeData.push(this.recursiveRedraw(obj));
    }
    this.treeData.sort((a, b) => ValueComparator.valueCompare(a, b, "label", 1));
  }

  recursiveRedraw(obj: RbObject) {
    let main = obj.get(this.mainattribute ?? this.displayattribute);
    let meta1 = this.meta1attribute != null ? obj.get(this.meta1attribute) : null;
    let meta2 = this.meta2attribute != null ? obj.get(this.meta2attribute) : null;
    let sub = this.subattribute != null ? obj.get(this.subattribute) : null;
    let ret = {
      object: obj,
      meta1: meta1,
      meta2: meta2,
      main: main,
      sub: sub,
      open: this.open.indexOf(obj) > -1,
      children: []
    };
    let childrenList = this.list.filter(item => item.get(this.parentattribute) == obj.get(this.childattribute));
    for(let child of childrenList) {
      ret.children.push(this.recursiveRedraw(child));
    }
    ret.children.sort((a, b) => ValueComparator.valueCompare(a, b, "label", 1));
    return ret;
  }

  public selectNode(node: any) {
    this.dataset.select(node.object);
  }

  public toggleNode(node: any) {
    if(node.open) {
      node.open = false;
      this.open.splice(this.open.indexOf(node.object));
    } else {
      node.open = true;
      this.open.push(node.object);
    }
  }

}
