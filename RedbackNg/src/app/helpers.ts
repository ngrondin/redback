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