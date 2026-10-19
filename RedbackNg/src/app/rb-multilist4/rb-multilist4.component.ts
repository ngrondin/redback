import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { MultiList4SeriesConfig } from './rb-multilist4-model';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { NavigateEvent, RbObject, RELATED_LOADING } from 'app/datamodel';
import { ColorConfig, Evaluator, Formatter, RecalcPlanner, VAEConfig } from 'app/helpers';
import { ApiService } from 'app/services/api.service';
import { ModalService } from 'app/services/modal.service';
import { NavigateService } from 'app/services/navigate.service';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-multilist4',
  templateUrl: '../rb-list4/rb-list4.component.html',
  styleUrls: ['../rb-list4/rb-list4.component.css']
})
export class RbMultilist4Component extends RbDataCalcComponent<MultiList4SeriesConfig> {
  @Input('showrefresh') showrefresh: boolean = true;
  @Input('emptytext') emptytext: string = "No records";
  
  enhancedList: any[] = [];
  isoDateRegExp: RegExp = /\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d(\.\d+|)([+-][0-2]\d:[0-5]\d|Z)/;
  reachedBottom: boolean = false;
  enhanceDragDataCallback?: Function;
  click?: Function;
  
  constructor(
    public userprefService: UserprefService,
    public apiService: ApiService,
    public modalService: ModalService,
    public navigateService: NavigateService
  ) {
    super();
  }
  
  dataCalcInit() {

  }

  dataCalcDestroy() {

  }

  createSeriesConfig(json: any): MultiList4SeriesConfig {
    return new MultiList4SeriesConfig(json);
  }

  getFilterSortForSeries(cfg: MultiList4SeriesConfig) : any {
    return {};
  }
  
  calc() {
    this.enhancedList = [];
    for (let cfg of this.seriesConfigs) {
      let dataset = this.getDatasetForConfig(cfg);
      if(dataset != null) {
        for(var obj of dataset.list) {
          let data: any = {
            object: obj,
            main: cfg.main.getValue(obj),
            sub: cfg.sub.getValue(obj),
            meta1: cfg.meta1.getValue(obj),
            meta2: cfg.meta2.getValue(obj),
            color: cfg.backColor.getColor(obj) || 'transparent',
            allowdrag: cfg.allowdrag
          }
    
          if(cfg.imageattribute != null) {
            let fileVal = obj.get(cfg.imageattribute);
            if(fileVal != null) {
              data.image = 'url(\'' + fileVal.thumbnail + '\')';
            } else {
              data.image = '';
            }
          }
    
          if(data.main.type != 'loading' && (data.main.value == null || data.main.value == "")) {
            if(data.sub.value != null && data.sub.value != "") {
              data.main.value = data.sub.value;
              data.sub.value = "";
            } else {
              data.main.value = "No Label"
            }
          }
          this.enhancedList.push(data);
        }
      }
    }
  }

  isSelected(object: RbObject) : boolean {
    return this.dataset?.isObjectSelected(object) || false;
  }
  
  showCount() : boolean {
    return this.showrefresh && this.dataset != null && this.dataset.totalCount > 10;
  }

  getCountText() : string | null {
    return this.dataset != null ? this.dataset.totalCount.toString() : null;
  }

  showRefresh() : boolean {
    return this.showrefresh;
  }

  itemClicked(object: RbObject, event: any) {
    let cfg = this.getSeriesConfigForObject(object);
    let dataset = this.getDatasetForConfig(cfg)
    if(dataset != null) {
      if(event.ctrlKey == true || event.metaKey == true) {
        dataset.addOneToSelection(object);
      } else if(event.shiftKey == true) {
        dataset.addRangeToSelection(object);
      } else {
        let alreadySelected = dataset.isObjectSelected(object);
        if(!alreadySelected) dataset.select(object);
        if(cfg.clickscript != null) {
          let event = {alreadyselected: alreadySelected, object: object, dataset: this.dataset};
          cfg.clickscript(event);
        }
        if(cfg.modal != null) {
          this.modalService.open(cfg.modal);
        } else if(cfg.link != null) {
          this.navigateService.navigateTo({
            objectname: object.objectname,
            datatargets:[{
              filter: {uid: "'" + object.uid + "'"}
            }]
          });
        }
      }
    }

  }

  public enhanceDragData(object: RbObject) : any {
    if(object != null && this.dataset != null) {
      return [object].concat(this.dataset.selectedObjects.filter(o => o.uid != object.uid));
    } else {
      return null
    }
  }

  refresh() {
    this.dataset?.refreshData(true);
  }

  onScroll(event: any) {
    if(event.currentTarget.scrollTop > (event.currentTarget.scrollHeight - (1.5*event.currentTarget.clientHeight)) && this.reachedBottom == false) {
      this.reachedBottom = true;
      this.dataset?.fetchNextPage();
      setTimeout(() => {this.reachedBottom = false}, 1000);
    }
  }  
}
