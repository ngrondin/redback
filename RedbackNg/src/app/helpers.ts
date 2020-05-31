
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