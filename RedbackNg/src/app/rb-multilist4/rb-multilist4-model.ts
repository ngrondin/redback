import { SeriesConfig } from "app/abstract/rb-datacalc";
import { RbObject, RELATED_LOADING } from "app/datamodel";
import { ColorConfig, ColorTool, Evaluator, LinkConfig, VAEConfig } from "app/helpers";
import { RbDatasetComponent } from "app/rb-dataset/rb-dataset.component";
import { ListFieldConfig } from "app/rb-list4/rb-list4.component";


export class MultiList4SeriesConfig extends SeriesConfig {
  main!: ListFieldConfig;
  sub!: ListFieldConfig;
  meta1!: ListFieldConfig;
  meta2!: ListFieldConfig;
  backColor!: ColorConfig;
  imageattribute?: string;
  modal?: string;
  link?: string;
  clickscript?: Function;
  allowdrag!: boolean;

  constructor(json: any) {
    super(json);
    this.main = new ListFieldConfig(json.mainattribute, json.mainexpression, json.maincolor, json.mainformat);
    this.sub = new ListFieldConfig(json.subattribute, json.subexpression, json.subcolor, json.subformat);
    this.meta1 = new ListFieldConfig(json.meta1attribute, json.meta1expression, json.meta1color, json.meta1format);
    this.meta2 = new ListFieldConfig(json.meta2attribute, json.meta2expression, json.meta2color, json.meta2format);
    this.backColor = new ColorConfig({expression: json.color, attribute: json.colorattribute, map: json.colormap});
    this.imageattribute = json.imageattribute;
    this.modal = json.modal;
    this.link = json.navigate;
    this.clickscript = json.clickscript != null ? Function("event", json.clickscript) : undefined;
    this.allowdrag = json.allowdrag || false;
  }
}
