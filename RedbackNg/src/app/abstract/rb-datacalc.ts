import { Component, Input } from "@angular/core";
import { RbObject } from "app/datamodel";
import { RbDataObserverComponent } from "./rb-dataobserver";

export class SeriesConfig {
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
    }
    
    dataObserverDestroy() {
    }
    
    onActivationEvent(event: any) {
        if(this.active) {
            if(this.dofilter) {
                this.filterDataset();
            } else {
                this.redraw();
            } 
        }
    }
    
    onDatasetEvent(event: any) {
        if(this.active) {
            //console.log(event + " " + this.id + "  " + (new Date()).getTime());
            this.redraw()
        }
    }

    redraw() {
        //console.log("redraw " + this.id + " " + (!this.recalcPlanned) + " - " + (new Date()).getTime());
        if(this.recalcPlanned == false) {
          this.recalcPlanned = true;
          setTimeout(() => {
              //console.log("redraw after timeout - " + (new Date()).getTime());
              this.calc();
              this.recalcPlanned = false;
          }, 50);
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

    abstract filterDataset();

    abstract calc();
    
}