import { Directive, ElementRef, Input, Pipe, PipeTransform, Renderer2, inject } from "@angular/core";
import { Observer } from "rxjs";
import { AppInjector } from "./app.module";
import { NavigateEvent, RbObject, Time } from "./datamodel";
import { FilterService } from "./services/filter.service";
import { UserprefService } from "./services/userpref.service";
import { RbDatasetComponent } from "./rb-dataset/rb-dataset.component";

export class Translator {
    cfg: any;

    constructor(c: any) {
        this.cfg = c;
    }

    get(val: any, selected: boolean) : any {
        if(this.cfg != null) {
            if(this.cfg[val] != null) {
                let item = this.cfg[val];
                if(typeof item == 'object') {
                    if(item['default'] != null && item['selected'] != null) {
                        return item[selected ? 'selected' : 'default'];
                    } else {
                        return item;
                    }
                } else {
                    return item;
                }         
            } else {
                return val;
            }
        } else {
            return val;
        }
    }
}

export class InitialsMaker {

    get(val: string): string {
        let ret : string = "";
        let pickNext: boolean = true;
        for(let c of val) {
            if(c == ' ') {
                pickNext = true;
            } else if(pickNext) {
                ret = ret + c.toUpperCase();
                pickNext = false;
            }            
        }
        return ret;
    }
}

export class Formatter {
    static months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dev"];
    static userPrefService: UserprefService;

    public static format(val: any, format: string) {
        if(format == 'duration') {
            return this.formatDuration(val);
        } else if(format == 'datetime') {
            return this.formatDateTime(val);
        } else if(format == 'date') {
            return this.formatDate(val);
        } else if(format == 'time') {
            return this.formatTime(val);
        } else if(format == 'currency') {
            return this.formatCurrency(val);
        } else if(format == 'integer') {
            return this.formatInteger(val);
        } else if(format.startsWith('decimal(')) {
            var decimals = parseInt(format.substring(8, format.length - 1));
            return this.formatDecimal(val, decimals);
        } else {
            return val;
        }
    }

    static formatDuration(value: any) {
        if(value != null && !isNaN(value)) {
            let ms = value;
            let years = Math.floor(ms / 31536000000);
            let weeks = Math.floor((ms % 31536000000) / 604800000);
            let days = Math.floor((ms % 604800000) / 86400000);
            let hours = Math.floor((ms % 86400000) / 3600000);
            let minutes = Math.floor((ms % 3600000) / 60000);
            let seconds = Math.floor((ms % 60000) / 1000);
            let milli = Math.floor((ms % 1000));
            let greaterThanMinute = ms > 60000;
            let greaterThanHour = ms > 3600000;
            let val = "";
            if(years != 0)
              val = val + " " + years + "y";
            if(weeks != 0)
              val = val + " " + weeks + "w";
            if(days != 0)
              val = val + " " + days + "d";
            if(hours != 0)
              val = val + " " + hours + "h";
            if(minutes != 0)
              val = val + " " + minutes + "m";
            if(seconds != 0 && !greaterThanHour)
              val = val + " " + seconds + "s";
            if(milli != 0 && !greaterThanMinute)
              val = val + " " + milli + "ms";
            if(ms == 0) 
              val = " 0";
            return val.substr(1);
        } else {
            return "";
        }
    }

    static formatDateTime(value: any) : string {
        let str = Formatter.formatDate(value);
        if(str != "") str = str + " ";
        str = str + Formatter.formatTime(value); 
        return str;
    }

    static formatDate(value: any) : string {
        let dt = null;
        if(value instanceof Date) dt = value;
        else if(typeof value == 'string' && !isNaN(Date.parse(value))) dt = new Date(value);
        if(dt != null && Formatter.userPrefService?.getGlobalPreferenceValue("timezone") == 'gmt') {
            dt = new Date(dt.getTime() + (dt.getTimezoneOffset() * 60 * 1000));
        }
        let str = "";
        if(dt != null) {
            const dateFormat = Formatter.userPrefService?.getGlobalPreferenceValue("dateformat") ?? "iso";
            if(dateFormat == 'isorev') {
                str =  dt.getDate().toString().padStart(2, "0") + "/" + (dt.getMonth() + 1).toString().padStart(2, "0") + "/" + dt.getFullYear().toString()
            } else if(dateFormat == 'american') {
                str =  (dt.getMonth() + 1).toString().padStart(2, "0") + "/" + dt.getDate().toString().padStart(2, "0") + "/" + dt.getFullYear().toString()
            } else {
                str = dt.getFullYear().toString() + "-" + (dt.getMonth() + 1).toString().padStart(2, "0") + "-" + dt.getDate().toString().padStart(2, "0");
            }
        }
        return str;
    }

