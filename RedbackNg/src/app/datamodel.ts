import { DataService } from './data.service';
import { analyzeAndValidateNgModules } from '@angular/compiler';

export class ObjectResp {
    objects: object;
}


export class RbObject {
    objectname: string;
    uid: string;
    domain: string;
    data: any = {};
    related: any = {};
    validation: any = {};
    changed: any;
    dataService: DataService;
    lastUpdated: number;

    constructor(json: any, ds: DataService) {
        this.dataService = ds;
        this.uid = json.uid;
        this.objectname = json.objectname;
        this.domain = json.domain;
        this.updateFromServer(json);
    }

    updateFromServer(json: any) {
        const inData: any = json.data;
        for(const attribute in json.data) {
            if(this.data[attribute] != json.data[attribute]) {
                this.data[attribute] = json.data[attribute];
            }

            if(json.related != null) {
                if(json.related[attribute] != null) {
                    let related = this.dataService.getLocalObject(json.related[attribute].objectname, json.related[attribute].uid);
                    if(this.related[attribute] != related) {
                        this.related[attribute] = related;
                    }
                } else {
                    this.related[attribute] = null;
                }
            }

            if(json.validation != null && json.validation[attribute] != null) {
                this.validation[attribute] = json.validation[attribute];
            }
        
        }

        this.changed = [];
        this.lastUpdated = (new Date()).getTime();
    }

