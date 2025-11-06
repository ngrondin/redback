import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { FunnelEntry, FunnelGroup, FunnelGroupConfig, FunnelPhase, FunnelPhaseConfig, FunnelPhaseGroup, FunnelPhaseGroupConfig, FunnelSeriesConfig } from './rb-funnel-models';
import { UserprefService } from 'app/services/userpref.service';
import { ValueComparator } from 'app/helpers';
import { NavigateService } from 'app/services/navigate.service';
import { NavigateEvent } from 'app/datamodel';

@Component({
  selector: 'rb-funnel',
  templateUrl: './rb-funnel.component.html',
  styleUrls: ['./rb-funnel.component.css']
})
export class RbFunnelComponent extends RbDataCalcComponent<FunnelSeriesConfig> {
  @Input('groups') groups: any;
  @Input('phases') phases: any;
  @Input('phasegroups') phasegroups: any;
  @Input('emptymessage') emptymessage: string = "Nothing to show";
  //@Output() navigate: EventEmitter<any> = new EventEmitter();

  phaseGroupConfig: FunnelPhaseGroupConfig = null;
  phasesConfig: FunnelPhaseConfig = null;
  groupConfigs: any = {};
  
  data: FunnelPhaseGroup[] = [];

  colorScheme = ['#EA6A47', '#B54051', '#40B569', '#ADABA7'];

  constructor(
    private userPref: UserprefService,
    private navigateService: NavigateService
  ) {
    super();
  }

  get pref() : any {
    return this.id != null ? (this.userPref.getCurrentViewUISwitch('funnel', this.id) || {phasegroupstate:{}}) : null;
  }

  dataCalcInit() {
    this.phaseGroupConfig = this.phasegroups != null ? new FunnelPhaseGroupConfig(this.phasegroups, this.pref) : null;
    this.phasesConfig = new FunnelPhaseConfig(this.phases, this.pref);
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
    if(this.phaseGroupConfig != null) {
      for(var phaseGroupObject of this.lists[this.phaseGroupConfig.dataset]) {
        const isOpen = this.pref != null && this.pref.phasegroupstate != null && this.pref.phasegroupstate[phaseGroupObject.uid] != null ? this.pref.phasegroupstate[phaseGroupObject.uid] : true;
        this.data.push(new FunnelPhaseGroup(phaseGroupObject.uid, phaseGroupObject.get(this.phaseGroupConfig.labelAttribute), phaseGroupObject.get(this.phaseGroupConfig.orderAttribute), this.colorScheme[this.data.length % this.colorScheme.length], isOpen, phaseGroupObject, this.phaseGroupConfig));
      }
    }
    this.data.push(new FunnelPhaseGroup(null, null, 0, "", true, null, this.phaseGroupConfig));
    for(var phaseGroup of this.data) {
      let phaseList = this.lists[this.phasesConfig.dataset].filter(rbo => rbo.get(this.phasesConfig.groupAttribute) == phaseGroup.id);
      for(var phaseObject of phaseList) {
        let phase = new FunnelPhase(phaseObject.uid, phaseObject.get(this.phasesConfig.labelAttribute), phaseObject.get(this.phasesConfig.orderAttribute), phaseObject, this.phasesConfig);
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
        phaseGroup.phases.push(phase);

      } 
      phaseGroup.phases = phaseGroup.phases.sort((a, b) => ValueComparator.valueCompare(a, b, "order")); 
    }
    this.data = this.data.sort((a, b) => ValueComparator.valueCompare(a, b, "order")); 
  }


  click(item: FunnelEntry) {
    let object = item.object;
    if(object != null) {
      let navEvent: NavigateEvent = {
        view: item.config.linkView,
        objectname: object.objectname,
        datatargets: [{
          filter: {uid: "'" + (item.config.linkAttribute != null ? object.get(item.config.linkAttribute) : object.uid) + "'"}
        }]
      }
      this.navigateService.navigateTo(navEvent);
    }
  }

  clickGroup(group: FunnelGroup) {
    group.open = !group.open;
  }

  clickPhaseGroup(group: FunnelPhaseGroup) {
    group.open = !group.open;
    if(this.id != null) {
      let funnelPref = this.pref;
      funnelPref.phasegroupstate[group.id] = group.open;
      this.userPref.setUISwitch("user", "funnel", this.id, funnelPref);
    }
  }

  dropped($event, phase) {
    let object = $event.data.object;
    let objectPhaseAttribute = $event.data.config.phaseAttribute;
    let phaseKeyAttribute = phase.config.keyAttribute;
    let phaseKey = phase.object.get(phaseKeyAttribute);
    object.setValue(objectPhaseAttribute, phaseKey)
  }
}