    static formatTime(value: any) : string {
        let dt = null;
        if(value instanceof Date) dt = value;
        else if(typeof value == 'string' && !isNaN(Date.parse(value))) dt = new Date(value);
        else if(typeof value == 'string' &&  value.startsWith('T')) dt = (new Time(value)).atDate(new Date());
        if(dt != null && Formatter.userPrefService?.getGlobalPreferenceValue("timezone") == 'gmt') {
            dt = new Date(dt.getTime() + (dt.getTimezoneOffset() * 60 * 1000));
        }
        let str = "";
        if(dt != null) {
            const timeformat = Formatter.userPrefService?.getGlobalPreferenceValue("timeformat") ?? "iso";
            if(timeformat == 'ampm') {
                const hrs = dt.getHours();
                const hrs12 = hrs == 0 ? 12 : hrs > 12 ? hrs - 12 : hrs;
                str = hrs12.toString().padStart(2, "0") + ":" + dt.getMinutes().toString().padStart(2, "0") + (hrs > 12 ? "pm" : "am");
            } else {
                str = dt.getHours().toString().padStart(2, "0") + ":" + dt.getMinutes().toString().padStart(2, "0")
            }
        }
        return str;
    }

    static formatDateTimeCustom(dt: Date, format: string) : string {
        let str = "";
        if(dt != null) {
            str = format;
            str = str.replace('YYYY', dt.getFullYear().toString());
            str = str.replace('YY', (dt.getFullYear() % 100).toString());
            str = str.replace('MMM', Formatter.months[dt.getMonth()]);
            str = str.replace('MM', (dt.getMonth() + 1).toString().padStart(2, "0"));
            str = str.replace('DD', (dt.getDate()).toString().padStart(2, "0"));
            str = str.replace('D', (dt.getDate()).toString());
            str = str.replace('HH', (dt.getHours()).toString().padStart(2, "0"));
            str = str.replace('H', (dt.getHours()).toString());
            str = str.replace('mm', (dt.getMinutes()).toString().padStart(2, "0"));
        }
        return str;
    }  
    
    static formatCurrency(value: number) : string {
        const formatter = new Intl.NumberFormat('en-US', {style: 'currency',  currency: 'USD'});
        return formatter.format(value);  
    }

    static formatInteger(value: number) : string {
        if(value == null) return "";
        return Math.round(value).toString();
    }

    static formatDecimal(value: number, decimals: number) : string {
        if(value == null) return "";
        return value.toFixed(decimals);
    }

}

@Pipe({name: 'rbDate'})
export class RbDatePipe implements PipeTransform {
  transform(value: Date): string {
    return Formatter.formatDate(value);
  }
}

@Pipe({name: 'rbTime'})
export class RbTimePipe implements PipeTransform {
  transform(value: Date): string {
    return Formatter.formatTime(value);
  }
}

export class Converter {
    public static convert(val: any, format: string) {
        if(format == "mstohour" && !isNaN(val)) {
            return val / 3600000;
        } else {
            return val;
        }
    }
}

export class ValueComparator{ 
    public static notEqual(a: any, b: any): boolean {
        return !this.equal(a, b);
    }

    public static equal(a: any, b: any): boolean {
        if(a == null && b == null) {
            return true;
        } else if(a == null || b == null) {
            return false;
        } else if(typeof a == 'object' && typeof b == 'object') {
            return JSON.stringify(a) == JSON.stringify(b);
        } else if(typeof a == 'object' || typeof b == 'object') {
            return false;
        } else {
            return a == b;
        }
    }

    public static valueCompare(a: any, b: any, key: string, dir: number = 1): number {
        let valA = a[key];
        let valB = b[key];
        if(valA == null) {
          return -1 * dir;
        } else if(valB == null) {
          return 1 * dir;
        } else if(valA > valB) {
          return 1 * dir;
        } else {
          return -1 * dir;
        }
      }
}

export class ObserverProxy implements Observer<any> {
    constructor(
        public observer: Observer<any>,
        public callable?: (value) => void,
        public errorCallable?: (error) => void
    ) {
        
    }
    closed?: boolean;
    next(value) {
        if(this.callable != null) this.callable(value);
        this.observer.next(value);
        this.observer.complete();
    }
    
    error(error) {
        if(this.errorCallable != null) this.errorCallable(error);
        this.observer.error(error);
    }

    complete() {

    }
}

