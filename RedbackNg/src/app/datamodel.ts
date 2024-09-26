import { DataService } from './services/data.service';
import { RbDatasetComponent } from './rb-dataset/rb-dataset.component';
import { ValueComparator } from './helpers';
import { NotificationService } from './services/notification.service';
import { FileService } from './services/file.service';

export class ObjectResp {
    objects: object;
}


export class RbObject {
    objectname: string;
    uid: string;
    domain: string;
    lastupdate: number;
    data: any = {};
    related: any = {};
    validation: any = {};
    flags: any = {};
    updatedAttributes: any = [];
    deleted: boolean = false;
    dataService: DataService;
    lastUpdated: number;
    datasets: RbDatasetComponent[] = [];
    
    constructor(json: any, ds: DataService) {
        this.dataService = ds;
        this.uid = json.uid;
        this.objectname = json.objectname;
        this.domain = json.domain;
        this.updateFromJSON(json);
    }

    updateFromJSON(json: any) {
        if(json.ts >= (this.lastUpdated ?? 0)) {
            const inData: any = json.data;
            let isChanged: boolean = false;
            for(const attribute in json.data) {
                if(json.validation != null && json.validation[attribute] != null) {
                    this.validation[attribute] = json.validation[attribute];
                }
    
                if(ValueComparator.notEqual(this.data[attribute], json.data[attribute])) {
                    isChanged = true;
                    this.data[attribute] = json.data[attribute];
                    if((this.validation[attribute] != null && this.validation[attribute].related != null) || this.related[attribute] !== undefined) {
                        this.related[attribute] = null;
                        this._setAttributeFlag(attribute, 'reqrel', false);
                    } 
                }
    
                if(json.related != null && json.related[attribute] != null) {
                    let related = this.dataService.receive(json.related[attribute]);
                    if(this.related[attribute] != related) {
                        this.related[attribute] = related;
                        isChanged = true;
                    }
                }
            }

            if(json.deleted == true) {
                this.deleted = true;
                isChanged = true;
            }
    
            if(json.validation != null) {
                this.validation._candelete = json.validation._candelete;
                this._linkMissingRelated();
            }
    
            if(isChanged) {
                this.updatedAttributes = [];
                this._adviseSetsOfChange();
            }

            this.lastUpdated = json.ts;
        }
    }

    linkMissingRelated() {
        if(this._linkMissingRelated()) {
            this._adviseSetsOfChange();
        }
    }

    _linkMissingRelated() {
        let isChanged: boolean = false;
        for(const attribute in this.data) {
            if(this.validation[attribute] != null && this.validation[attribute].related != null && this.related[attribute] == null && this.data[attribute] != null) {
                let relatedRule = this.validation[attribute].related;
                if(relatedRule != null && this.related[attribute] == null && relatedRule.uiresolve != false) {
                    let relatedObject = null, uid = null, filter = null;
                    if(relatedRule.link == 'uid') {
                        uid = this.data[attribute];
                        relatedObject = this.dataService.get(relatedRule.object, uid);
                    } else {
                        filter = {...relatedRule.listfilter};
                        filter[relatedRule.link] = this.data[attribute];
                        var relatedObjectOptions = this.dataService.list(relatedRule.object, filter);
                        var selectedPoints = 0;
                        for(var option of relatedObjectOptions) {
                            var point = option.domain == this.domain ? 3 : option.domain == 'root' ? 1 : 2;
                            if(point > selectedPoints) {
                                selectedPoints = point;
                                relatedObject = option;
                            }
                        }
                    }
                    if(relatedObject != null) {
                        isChanged = true
                        this.related[attribute] = relatedObject;
                    } else {
                        if(this.flags[attribute] == null || this.flags[attribute]['reqrel'] != true) {
                            if(uid != null) this.dataService.enqueueDeferredFetch(relatedRule.object, uid, this);
                            else this.dataService.enqueueDeferredFetchList(relatedRule.object, filter, this);
                            this._setAttributeFlag(attribute, 'reqrel', true);
                        } else {
                            //console.log("Can't find related object for " + this.objectname + ":" + this.uid + "." + attribute + " = '" + this.data[attribute] + "'");
                        }
                    }
                }
            } 
        }
        return isChanged;
    }

    _setAttributeFlag(attribute, flag, value) {
        if(this.flags[attribute] == null) this.flags[attribute] = {};
        this.flags[attribute][flag] = value;
    }

    refresh() {
        this.dataService.fetch(this.objectname, this.uid).subscribe((obj) => {});
    }

