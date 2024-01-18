import { SeriesConfig } from "app/abstract/rb-datacalc";
import { RbObject } from "app/datamodel";

export class FunnelSeriesConfig extends SeriesConfig {
    dataset: string;
    labelAttribute: string;
    subLabelAttribute: string;
    colorAttribute: string;
    colorMap: any;
    groupAttribute: string;
    phaseAttribute: string;
    linkView: string;
    linkAttribute: string;
    modal: string;
    canEdit: boolean;
  
    constructor(json: any, userpref: any) {
      super(json);
      let subpref = userpref != null && userpref.series != null ? userpref.series[json.dataset] : null;
      this.labelAttribute = subpref != null && subpref.labelattribute != null ? subpref.labelattribute : json.labelattribute;
      this.subLabelAttribute = subpref != null && subpref.sublabelattribute != null ? subpref.sublabelattribute : json.sublabelattribute;
      this.colorAttribute = json.colorattribute;
      this.colorMap = json.colormap;
      this.groupAttribute = json.groupattribute;
      this.phaseAttribute = json.phaseattribute;
      this.linkView = json.linkview;
      this.modal = json.modal;
    }
}

export class FunnelGroupConfig {
    key: string;
    label: string;
    order: number;
    open: boolean;

    constructor(json: any) {
     this.key = json.key;
     this.label = json.label;
     this.order = json.order;
     this.open = json.open;
    }
}

export class FunnelPhaseConfig extends SeriesConfig {
    dataset: string;
    keyAttribute: string;
    labelAttribute: string;
    orderAttribute: string;
    groupAttribute: string;
  
    constructor(json: any, userpref: any) {
      super(json);
      let subpref = userpref != null && userpref.series != null ? userpref.series[json.dataset] : null;
      this.keyAttribute = json.keyattribute;
      this.labelAttribute = json.labelattribute;
      this.orderAttribute = json.orderattribute;
      this.groupAttribute = json.groupattribute;
    }
}

export class FunnelPhaseGroupConfig extends SeriesConfig {
  dataset: string;
  keyAttribute: string;
  labelAttribute: string;
  orderAttribute: string;

  constructor(json: any, userpref: any) {
    super(json);
    let subpref = userpref != null && userpref.series != null ? userpref.series[json.dataset] : null;
    this.keyAttribute = json.keyattribute;
    this.labelAttribute = json.labelattribute;
    this.orderAttribute = json.orderattribute;
  }
}

export class FunnelPhaseGroup {
  id: string;
  label: string;
  order: number;
  color: string;
  open: boolean;
  object: RbObject;
  config: FunnelPhaseGroupConfig;
  phases: FunnelPhase[] = [];

  constructor(i: string, l: string, o: number, c: string, op: boolean, obj: RbObject, cfg: FunnelPhaseGroupConfig) {
    this.id = i;
    this.label = l;
    this.order = o;
    this.color = c;
    this.open = op;
    this.object = obj;
    this.config = cfg;
  }
}

export class FunnelPhase {
    id: string;
    label: string;
    order: number;
    object: RbObject;
    config: FunnelPhaseConfig;
    groups: FunnelGroup[] = [];
  
    constructor(i: string, l: string, o: number, obj: RbObject, cfg: FunnelPhaseConfig) {
      this.id = i;
      this.label = l;
      this.order = o;
      this.object = obj;
      this.config = cfg;
    }
}

export class FunnelGroup {
    id: string;
    label: string;
    open: boolean;
    entries: FunnelEntry[] = [];
  
    constructor(i: string, l: string, o: boolean) {
      this.id = i;
      this.label = l;
      this.open = o;
    }
}
  
export class FunnelEntry {
    id: string;
    label: string;
    sublabel: string;
    color: string;
    object: RbObject;
    config: FunnelSeriesConfig;
  
    constructor(i: string, l: string, sl: string, c: string, o: RbObject, cfg: FunnelSeriesConfig) {
      this.id = i;
      this.label = l;
      this.sublabel = sl;
      this.color = c;
      this.object = o;
      this.config = cfg;
    }
}