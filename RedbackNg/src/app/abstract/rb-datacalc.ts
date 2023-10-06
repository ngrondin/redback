import { ThisReceiver } from "@angular/compiler";
import { Component, Input } from "@angular/core";
import { RbObject } from "app/datamodel";
import { RbDataObserverComponent } from "./rb-dataobserver";

export class SeriesConfig {
    active: boolean = true;
    dataset: string;

    constructor(json: any) {
        this.dataset = json.dataset;
    }
}

@Component({template: ''})
export abstract class RbDataCalcComponent<T extends SeriesConfig> extends RbDataObserverComponent {
    @Input('series') series: any[];
    @Input('dofilter') dofilter: boolean = true;

    seriesConfigs: T[] = [];
    filterDone: boolean = false;
    lastRecalc: number = -1;
    recalcInterval: number = -1;
    minRecalcTime: number = -1;
    recalcPlanned: boolean = false;

    
    constructor(
    ) {
        super();
    }
    
    dataObserverInit() {
        if(this.series != null) {
            this.seriesConfigs = [];
            for(let item of this.series) {
                this.seriesConfigs.push(this.createSeriesConfig(item));
            }
        }        
        this.dataCalcInit();
        this.updateData();
    }
    
    dataObserverDestroy() {
    }
    
    onActivationEvent(event: any) {
        if(this.active) {
            this.updateData();
        }
    }
    
    onDatasetEvent(event: any) {
        if(this.active) {
            //console.log(event + " " + this.id + "  " + (new Date()).getTime());
            this.redraw()
        }
    }


    get activeSeries() : T[] {
        return this.seriesConfigs.filter(item => item.active);
    }


    updateData(forceFilter: boolean = false) : boolean {
        if(this.dofilter && (!this.filterDone || forceFilter)) {
            this.filterDone = true;
            let fetched = true;
            for(let cfg of this.activeSeries) {
              let filterSort = this.getFilterSortForSeries(cfg);
              if(this.datasetgroup != null) {
                fetched = fetched && this.datasetgroup.datasets[cfg.dataset].filterSort(filterSort);
              } else {
                fetched = fetched && this.dataset.filterSort(filterSort);
              }
            }
            if(!fetched) {
                this.redraw();
            }
            return fetched;
        } else {
            this.redraw();
            return false;
        }        
    }

    forceDatasetReload() {
        if(this.dataset != null) {
          this.dataset.refreshData();
        }
        if(this.datasetgroup != null) {
          this.datasetgroup.refreshAllData();
        }
    }

    redraw() {
        if(this.recalcPlanned == false) {
          this.recalcPlanned = true;
          let now = new Date().getTime();
          let nextCalc = now;
          if(this.recalcInterval > -1) {
            nextCalc = this.lastRecalc + (this.recalcInterval * Math.ceil((now - this.lastRecalc) / this.recalcInterval));
          } else if(this.minRecalcTime > -1) {
            nextCalc = this.lastRecalc + this.minRecalcTime;
          }
          let tillNextCalc = Math.max(nextCalc, now) - now
          setTimeout(() => {
              this.calc();
              this.recalcPlanned = false;
              this.lastRecalc = (new Date()).getTime();
          }, tillNextCalc);
        }
      }

    iterateAllLists(callback: (object: RbObject, config: T) => void) {
        for(let seriesConfig of this.seriesConfigs) {
            this.iterateList(seriesConfig, callback);
        }
    }

    iterateList(config: T, callback: (object: RbObject, config: T) => void) {
        let list = this.getList(config);
        if(list != null) {
            for(let i = 0; i < list.length; i++) {
                callback(list[i], config);
            }
        }
    }

    getList(config: T) : RbObject[] {
        return this.lists != null ? this.lists[config.dataset] : this.list;
    }

    getSeriesConfigForObject(object: RbObject) : T {
        if(this.datasetgroup != null) {
            for(let cfg of this.seriesConfigs) {
                if(this.datasetgroup.datasets[cfg.dataset].objectname == object.objectname) {
                    return cfg;
                }
            }
        } else {
            return this.seriesConfigs[0];
        }
        return null;
    }

    containsObject(object: RbObject) : boolean {
        let ret: boolean = false;
        if(this.datasetgroup != null) {
            for(let cfg of this.seriesConfigs) {
                if(this.datasetgroup.datasets[cfg.dataset].objectname == object.objectname) {
                    if(this.datasetgroup.datasets[cfg.dataset].list.indexOf(object) > -1) {
                        return true;
                    }
                }
            }
        } else if(this.dataset != null) {
            if(this.dataset.list.indexOf(object) > -1) {
                return true;
            }
        }
        return false;
    }

    abstract dataCalcInit();

    abstract dataCalcDestroy();

    abstract createSeriesConfig(json: any) : T;

    abstract getFilterSortForSeries(config: T) : any;

    abstract calc();
    
}