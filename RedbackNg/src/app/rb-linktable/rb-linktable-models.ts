import { LinkConfig, ColorConfig, Evaluator, VAEConfig } from "app/helpers";

export class LinkTableColumnConfig {
    id: string;
    label: string;
    attribute: string | null;
    expression: Function | null;
    displayAttribute: string;
    checkbox: boolean;
    checkboxevent?: Function;
    format: string;
    align: string;
    size: number;
    width: number;
    wrap: boolean;
    showExpr: Function;
    link: LinkConfig | null;
    modal: string;
    sum: boolean;
    sumlink: LinkConfig | null;
    iconmap: any;
    backColor: ColorConfig | null;
    foreColor: ColorConfig | null;
    bold: VAEConfig | null;
    alt?: {[key: string]: LinkTableColumnConfig};

    constructor(json: any, userpref: any) {
      this.id = json.id;
      this.label = json.label;
      this.attribute = json.attribute;
      this.expression = userpref != null && userpref.exression != null ? Evaluator.createFunction(userpref.expression) : json.expression != null ? Evaluator.createFunction(json.expression) : null;
      this.displayAttribute = json.displayattribute;
      this.checkbox = json.checkbox != null ? json.checkbox : false;
      this.checkboxevent = json.checkboxevent != null ? Function("object", json.checkboxevent) : null;
      this.format = json.format;
      this.align = json.align;
      this.size = json.size;
      this.width = json.size != null ? json.size : 11.3;
      this.wrap = json.wrap != null ? json.wrap : false;
      this.showExpr = Evaluator.createFunction(json.show != null ? json.show : "true");
      this.link = json.link != null ? new LinkConfig(json.link) : null;
      this.modal = json.modal;
      this.sum = json.sum;
      this.sumlink = json.sumlink != null ? new LinkConfig(json.sumlink) : null;
      this.iconmap = json.iconmap;
      let bc = userpref?.backcolor || json.backcolor;
      this.backColor = (bc != null ? new ColorConfig(bc) : null);
      let fc = userpref?.forecolor || json.forecolor || json.foreColor;
      this.foreColor = (fc != null ? new ColorConfig(fc) : null);
      let bld = userpref?.bold || json.bold;
      this.bold = (bld != null ? new VAEConfig(bld) : null);
      if(json.alt != null) {
        this.alt = {};
        for(const key in json.alt) {
            let newJson = {...json};
            delete newJson.alt;
            newJson = {...newJson, ...json.alt[key]};
            this.alt[key] = new LinkTableColumnConfig(newJson, userpref);
        }
      }
    }

    get widthStr() : string {
      return 'min(' + (this.width * 0.88) + 'vw, ' + (this.width * 16.896) + 'px)';
    }

    get isClickable() : boolean {
      return this.link != null || this.modal != null;
    }

    get isSumClickable() : boolean {
      return this.sumlink != null;
    }
  }

  export class LinkTableGroupConfig {
    attribute: string | null;
    expression: Function | null;
    link: LinkConfig | null;
    modal: string;

    constructor(json: any) {
      this.attribute = json.attribute;
      this.expression = json.expression != null ? Evaluator.createFunction(json.expression) : null;
      this.link = json.link != null ? new LinkConfig(json.link) : null;
      this.modal = json.modal;
    }

  }
