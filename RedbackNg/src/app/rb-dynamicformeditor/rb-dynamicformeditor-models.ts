import { SeriesConfig } from "app/abstract/rb-datacalc";
import { RbObject } from "app/datamodel";

export class DynamicFormEditorItemSeriesConfig extends SeriesConfig {
    dataset: string;
    typeattribute : string;
    codeattribute : string;
    orderattribute : string;
    labelattribute : string;
    detailattribute : string;
    categorylinkattribute : string;
    dependencyattribute : string;
    dependencyoperatorattribute : string;
    dependencyvalueattribute : string;
    modal : string;
  
    constructor(json: any) {
      super(json);
      this.typeattribute = json.typeattribute;
      this.codeattribute = json.codeattribute;
      this.orderattribute = json.orderattribute;
      this.labelattribute = json.labelattribute;
      this.detailattribute = json.detailattribute;
      this.categorylinkattribute = json.categorylinkattribute;
      this.dependencyattribute = json.dependencyattribute;
      this.dependencyoperatorattribute = json.dependencyoperatorattribute;
      this.dependencyvalueattribute = json.dependencyvalueattribute;
      this.modal = json.modal;
    }
}

export class DynamicFormEditorCategorySeriesConfig extends SeriesConfig {
    orderattribute : string;
    labelattribute : string;
    modal : string;
  
    constructor(json: any) {
      super(json);
      this.orderattribute = json.orderattribute;
      this.labelattribute = json.labelattribute;
      this.modal = json.modal;
    }
}

export class DynamicFormEditorItem {
    is: string = 'item';
    
    constructor(
        public object: RbObject,
        public label: string,
        public icon: string
    ) {}
}

export class DynamicFormEditorCategory {
    is: string = 'cat';
    items: DynamicFormEditorItem[] = [];
    
    constructor(
        public object: RbObject,
        public label: string
    ) {}
}