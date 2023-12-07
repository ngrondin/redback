import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { FunnelEntry, FunnelGroup, FunnelGroupConfig, FunnelPhase, FunnelPhaseConfig, FunnelSeriesConfig } from './rb-funnel-models';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-funnel',
  templateUrl: './rb-funnel.component.html',
  styleUrls: ['./rb-funnel.component.css']
})
export class RbFunnelComponent extends RbDataCalcComponent<FunnelSeriesConfig> {
  @Input('phases') phases: any;
  @Input('groups') groups: any;
  @Input('emptymessage') emptymessage: string = "Nothing to show";
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  phasesConfig: FunnelPhaseConfig = null;
  groupConfigs: any = {};
  
  data: any = [];

  constructor(
    private userPref: UserprefService
  ) {
    super();
  }

  
  dataCalcInit() {
    this.phasesConfig = new FunnelPhaseConfig(this.phases, this.userPref);
    for(var group of this.groups) {
      var cfg = new FunnelGroupConfig(group)
      this.groupConfigs[cfg.key] = cfg;
    }
  }

  dataCalcDestroy() {

  }

  createSeriesConfig(json: any): FunnelSeriesConfig {
    return new FunnelSeriesConfig(json, this.userPref);
  }

  getFilterSortForSeries(config: FunnelSeriesConfig) {
    return {};
  }

  calc() {
    this.data = [];
    let phaseList = this.lists[this.phasesConfig.dataset];
    for(var phaseObject of phaseList) {
      let phase = new FunnelPhase(phaseObject.uid, phaseObject.get(this.phasesConfig.labelAttribute), phaseObject, this.phasesConfig);
      var groups = {};
      for(let cfg of this.seriesConfigs) {
        var entryList = this.lists[cfg.dataset].filter(rbo => rbo.get(cfg.phaseAttribute) == phaseObject.get(this.phasesConfig.keyAttribute));
        for(let entryObject of entryList) {
          var groupKey = entryObject.get(cfg.groupAttribute);
          var groupCfg = this.groupConfigs[groupKey];
          if(groupCfg != null) {
            if(groups[groupKey] == null) groups[groupKey] = new FunnelGroup(groupKey, groupCfg.label, groupCfg.open);
            var color = cfg.colorMap[entryObject.get(cfg.colorAttribute)];
            if(color == null) color = "#888";
            let entry = new FunnelEntry(entryObject.uid, entryObject.get(cfg.labelAttribute), entryObject.get(cfg.subLabelAttribute), color, entryObject, cfg);
            groups[groupKey].entries.push(entry);  
          }
        }
      }
      for(var key of Object.keys(groups)) {
        if(groups[key].entries.length > 0) {
          phase.groups.push(groups[key]);
        }
      }
      phase.groups = phase.groups.sort((a, b) => this.groupConfigs[a.id].order - this.groupConfigs[b.id].order);
      this.data.push(phase);
    }
  }

  click(entry: FunnelEntry) {
    let object = entry.object;
    if(object != null) {
      let target = {};
      if(entry.config.linkView != null) {
        target['view'] = entry.config.linkView;
      } else {
        target['object'] = object.objectname;
      }
      if(entry.config.linkAttribute != null) {
        target['filter'] = {uid: "'" + object.get(entry.config.linkAttribute) + "'"};
      } else {
        target['filter'] = {uid: "'" + object.uid + "'"};
      }
      this.navigate.emit(target);
    }
  }

  clickGroup(group: FunnelGroup) {
    group.open = !group.open;
  }

  dropped($event, phase) {
    let object = $event.data.object;
    let objectPhaseAttribute = $event.data.config.phaseAttribute;
    let phaseKeyAttribute = phase.config.keyAttribute;
    let phaseKey = phase.object.get(phaseKeyAttribute);
    object.setValue(objectPhaseAttribute, phaseKey)
    console.log($event);
  }
}



