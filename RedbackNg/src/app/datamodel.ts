import { DataService } from './data.service';
import { analyzeAndValidateNgModules } from '@angular/compiler';

export class ObjectResp {
    objects: object;
}


export class RbObject {
    objectname: string;
    uid: string;
    domain: string;
    data: any;
    related: any;
    validation: any;
    changed: any;
    dataService: DataService;

    constructor(json: any, ds: DataService) {
        this.uid = json.uid;
        this.objectname = json.objectname;
        this.domain = json.domain;
        this.data = json.data;
        this.related = json.related != null ? json.related : {};
        this.validation = json.validation != null ? json.validation : {};
        this.changed = [];
        this.dataService = ds;
        this.resolveRelatedObjects();
    }

    updateFromServer(json: any) {
        const inData: any = json.data;
        for(const attribute in json.data) 
            this.data[attribute] = json.data[attribute];
        if(json.validation != null) 
            for(const attribute in json.validation) 
                this.validation[attribute] = json.validation[attribute];
        if(json.related != null)
            for(const attribute in json.related) 
                this.related[attribute] = json.related[attribute];
        this.changed = [];
        this.resolveRelatedObjects();
    }

    resolveRelatedObjects() {
        for(const attribute in this.related) {
            this.related[attribute] = this.dataService.getLocalObject(this.related[attribute].objectname, this.related[attribute].uid);
        }
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
            if(this.related[attribute] != null) {
                this.related[attribute] = related;
            }
            this.changed.push(attribute);
            return true;
        }
        return false;
    }

}




export class RbFile {
    fileUid: string;
    relatedObject: string;
    relatedUid: string;
    fileName: string;
    mime: string;
    username: string;
    date: Date;

    constructor(json: any, ds: DataService) {
        this.fileUid = json.fileuid;
        this.relatedObject = json.relatedobject;
        this.relatedUid = json.relateduid;
        this.fileName = json.filename;
        this.mime = json.mime;
        this.username = json.username;
        this.date = new Date(json.date);
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