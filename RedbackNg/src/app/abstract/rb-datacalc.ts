import { ThisReceiver } from "@angular/compiler";
import { Component, Input } from "@angular/core";
import { RbObject } from "app/datamodel";
import { RbDataObserverComponent } from "./rb-dataobserver";
import { RbDatasetComponent } from "app/rb-dataset/rb-dataset.component";
import { LogService } from "app/services/log.service";
import { AppInjector } from "app/app.module";

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
    initialFilteringDone: boolean = false;
    lastRecalc: number = -1;
    recalcInterval: number = -1;
    minRecalcTime: number = -1;
    recalcPlanned: boolean = false;
    private _logService: LogService;
    
    constructor(
    ) {
        super();
        this._logService = AppInjector.get(LogService);
    }
    
    dataObserverInit() {
        if(this.series != null) {
            this.seriesConfigs = [];
            for(let item of this.series) {
                this.seriesConfigs.push(this.createSeriesConfig(item));
            }
        }        
        this.dataCalcInit();
        //this.updateData();
    }
    
    dataObserverDestroy() {
    }
    
    onActivationEvent(event: any) {
        this._logService.debug("DataCalc " + this.id + ": Activation (" + event + ")");
        if(this.active) {
            this.updateData();
        }
    }
    
    onDatasetEvent(event: any) {
        if(this.active) {
            this._logService.debug("DataCalc " + this.id + ": Dataset Event (" + event.dataset.id + "." + event.event + ", list=" + event.dataset.list.length + (event.event == 'load' ? ", filter=" + JSON.stringify(event.dataset.resolvedFilter) : "") + ")");
            this.redraw()
        }
    }

    get activeSeries() : T[] {
        return this.seriesConfigs.filter(item => item.active);
    }


    updateData(forceFilter: boolean = false) : boolean {
        if(this.dofilter && (!this.initialFilteringDone || forceFilter)) {
            this._logService.debug("DataCalc " + this.id + ": updateData");
            this.initialFilteringDone = true;
            let fetched = false;
            for(let cfg of this.activeSeries) {
              let filterSort = this.getFilterSortForSeries(cfg);
              if(filterSort != null) {
                if(this.datasetgroup != null && this.datasetgroup.datasets[cfg.dataset] != null) {
                    fetched = this.datasetgroup.datasets[cfg.dataset].filterSort(filterSort) || fetched;
                } else if(this.dataset != null) {
                    fetched = this.dataset.filterSort(filterSort) || fetched;
                } else {
                    fetched = false;
                }
              }
            }
            fetched = this.updateOtherData() || fetched;
            if(!fetched) {
                this.redraw();
            }
            return fetched;
        } else {
            this.redraw();
            return false;
        }        
    }

    updateOtherData() : boolean {
        return true;
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
        //this._logService.debug("DataCalc " + this.id + ": Redraw (" + !this.recalcPlanned + ")");
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
            try {
                var start = (new Date()).getTime();
                this._logService.debug("DataCalc " + this.id + ": start calc");
                this.calc();
                this.recalcPlanned = false;
                this.lastRecalc = (new Date()).getTime();
                let end = (new Date()).getTime();
                this._logService.debug("DataCalc " + this.id + ": finished calc in " + (end-start) + "ms");
            } catch(err) {
                this._logService.error(err);
            }
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
        for(var cfg of this.seriesConfigs) {
            let dataset = this.getDatasetForConfig(cfg);
            if(dataset.contains(object)) {
                return cfg;
            }
        }
        return null;
    }

    getDatasetForObject(object: RbObject) : RbDatasetComponent {
        for(var cfg of this.seriesConfigs) {
            let dataset = this.getDatasetForConfig(cfg);
            if(dataset.contains(object)) {
                return dataset;
            }
        }
        return null;
    }

    getDatasetForConfig(cfg: SeriesConfig) {
        return this.datasetgroup != null ? this.datasetgroup.datasets[cfg.dataset] : this.dataset;
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