    get(attr: string) : any {
        if(attr != null) {
            if(attr == 'uid') {
                return this.uid;
            } else if(attr.indexOf('.') == -1) {
                return this.data[attr];
            } else {
                let relationship = attr.substring(0, attr.indexOf('.'));
                let finalattr = attr.substring(attr.indexOf('.') + 1);
                if(this.related[relationship] != null) {
                    return this.related[relationship].data[finalattr];
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }
    
    canEdit(attribute: string) : boolean {
        return (this.validation != null && this.validation[attribute] != null && this.validation[attribute].editable == true);
    }

    setValue(attribute: string, value: any) {
        let ret = this._setValueAndRelated(attribute, value, null);
        if(ret == true && this.dataService.saveImmediatly) {
            this.dataService.updateObjectToServer(this);
        }
    }

    setValues(map: any) {
        let ret: boolean = false;
        for(let key of Object.keys(map)) {
            ret = this._setValueAndRelated(key, map[key], null) || ret;
        }
        if(ret == true && this.dataService.saveImmediatly) {
            this.dataService.updateObjectToServer(this);
        }
    }

    setValueAndRelated(attribute: string, value: any, related: RbObject) {
        let ret = this._setValueAndRelated(attribute, value, related);
        if(ret == true && this.dataService.saveImmediatly) {
            this.dataService.updateObjectToServer(this);
        }
    }

    _setValueAndRelated(attribute: string, value: any, related: RbObject) : boolean {
        if(attribute == 'uid') {
            if(this.uid == null) {
                this.uid = value;
                this.dataService.createObject(this.objectname, this.uid, this.data).subscribe(val => {});
            }
        } else if(this.validation[attribute].editable == true) {
            this.data[attribute] = value;
            this.related[attribute] = related;
            this.changed.push(attribute);
            this.lastUpdated = (new Date()).getTime();
            return true;
        }
        return false;
    }

    simplify() : string {
        let str: string = "";
        let o: any = {
            "objectname": this.objectname,
            "uid": this.uid,
            "data": this.data,
            "related":{}
        };
        for(let a of Object.keys(this.related)) {
            o.related[a] = this.related[a].simplify();
        }
        return o;
    }
}




export class RbFile {
    fileUid: string;
    relatedObject: string;
    relatedUid: string;
    fileName: string;
    mime: string;
    thumbnail: string;
    username: string;
    date: Date;

    constructor(json: any, ds: DataService) {
        this.fileUid = json.fileuid;
        this.relatedObject = json.relatedobject;
        this.relatedUid = json.relateduid;
        this.fileName = json.filename;
        this.mime = json.mime;
        this.thumbnail = json.thumbnail;
        this.username = json.username;
        this.date = new Date(json.date);
    }
}



export class RbAggregate {
    objectname: string;
    dimensions: any;
    metrics: any;
    related: any;
    dataService: DataService;

    constructor(json: any, ds: DataService) {
        this.objectname = json.objectname;
        this.dimensions = json.dimensions;
        this.metrics = json.metrics;
        this.related = json.related != null ? json.related : {};
        this.dataService = ds;
        if(json.related != null) {
            for(const attribute in json.related) {
                this.related[attribute] = this.dataService.updateObjectFromServer(json.related[attribute]);
            }
        }
    }

    getDimension(attr: string) : any {
        if(attr != null) {
            if(attr.indexOf('.') == -1) {
                return this.dimensions[attr];
            } else {
                let relationship = attr.substring(0, attr.indexOf('.'));
                let finalattr = attr.substring(attr.indexOf('.') + 1);
                if(this.related[relationship] != null) {
                    return this.related[relationship].data[finalattr];
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    getMetric(metric: string) : any {
        if(metric != null) {
            return this.metrics[metric];
        } else {
            return null;
        }        
    }

}




export class XY {
    x: number;
    y: number;

    constructor(a: number, b: number) {
        this.x = a;
        this.y = b;
    }
}

export class Time {
    hours: number = 0;
    minutes: number = 0;
    seconds: number = 0;
    nano: number = 0;
    zoneId: string = 'UTC';

    constructor(iso?: string) {
        if(iso != null) {
            let str: string = iso;
            let timeStr: string = null;
            this.zoneId = Intl.DateTimeFormat().resolvedOptions().timeZone;
            if(str.startsWith("T")) str = str.substring(1);
            let pos1: number = str.indexOf("[");
            let pos2: number = str.indexOf("]");
            let pos3: number = str.indexOf("+");
            if(pos3 == -1) pos3 = str.indexOf("-");
            if(pos3 == -1) pos3 = str.indexOf("Z");
            
            if(pos1 == -1 && pos2 == -1 && pos3 == -1) {
                timeStr = str;
            } else if(pos1 > -1 && pos2 > -1 && pos3 == -1) {
                timeStr = str.substring(0, pos1).trim();
                this.zoneId = str.substring(pos1 + 1, pos2);
            } else if(pos1 == -1 && pos2 == -1 && pos3 > -1) {
                timeStr = str.substring(0, pos3).trim();
                this.zoneId = str.substring(pos3);
            } 
            if(timeStr != null) {
                let timeParts: string[] = timeStr.split(":");
                if(timeParts.length > 0)
                    this.hours = Number.parseInt(timeParts[0]); 
                if(timeParts.length > 1)
                    this.minutes = Number.parseInt(timeParts[1]); 
                if(timeParts.length > 2) {
                    if(timeParts[2].indexOf(".") > -1) {
                        let subParts: string[] = timeParts[2].split("\\.");
                        this.seconds = Number.parseInt(subParts[0]); 
                        this.nano = Number.parseInt(subParts[1]) * Math.pow(10, (9 - subParts[1].length)); 
                    } else {
                        this.seconds = Number.parseInt(timeParts[2]); 
                        this.nano = 0;
                    }
                }
            }
        } else {
            let dt = new Date();
            this.hours = dt.getHours();
            this.minutes = dt.getMinutes();
            this.seconds = dt.getSeconds();
            this.nano = 0;
            this.zoneId = Intl.DateTimeFormat().resolvedOptions().timeZone;
        }
    }

    public getHours(): number {
        return this.hours;
    }

    public getMinutes(): number {
        return this.minutes;
    }

    public getSeconds(): number {
        return this.seconds;
    }

    public setHours(h: number) {
        this.hours = h;
    }

    public setMinutes(m: number) {
        this.minutes = m;
    }

    public setSeconds(s: number) {
        this.seconds = s;
    }

    public atDate(date: Date) : Date {
        let inputDate = new Date(date.getTime());
        inputDate.setMilliseconds(0);
        let tempDate = new Date(inputDate.toLocaleString("en-US", {timeZone: this.zoneId}));
        tempDate.setMilliseconds(0);
        var diff = inputDate.getTime() - tempDate.getTime();
        tempDate.setHours(this.hours);
        tempDate.setMinutes(this.minutes);
        tempDate.setSeconds(this.seconds);
        tempDate.setMilliseconds(this.nano / 1000000);
        var outputDate = new Date(tempDate.getTime() + diff);
        return outputDate;
    }

    public toString(): string {
        let str = "T";
        str = str + this.hours.toString().padStart(2, '0');
        str = str + ":";
        str = str + this.minutes.toString().padStart(2, '0');
        str = str + ":";
        str = str + this.seconds.toString().padStart(2, '0');
        if(this.nano > 0) {
            str = str + ".";
			if((this.nano / 1000000) - Math.floor(this.nano / 1000000) == 0) {
                str = str + (this.nano / 1000000).toString().padStart(3, "0");
            } else if((this.nano / 1000) - Math.floor(this.nano / 1000) == 0) {
                str = str + (this.nano / 1000).toString().padStart(6, "0");
			} else {
                str = str + (this.nano).toString().padStart(9, "0");
			}
        }
        str = str + "[";
        str = str + this.zoneId;
        str = str + "]";
        return str;
    }

}