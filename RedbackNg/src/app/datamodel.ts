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

    setValue(attribute: string, value: any) {
        this.setValueAndRelated(attribute, value, null);
    }

    setValueAndRelated(attribute: string, value: any, related: RbObject) {
        if(this.validation[attribute].editable == true) {
            this.data[attribute] = value;
            this.changed.push(attribute);
            if(this.related[attribute] != null) 
                this.related[attribute] = related;
            if(this.dataService.saveImmediatly)
                this.saveToServer();
        }
    }

    saveToServer() {
        this.dataService.updateObjectToServer(this);
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