export class Evaluator {
    public static eval(expr: any, object: RbObject, relatedObject: RbObject, dataset: RbDatasetComponent) {
        if(expr == null) {
            return null;
        } else if(expr == 'true' || expr == true) {
            return true;
        } else if(expr == 'false' || expr == false) {
            return false;
        } else if(typeof expr == 'string' && !((expr.indexOf("object.") > -1 && object == null) || (expr.indexOf("relatedObject.") > -1 && relatedObject == null))) {
            try {
                return eval(expr);            
            } catch(err) {
                return null;
            }
        } else {
            return null;
        }
    }
}

export class Hasher {
    public static hash(value: any) {
        let hash = JSON.stringify(value).split("").reduce(function(a,b){a=((a<<5)-a)+b.charCodeAt(0);return a&a},0);   
        return hash;
    }
}

export class HtmlParser {
    private static selfEndedTags = ["br"];

    public static parse(str) {
        let ret = this.recparse(str ?? "", 0);
        return ret.nodes;
    }
    
    private static recparse(str, pos) {
        let nodes = [];
        while(pos > -1 && pos < str.length) {
            let lastpos = pos;
            pos = str.indexOf("<", pos);
            let text = str.substring(lastpos, (pos > -1 ? pos : str.length));
            if(text.length > 0) {
                text = text.replaceAll('\r', '').replaceAll('\n', '').replaceAll('\t', '');
                nodes.push({type:"text", text: text});
            }
            if(pos > -1) {
                lastpos = pos;
                pos = str.indexOf(">", pos);
                if(pos > -1) {
                    let substr = str.substring(lastpos + 1, pos);
                    pos++;
                    if(substr.startsWith("/")) {
                        return {nodes, pos};
                    } else {
                        let selfend = false;
                        if(substr.endsWith("/")) {
                            substr = substr.substring(0, substr.length - 1);
                            selfend = true;
                        }
                        let firstSpacePos = substr.indexOf(" ");
                        let tag = firstSpacePos > -1 ? substr.substring(0, firstSpacePos) : substr;
                        let attrs = firstSpacePos > -1 ? substr.substring(firstSpacePos + 1) : null;
                        let node = {type: "tag", tag, attrs, selfend};
                        if(selfend) {
                            //seld ended
                        } else if(this.selfEndedTags.indexOf(tag) > -1) {
                            //implicit self-ended tag
                        } else {
                            let result = this.recparse(str, pos);
                            node['children'] = result.nodes;
                            pos = result.pos;
                        }
                        nodes.push(node);
                    }
                }
            }
        }
        return {nodes, pos};
    }

    public static stringify(nodes, indent = false, baseindent = 0, wraphtml = false) {
        var str = "";
        var basepad = "".padStart(baseindent, "\t");
        for(var node of nodes) {
            if(node.type == 'tag') {
                str = str + basepad + "<" + node.tag;
                str = str + (node.attrs != null ? " " + node.attrs : "");
                str = str + (node.selfend ? "/" : "");
                str = str + ">";
                if(indent) {
                    str = str + "\r\n";
                }
                if( node.children != null && node.children.length > 0) {
                    str = str + this.stringify(node.children, indent, (indent ? baseindent + 1 : 0));
                }
                if(node.selfend == false && this.selfEndedTags.indexOf(node.tag) == -1) {
                    str = str + basepad + "</" + node.tag + ">"; 
                    if(indent) {
                        str = str + "\r\n";
                    }  
                }         
            } else if(node.type == 'text') {
                str = str + basepad + node.text;
                if(indent) {
                    str = str + "\r\n";
                }  
            }
             
        }
        if(wraphtml && !(nodes.length == 1 && nodes[0].type == 'tag' && nodes[0].tag == 'html')) {
            str = "<html>" + str + "</html>";
        }
        return str;
    }
}

export class FileReferenceResolver {
    public static resolve(htmlStr: string, fileService: string) : string {
        let ret = htmlStr;
        if(ret != null) {
            let pos = -1;
            let marker = "src=\"fileuid:";
            let endMarker = "\"";
            while((pos = ret.indexOf(marker)) > -1) {
                let posend = ret.indexOf(endMarker, pos + marker.length);
                let fileuid = ret.substring(pos + marker.length, posend);
                ret = ret.substring(0, pos) + "src=\"/" + fileService + "?fileuid=" + fileuid + "\"" + ret.substring(posend + 1);
            }    
        }
        return ret;
    }
}

@Directive({
    selector: 'iframe'
  })
  export class CachedSrcDirective {
  
      @Input() 
      public get cachedsrc(): string {
          return this.elref.nativeElement.src;
      }
      public set cachedsrc(src: string) {
          if (this.elref.nativeElement.src !== src) {
              this.renderer.setAttribute(this.elref.nativeElement, 'src', src);
          }
      }
  
      constructor(
          private elref: ElementRef,
          private renderer : Renderer2
          ) { }
  }

