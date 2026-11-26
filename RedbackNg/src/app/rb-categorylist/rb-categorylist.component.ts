import { Component, Input } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { RbObject } from 'app/datamodel';
import { RecalcPlanner } from 'app/helpers';
import { ListFieldConfig } from 'app/rb-list4/rb-list4.component';

@Component({
  selector: 'rb-categorylist',
  templateUrl: './rb-categorylist.component.html',
  styleUrls: ['./rb-categorylist.component.css']
})
export class RbCategorylistComponent extends RbDataObserverComponent {
  @Input('attribute') attribute?: string;
  @Input('expression') expression?: string;
  @Input('format') format?: string;
  @Input('clickscript') clickscript?: string;
  @Input('emptytext') emptytext: string = "No records";

  enhancedList: any[] = [];
  reachedBottom: boolean = false;
  click?: Function;

  label!: ListFieldConfig;

  recalcPlanner!: RecalcPlanner;

  dataObserverInit() {
    this.recalcPlanner = new RecalcPlanner(this.calcList.bind(this));
    this.label = new ListFieldConfig(this.attribute, this.expression, null, this.format);
    this.click = this.clickscript != null ? Function("event", this.clickscript) : undefined;
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
    if(this.active) {
      if(event.event == 'load' || event.event == 'removed' || event.event == 'clear' || event.event == 'update') {
        this.redraw();
      }
    }
  }

  onActivationEvent(state: boolean) {
    if(this.active) {
      this.redraw();
    }
  }

  isSelected(object: RbObject) : boolean {
    return this.dataset?.isObjectSelected(object) || false;
  }

  public redraw() {
    this.recalcPlanner.request();
  }

  public calcList() {
    this.enhancedList = [];
    for(let obj of this.list) {
      let value = this.label.getValue(obj);
      if(value == null || value.type == 'loading' || value.value == null || value.value == "") {
        continue;
      }
      this.enhancedList.push({
        object: obj,
        label: value
      });
    }
  }

  itemClicked(object: RbObject, event: any) {
    if(event.ctrlKey == true || event.metaKey == true) {
      this.dataset?.addOneToSelection(object);
    } else if(event.shiftKey == true) {
      this.dataset?.addRangeToSelection(object);
    } else {
      let alreadySelected = this.dataset?.isObjectSelected(object);
      if(!alreadySelected) this.dataset?.select(object);
      if(this.click != null) {
        let clickEvent = {alreadyselected: alreadySelected, object: object, dataset: this.dataset};
        this.click(clickEvent);
      }
    }
  }

  onScroll(event: any) {
    if(event.currentTarget.scrollTop > (event.currentTarget.scrollHeight - (1.5*event.currentTarget.clientHeight)) && this.reachedBottom == false) {
      this.reachedBottom = true;
      this.dataset?.fetchNextPage();
      setTimeout(() => {this.reachedBottom = false}, 1000);
    }
  }

}
