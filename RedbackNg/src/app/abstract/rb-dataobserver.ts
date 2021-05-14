import { HostBinding } from "@angular/core";
import { Component } from "@angular/core";
import { Input } from "@angular/core";
import { RbObject } from "app/datamodel";
import { RbDatasetComponent } from "app/rb-dataset/rb-dataset.component";
import { RbDatasetGroupComponent } from "app/rb-datasetgroup/rb-datasetgroup.component";
import { Subscription } from "rxjs";
import { RbComponent } from "./rb-component";

@Component({template: ''})
export abstract class RbDataObserverComponent extends RbComponent {
    @Input('dataset') dataset: RbDatasetComponent;
    @Input('datasetgroup') datasetgroup: RbDatasetGroupComponent;
    @Input('show') show: string;
    @HostBinding('style.display') get visitility() { return this.showResult ? 'flex' : 'none'; }
    
    public datasetSubscription: Subscription;
    public datasetGroupSubscription: Subscription;
    public showResult: boolean = true;

    constructor() {
        super();
    }

    componentInit() {
        if(this.dataset != null) {
            this.datasetSubscription = this.dataset.getObservable().subscribe(event => this.internalDatasetEvent(event));
        }
        if(this.datasetgroup != null) {
            this.datasetGroupSubscription = this.datasetgroup.getObservable().subscribe(event => this.internalDatasetEvent(event));
        }
        if(this.show != null) {
            this.evalShow()
        } else {
            this.showResult = true;
        }
        this.dataObserverInit();
    }

    componentDestroy() {
        if(this.datasetSubscription != null) {
            this.datasetSubscription.unsubscribe();
        }
        if(this.datasetGroupSubscription != null) {
            this.datasetGroupSubscription.unsubscribe();
        }
        this.dataObserverDestroy();
    }

    private internalDatasetEvent(event: string) {
        if(this.show != null) {
            this.evalShow();
        }
        this.onDatasetEvent(event);
    }

    abstract dataObserverInit();

    abstract dataObserverDestroy();

    abstract onDatasetEvent(event: string);

    get list(): RbObject[] {
        return this.dataset != null ? this.dataset.list : null;
    }

    get lists(): any {
        return this.datasetgroup != null ? this.datasetgroup.lists : null;
    }

    get rbObject() : RbObject {
        return this.dataset != null ? this.dataset.selectedObject : null;
    }

    get selectedObject() : RbObject {
        return this.dataset != null ? this.dataset.selectedObject : null;
    }

    get isLoading() : boolean {
        return this.dataset != null ? this.dataset.isLoading : false;
    }

    evalShow() {
        if(this.show == 'true') {
            this.showResult = true;
        } else if(this.show == 'false') {
            this.showResult = false;
        } else if(this.dataset != null && (this.dataset.selectedObject != null || this.dataset.relatedObject != null)) {
            let str: string = decodeURIComponent(this.show);
            let object = this.dataset.selectedObject;
            let relatedObject = this.dataset.relatedObject;
            if(!((str.indexOf("object.") > -1 && object == null) || (str.indexOf("relatedObject.") > -1 && relatedObject == null))) {
                this.showResult = eval(str);            
            } else {
                this.showResult = false;
            }
        } else {
            this.showResult = false;
        }
    }
  }