export class LinkConfig {
    target: string;
    view: string;
    objectname: string;
    tab: string;
    reset: boolean;
    datatargets: LinkConfigDataTarget[];
    
    constructor(json: any) {
        this.target = json.target;
        this.view = json.view;
        this.objectname = json.objectname;
        this.tab = json.tab;
        this.reset = json.reset ?? false;
        this.datatargets = [];
        if(json.attribute != null || json.objectname != null || json.filter != null || json.filtersingleobject != null || json.select != null) { //This is a deprecated notation
            this.datatargets.push({
                attribute: json.attribute,
                objectname: json.objectname,
                datasetid: json.datasetid,
                filter: json.filter,
                filtersingleobject: json.filtersingleobject ?? true,
                sort: json.sort,
                select: json.select
            });
        }  
        if(json.datatargets != null) {
            for(var item of json.datatargets) {
                this.datatargets.push({
                    attribute: item.attribute,
                    objectname: item.objectname,
                    datasetid: item.datasetid,
                    filter: item.filter,
                    filtersingleobject: item.filtersingleobject ?? true,
                    sort: item.sort,
                    select: item.select
                });
            }  
        }    
    }

    getNavigationEvent(object: RbObject, dataset: RbDatasetComponent): NavigateEvent {
        let event: NavigateEvent = {
            target: this.target,
            view: this.view,
            tab: this.tab,
            reset: this.reset,
            datatargets: []
        };
        event.objectname = this.view == null && this.objectname != null ? this.objectname : null;
        for(var datatarget of this.datatargets) {
            let objectuid = object != null ? (datatarget.attribute != null ? object.get(datatarget.attribute) : object.uid) : null;
            let filter = null;
            let sort = null;
            let select = null;
            if(datatarget.filter != null) {
                let filterService: FilterService = AppInjector.get(FilterService);
                let rfilter = filterService.resolveFilter(datatarget.filter, object, dataset, null, null, null);
                filter = filterService.unresolveFilter(rfilter);  //Unresolving this as the DataSet will resolve it
                sort = datatarget.sort;
            } 
            if(datatarget.select != null) {
                let filterService: FilterService = AppInjector.get(FilterService);
                select = filterService.resolveFilter(datatarget.select, object, dataset, null, null, null);
            } 
            if(datatarget.filter == null && datatarget.select == null) {
                if(datatarget.filtersingleobject == false) {
                    select = {uid: objectuid};
                } else if(datatarget.filtersingleobject == true) {
                    filter = {uid: "'" + objectuid + "'"};
                } 
            }
            event.datatargets.push({objectname: datatarget.objectname, datasetid: datatarget.datasetid, filter: filter, sort: sort, select: select}); 
        }
        return event;
    }
}

export class LinkConfigDataTarget{
    attribute: string;
    objectname: string;
    datasetid: string;
    filter: any;
    filtersingleobject: boolean;
    sort: any;
    select: any;
}

export class ColorConfig {
    attribute: string;
    expression: string;
    map: any;

    constructor(json: any) {
        this.attribute = json.attribute;
        this.expression = json.expression;
        this.map = json.map;
    }

    getColor(object: RbObject): string {
        let val: string = null;
        if(this.attribute != null) {
            val = object.get(this.attribute);
        } else if(this.expression != null) {
            val = Evaluator.eval(this.expression, object, null, null);
        }
        if(this.map != null) {
            val = this.map[val];
        }
        return val;
    }
}

export class ColorTool {

    public static decode(str: string): number[] {
        const cleanedHex = str.startsWith("#") ? str.slice(1) : str;
        const bigint = parseInt(cleanedHex, 16);
        const r = (bigint >> 16) & 255;
        const g = (bigint >> 8) & 255;
        const b = bigint & 255;
        return [r, g, b];
    }

    public static encode(values: number[]): string {
        if(values.length == 3) {
            const bigint = (values[0] << 16) + (values[1] << 8) + values[2];
            const str = "#" + bigint.toString(16).padStart(6, "0");
            return str;
        } else {
            return null;
        }
    }

    public static lightenTo(color: string, intensity: number, spread: number): string {
        let values = ColorTool.decode(color);
        let min = Math.min(...values);
        let max = Math.max(...values);
        let initialSpread = max - min;
        let scale = initialSpread > 0 ? spread / initialSpread : 0;
        let targetMin = intensity - spread;
        let newValues = values.map(v => Math.round(targetMin + ((v-min) * scale)));
        let newColor = ColorTool.encode(newValues);
        return newColor;
    }

    
}
