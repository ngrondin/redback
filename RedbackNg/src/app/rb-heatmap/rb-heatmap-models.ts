import { SeriesConfig } from "app/abstract/rb-datacalc";
import { RbObject } from "app/datamodel";
import { ColorConfig, ColorTool, VAEConfig } from "app/helpers";

/**
 * One axis of the heat map: the rows or the columns. Each object of the dataset is one row (or column).
 * keyattribute is the value a series object points to with its rowattribute (or colattribute), uid by default.
 * The label is a value, an attribute or an expression, like the series label.
 */
export class HeatmapAxisConfig extends SeriesConfig {
    keyAttribute: string;
    label: VAEConfig;

    constructor(json: any) {
        super(json);
        this.keyAttribute = json.keyattribute ?? "uid";
        this.label = HeatmapAxisConfig.vae(json.label, json.labelattribute, json.labelexpression) ?? new VAEConfig({attribute: this.keyAttribute});
    }

    static vae(obj: any, attribute: string, expression: string): VAEConfig | null {
        if(obj != null) return new VAEConfig(typeof obj === 'object' ? obj : {value: obj});
        if(attribute != null || expression != null) return new VAEConfig({attribute: attribute, expression: expression});
        return null;
    }
}

/**
 * A series of the heat map: each object is drawn in the box of the row its rowattribute points to
 * and the column its colattribute points to.
 */
export class HeatmapSeriesConfig extends SeriesConfig {
    rowAttribute: string;
    colAttribute: string;
    label: VAEConfig | null;
    color: ColorConfig | null;
    tip: VAEConfig | null;
    click: Function | null;

    constructor(json: any) {
        super(json);
        this.rowAttribute = json.rowattribute;
        this.colAttribute = json.colattribute;
        this.label = HeatmapAxisConfig.vae(json.label, json.labelattribute, json.labelexpression);
        if(json.color != null && typeof json.color === 'object') {
            this.color = new ColorConfig(json.color);
        } else if(json.color != null || json.colorattribute != null || json.colorexpression != null) {
            this.color = new ColorConfig({value: json.color, attribute: json.colorattribute, map: json.colormap, expression: json.colorexpression});
        } else {
            this.color = null;
        }
        this.tip = HeatmapAxisConfig.vae(json.tip, json.tipattribute, json.tipexpression);
        this.click = json.clickscript != null ? Function("event", json.clickscript) : null;
    }
}

export class HeatmapHeader {
    key: any;
    label: string;
    object: RbObject;

    constructor(key: any, label: string, object: RbObject) {
        this.key = key;
        this.label = label;
        this.object = object;
    }
}

export class HeatmapEntry {
    label: string;
    color: string;
    textColor: string;
    tip: string;
    object: RbObject;
    config: HeatmapSeriesConfig;

    constructor(label: string, color: string, tip: string, object: RbObject, config: HeatmapSeriesConfig) {
        this.label = label;
        this.color = color;
        let rgb = ColorTool.decode(color);
        this.textColor = rgb != null && (0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]) < 145 ? "#ffffff" : "#222222";
        this.tip = tip;
        this.object = object;
        this.config = config;
    }
}
