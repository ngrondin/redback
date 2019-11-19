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
        this.related = json.related;
        this.validation = json.validation;
        this.changed = [];
        this.dataService = ds;
    }

    updateFromServer(json: any) {
        for(const attribute of json.data) {
            if(json.data[attribute] != this.data[attribute])
                this.data[attribute] = json.data[attribute];
        }
    }

    setValue(attribute: string, value: any) {
        if(this.validation[attribute].editable == true) {
            this.data[attribute] = value;
            this.changed[attribute] = true;
            if(this.related[attribute] != null) 
                this.related[attribute] = null;
            if(this.dataService.saveImmediatly)
                this.saveToServer();
        }
    }

    saveToServer() {
        this.dataService.updateObjectToServer(this);
    }

}