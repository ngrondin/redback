import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { FunnelEntry, FunnelGroup, FunnelGroupConfig, FunnelPhase, FunnelPhaseConfig, FunnelPhaseGroup, FunnelPhaseGroupConfig, FunnelSeriesConfig } from './rb-funnel-models';
import { UserprefService } from 'app/services/userpref.service';
import { ValueComparator } from 'app/helpers';
import { NavigateService } from 'app/services/navigate.service';
import { NavigateEvent } from 'app/datamodel';
import { ModalService } from 'app/services/modal.service';

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
  openState: any = {};

  data: FunnelPhaseGroup[] = [];

  colorScheme = ['#EA6A47', '#B54051', '#40B569', '#ADABA7'];

  constructor(
    private userPref: UserprefService,
    private navigateService: NavigateService,
    private modalService: ModalService
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
            var groupKey = cfg.group != null ? cfg.group.getValue(entryObject) : null;
            var groupCfg = this.groupConfigs[groupKey];
            if (groupCfg != null) {
              var phaseGroupId = phase.id + "_" + groupKey;
              let isOpen = this.openState[phaseGroupId] ?? groupCfg.open;
              if(groups[groupKey] == null) groups[groupKey] = new FunnelGroup(phaseGroupId, groupCfg.label, isOpen, groupCfg.order);
              var color = color = "#888";
              if (cfg.colorAttribute != null) {
                let val = entryObject.get(cfg.colorAttribute);
                color = cfg.colorMap != null ? cfg.colorMap[val] : val;
              }
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
        phase.groups = phase.groups.sort((a, b) => a.order - b.order);
        phaseGroup.phases.push(phase);

      }
      phaseGroup.phases = phaseGroup.phases.sort((a, b) => ValueComparator.valueCompare(a, b, "order"));
    }
    this.data = this.data.sort((a, b) => ValueComparator.valueCompare(a, b, "order"));
  }


  click(item: FunnelEntry) {
    if (item.object != null) {
      if (item.config.linkView != null) {
        let navEvent: NavigateEvent = {
          view: item.config.linkView,
          objectname: item.object.objectname,
          datatargets: [{
            filter: {uid: "'" + (item.config.linkAttribute != null ? item.object.get(item.config.linkAttribute) : item.object.uid) + "'"}
          }]
        }
        this.navigateService.navigateTo(navEvent);
      } else if (item.config.modal != null) {
        this.getDatasetForConfig(item.config)?.select(item.object);
        this.modalService.open(item.config.modal);
      }
    }
  }

  clickGroup(group: FunnelGroup) {
    group.open = !group.open;
    this.openState[group.id] = group.open;
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