    get(attr: string) : any {
        let ret: any = null;
        if(attr != null) {
            if(attr == 'uid') {
                ret = this.uid;
            } else if(attr.indexOf('.') == -1) {
                ret = this.data[attr];
            } else {
                let base = attr.substring(0, attr.indexOf('.'));
                if(this.related[base] != null) {
                    let rest = attr.substring(attr.indexOf('.') + 1);
                    ret = this.related[base].get(rest);
                } else if(typeof this.data[base] == 'object') {
                    let parts = attr.split(".");
                    let cur = this.data[base];
                    for(let i = 1; i < parts.length && cur != null; i++) {
                        cur = cur[parts[i]];
                    }
                    ret = cur;
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
            } else {
                let firstRelated = this.related[attr.substring(0, attr.indexOf("."))];
                if(firstRelated != null) {
                    ret = firstRelated.getRelated(attr.substring(attr.indexOf(".") + 1));
                }
            }
        }
        if(typeof ret == 'undefined') ret = null;
        return ret;        
    }
    
    canEdit(attribute: string) : boolean {
        return (this.validation != null && this.validation[attribute] != null && this.validation[attribute].editable == true);
    }

    canDelete() : boolean {
        return (this.validation != null && this.validation._candelete == true);
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
            let val = valueMap[key];
            let rel = relatedMap != null ? relatedMap[key] : null
            ret = this._setValueAndRelated(key, val, rel) || ret;
        }
        this._afterSetValue(ret);
    }    

    _setValueAndRelated(attribute: string, value: any, related: RbObject) : boolean {
        if(attribute == 'uid') {
            if(this.uid == null) {
                this.uid = value;
                this.dataService.create(this.objectname, this.uid, this.data).subscribe(val => {});
            }
        } else if(this.validation[attribute] != null && this.validation[attribute].editable == true) {
            this.data[attribute] = value;
            this.related[attribute] = related;
            if(this.updatedAttributes.indexOf(attribute) == -1) this.updatedAttributes.push(attribute);
            return true;
        }
        return false;
    }

    _afterSetValue(updated: boolean) {
        if(updated) {
            this._adviseSetsOfChange();
            if(this.dataService.saveImmediatly) {
                this.dataService.pushToServer(this);
            }            
        }
    }

    _adviseSetsOfChange() {
        let sets = [...this.datasets]; // this is because while calling the datasets, these can actually remove themselves which mutates the list
        for(let set of sets) {
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

    constructor(json: any, fs: FileService) {
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
                this.related[attribute] = this.dataService.receive(json.related[attribute]);
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
    contextlabel: string;
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
        this.contextlabel = json.contextlabel;
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
    icon: string;
    confirm: string;
    main: boolean;

    constructor(json: any) {
        this.action = json.action;
        this.description = json.description;
        this.icon = json.icon;
        this.confirm = json.confirm;
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
    select: any;
  
    constructor(o: string, f: any, s: string, sl: any) {
      this.objectname = o
      this.filter = f;
      this.search = s;
      this.select = sl;
    }
}

export class NavigateEvent {
    target?: string;
    view?: string;
    domain?: string;
    tab?: string;
    objectname?: string;
    filter?: any;
    search?: string;
    select?: any;
    label?: string;
    reset?: boolean;
}
  
export class NavigateData {
    domain: string;
    view: string;
    tab: string;
    title: string;
    additionalTitle: string;
    _breadcrumbLabel: string;
    mode: string;
    dataTarget: DataTarget;
  
    constructor(dom: string, v: string, t:string, d: DataTarget) {
      this.domain = dom;
      this.view = v;
      this.tab = t;
      this.title = null;
      this.dataTarget = d;
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

export class ChatUser {
    id: string;
    username: string;
    fullname: string;
    available: boolean;

    constructor(json: any) {
        this.id = json.id;
        this.username = json.username;
        this.fullname = json.fullname;
        this.available = json.available;
    }

    get displayName() {
        return this.fullname != null ? this.fullname : this.username;
    }
}

export class ChatMessage {
    id: string;
    date: Date;
    from: ChatUser;
    body: string;
    readby: ChatUser[];

    constructor(json: any) {
        this.id = json.id;
        this.date = new Date(json.date);
        this.body = json.body;
        this.readby = [];
    }

    get isRead() {
        return this.readby.find(u => u.username == window.redback.username) != null
    }
}

export class Chat {
    id: string;
    name: string;
    owner: string;
    latest: Date;
    users: ChatUser[];
    messages: ChatMessage[];
    unreadCount: number;

    constructor(json: any) {
        this.id = json.id;
        this.name = json.name;
        this.owner = json.owner;
        this.latest = new Date(json.latest);
        this.users = [];
        this.messages = [];
        this.unreadCount = 0;
    }
}