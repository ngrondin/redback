import { DataService } from './services/data.service';
import { analyzeAndValidateNgModules } from '@angular/compiler';
import { RbDatasetComponent } from './rb-dataset/rb-dataset.component';
import { ValueComparator } from './helpers';
import { NotificationService } from './services/notification.service';

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
    changed: any = [];
    dataService: DataService;
    lastUpdated: number;
    datasets: RbDatasetComponent[] = [];

    constructor(json: any, ds: DataService) {
        this.dataService = ds;
        this.uid = json.uid;
        this.objectname = json.objectname;
        this.domain = json.domain;
        this.updateFromServer(json);
    }

    updateFromServer(json: any) {
        const inData: any = json.data;
        let isChanged: boolean = false;
        for(const attribute in json.data) {
            if(this.data[attribute] === undefined || ValueComparator.notEqual(this.data[attribute], json.data[attribute])) {
                this.data[attribute] = json.data[attribute];
                isChanged = true;
            }

            if(json.related != null) {
                if(json.related[attribute] != null) {
                    let related = this.dataService.updateObjectFromServer(json.related[attribute]);
                    if(this.related[attribute] != related) {
                        this.related[attribute] = related;
                    }
                } else {
                    this.related[attribute] = null;
                }
            }

            if(json.validation != null && json.validation[attribute] != null) {
                this.validation[attribute] = json.validation[attribute];
                let relatedValid = this.validation[attribute].related;
                if(relatedValid != null && this.related[attribute] == null) {
                    if(relatedValid.link == 'uid') {
                        this.related[attribute] = this.dataService.getLocalObject(relatedValid.object, this.data[attribute]);
                    } else {
                        let filter = {};
                        filter[relatedValid.link] = this.data[attribute];
                        this.related[attribute] = this.dataService.findFirstLocalObject(relatedValid.object, filter);
                    }
                }
            }
        }

        if(isChanged) {
            this.changed = [];
            this.lastUpdated = (new Date()).getTime();
            this._adviseSetsOfChange();
        }
    }

    refresh() {
        this.dataService.getServerObject(this.objectname, this.uid).subscribe((obj) => {});
    }

    get(attr: string) : any {
        let ret: any = null;
        if(attr != null) {
            if(attr == 'uid') {
                ret = this.uid;
            } else if(attr.indexOf('.') == -1) {
                ret = this.data[attr];
            } else {
                let relationship = attr.substring(0, attr.indexOf('.'));
                let finalattr = attr.substring(attr.indexOf('.') + 1);
                if(this.related[relationship] != null) {
                    ret = this.related[relationship].data[finalattr];
                } 
            }
        }
        if(typeof ret == 'undefined') ret = null;
        return ret;
    }

    getRelated(attr: string) : RbObject {
        let ret: RbObject = null;
        if(attr != null) {
            if(attr.indexOf('.') == -1) {
                ret = this.related[attr];
            }
        }
        if(typeof ret == 'undefined') ret = null;
        return ret;        
    }
    
    canEdit(attribute: string) : boolean {
        return (this.validation != null && this.validation[attribute] != null && this.validation[attribute].editable == true);
    }

    setValue(attribute: string, value: any) {
        let ret = this._setValueAndRelated(attribute, value, null);
        this._afterSetValue(ret);
    }

    setValues(map: any) {
        this.setValuesAndRelated(map, null);
    }

    setValueAndRelated(attribute: string, value: any, related: RbObject) {
        let ret = this._setValueAndRelated(attribute, value, related);
        this._afterSetValue(ret);        
    }

    setValuesAndRelated(valueMap: any, relatedMap: any) {
        let ret: boolean = false;
        for(let key of Object.keys(valueMap)) {
            ret = this._setValueAndRelated(key, valueMap[key], relatedMap[key]) || ret;
        }
        this._afterSetValue(ret);
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
            if(this.changed.indexOf(attribute) == -1) this.changed.push(attribute);
            return true;
        }
        return false;
    }

    _afterSetValue(updated: boolean) {
        if(updated) {
            this.lastUpdated = (new Date()).getTime();
            this._adviseSetsOfChange();
            if(this.dataService.saveImmediatly) {
                this.dataService.updateObjectToServer(this);
            }            
        }
    }

    _adviseSetsOfChange() {
        for(let set of this.datasets) {
            set.objectUpdated(this);
        }
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

    addSet(set: RbDatasetComponent) {
        if(this.datasets.indexOf(set) == -1) {
            this.datasets.push(set);
        }
    }

    removeSet(set: RbDatasetComponent) {
        let index = this.datasets.indexOf(set);
        if(index > -1) {
            this.datasets.splice(index);
        }
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


export class RbNotification {
    process: string;
    pid: string;
    code: string;
    type: string;
    label: string;
    message: string;
    actions: RbNotificationAction[];
    data: any;
    notificationService: NotificationService;

    constructor(json: any, ns: NotificationService) {
        this.notificationService = ns;
        this.process = json.process;
        this.pid = json.pid;
        this.code = json.code;
        this.type = json.type;
        this.label = json.label;
        this.message = json.message;
        this.data = json.data;
        if(json.actions != null) {
            this.actions = [];
            for(var actionJson of json.actions) {
                this.actions.push(new RbNotificationAction(actionJson));
            }
        }
    }
}

export class RbNotificationAction {
    action: string;
    description: string;
    main: boolean;

    constructor(json: any) {
        this.action = json.action;
        this.description = json.description;
        this.main = json.main;
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


export class DataTarget {
    objectname: string;
    filter: any;
    sort: any;
    search: string;
    selectedObject: RbObject;
  
    constructor(o: string, f: any, s: string) {
      this.objectname = o
      this.filter = f;
      this.search = s;
    }
  }
  
  export class ViewTarget {
    domain: string;
    view: string;
    title: string;
    additionalTitle: string;
    _breadcrumbLabel: string;
    mode: string;
    dataTarget: DataTarget;
  
    constructor(dom: string, v: string, o: string, f: any, s: string) {
      this.domain = dom;
      this.view = v;
      this.title = null;
      if(f != null) {
        this.dataTarget = new DataTarget(o, f, s);
      }
    }

    get fulltitle() : string {
        return (this.title != null ? this.title : '') + (this.additionalTitle != null ? (this.title != null ? " - " : "") + this.additionalTitle : "");
    }
  
    get breadcrumbLabel(): string {
      if(this._breadcrumbLabel != null) {
        return this._breadcrumbLabel;
      } else {
        return this.title; 
      }
    }
  
    set breadcrumbLabel(v: string) {
      this._breadcrumbLabel = v;
    }
  }