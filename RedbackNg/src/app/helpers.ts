import { Pipe, PipeTransform } from "@angular/core";
import { Observer } from "rxjs";
import { RbObject } from "./datamodel";

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
    public static format(val: any, format: string) {
        if(format == 'duration') {
            return this.formatDuration(val);
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

    static formatDateTime(value: Date) : string {
        let str = Formatter.formatDate(value);
        if(str != "") str = str + " ";
        str = str + Formatter.formatTime(value); 
        return str;
    }

    static formatDate(value: Date) : string {
        let str = "";
        if(value != null) {
            str = str + value.getFullYear().toString();
            str = str + "-";
            str = str + (value.getMonth() + 1).toString().padStart(2, "0");
            str = str + "-";
            str = str + value.getDate().toString().padStart(2, "0");
        }
        return str;
    }

    static formatTime(value: Date) : string {
        let str = "";
        if(value != null) {
            str = str + value.getHours().toString().padStart(2, "0");
            str = str + ":";
            str = str + value.getMinutes().toString().padStart(2, "0");
        }
        return str;
    }

  static formatDateTimeCustom(dt: Date, format: string) : string {
        let str = "";
        if(dt != null) {
            str = format;
            str = str.replace('YYYY', dt.getFullYear().toString());
            str = str.replace('YY', (dt.getFullYear() % 100).toString());
            str = str.replace('MM', (dt.getMonth() + 1).toString().padStart(2, "0"));
            str = str.replace('DD', (dt.getDate()).toString().padStart(2, "0"));
            str = str.replace('HH', (dt.getHours()).toString().padStart(2, "0"));
            str = str.replace('mm', (dt.getMinutes()).toString().padStart(2, "0"));
        }
        return str;
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
    public static eval(expr: string, object: RbObject, relatedObject: RbObject) {
        if(!((expr.indexOf("object.") > -1 && object == null) || (expr.indexOf("relatedObject.") > -1 && relatedObject == null))) {
            return eval(expr);            
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

    public static stringify(nodes, indent = false, baseindent = 0) {
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
        return str;
    }
}