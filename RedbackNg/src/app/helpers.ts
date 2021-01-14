
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
}