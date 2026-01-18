import { LinkConfig, ColorConfig } from "app/helpers";

export class LinkTableColumnConfig {
    id: string;
    label: string;
    attribute: string;
    expression: string;
    displayAttribute: string;
    format: string;
    align: string;
    size: number;
    width: number;
    showExpr: string;
    link: LinkConfig;
    modal: string;
    sum: boolean;
    sumlink: LinkConfig;
    iconmap: any;
    backColor: ColorConfig;
    foreColor: ColorConfig;
    alt: {[key: string]: LinkTableColumnConfig};
  
    constructor(json: any, userpref: any) {
      this.id = json.id;
      this.label = json.label;
      this.attribute = json.attribute;
      this.expression = userpref != null && userpref.exression != null ? userpref.expression : json.expression;
      this.displayAttribute = json.displayattribute;
      this.format = json.format;
      this.align = json.align;
      this.size = json.size;
      this.width = json.size != null ? json.size : 11.3;
      this.showExpr = (json.show != null ? json.show : "true");
      this.link = json.link != null ? new LinkConfig(json.link) : null;
      this.modal = json.modal;
      this.sum = json.sum;
      this.sumlink = json.sumlink != null ? new LinkConfig(json.sumlink) : null;
      this.iconmap = json.iconmap;
      this.backColor = userpref != null && userpref.backcolor != null ? new ColorConfig(userpref.backcolor) : (json.backcolor != null ? new ColorConfig(json.backcolor) : null);
      this.foreColor = userpref != null && userpref.foreColor != null ? new ColorConfig(userpref.foreColor) : (json.foreColor != null ? new ColorConfig(json.foreColor) : null);
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
    attribute: string;
    expression: string;
  
    constructor(json: any) {
      this.attribute = json.attribute;
      this.expression = json.expression;
    }
  
  }  