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
            this.redraw()
        }
    }

    redraw() {
        if(this.recalcPlanned == false) {
          this.recalcPlanned = true;
          setTimeout(() => {
              this.calc();
              this.recalcPlanned = false;
          }, 250);
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
        if(this.lists != null) {
            for(let cfg of this.seriesConfigs) {
                if(this.lists[cfg.dataset].object == object.objectname) {
                    return cfg;
                }
            }
        } else {
            return this.seriesConfigs[0];
        }
        return null;
    }

    abstract dataCalcInit();

    abstract dataCalcDestroy();

    abstract createSeriesConfig(json: any) : T;

    abstract filterDataset();

    abstract calc();
    
}