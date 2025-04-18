import { HostBinding } from "@angular/core";
import { Component } from "@angular/core";
import { Input } from "@angular/core";
import { RbObject } from "app/datamodel";
import { RbAggregatesetComponent } from "app/rb-aggregateset/rb-aggregateset.component";
import { RbDatasetComponent } from "app/rb-dataset/rb-dataset.component";
import { DatasetListMap, DatasetMap, RbDatasetGroupComponent } from "app/rb-datasetgroup/rb-datasetgroup.component";
import { Subscription } from "rxjs";
import { RbComponent } from "./rb-component";
import { Evaluator } from "app/helpers";



@Component({template: ''})
export abstract class RbDataObserverComponent extends RbComponent {
    @Input('dataset') dataset: RbDatasetComponent;
    @Input('datasetgroup') datasetgroup: RbDatasetGroupComponent;
    @Input('aggregateset') aggregateset: RbAggregatesetComponent;
    @Input('object') _rbObject: RbObject;
    @Input('show') showExpr: string;
    @HostBinding('style.display') get visitility() { return this.show ? 'flex' : 'none'; }
    
    public globalSubscription: Subscription;
    public datasetSubscription: Subscription;
    public datasetGroupSubscription: Subscription;
    public aggregatesetSubscription: Subscription;
    public show: boolean = true;

    constructor() {
        super();
    }

    componentInit() {
        this.globalSubscription = window.redback.getObservable().subscribe(event => this.internalDatasetEvent(event));
        if(this.dataset != null) {
            this.datasetSubscription = this.dataset.getObservable().subscribe(event => this.internalDatasetEvent(event));
        }
        if(this.datasetgroup != null) {
            this.datasetGroupSubscription = this.datasetgroup.getObservable().subscribe(event => this.internalDatasetEvent(event));
        }
        if(this.aggregateset != null) {
            this.aggregatesetSubscription = this.aggregateset.getObservable().subscribe(event => this.internalDatasetEvent(event));
        }        
        if(this.showExpr != null) {
            this.evalShow()
        } else {
            this.show = true;
        }
        this.dataObserverInit();
    }

    componentDestroy() {
        this.globalSubscription.unsubscribe();
        if(this.datasetSubscription != null) {
            this.datasetSubscription.unsubscribe();
        }
        if(this.datasetGroupSubscription != null) {
            this.datasetGroupSubscription.unsubscribe();
        }
        if(this.aggregatesetSubscription != null) {
            this.aggregatesetSubscription.unsubscribe();
        }          
        this.dataObserverDestroy();
    }

    private internalDatasetEvent(event: string) {
        if(this.showExpr != null) {
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

    get lists(): DatasetListMap {
        return this.datasetgroup != null ? this.datasetgroup.lists : null;
    }

    get aggregateList(): any {
        return this.aggregateset != null ? this.aggregateset.list : null;
    }

    get rbObject() : RbObject {
        return this.dataset != null ? this.dataset.selectedObject : this.datasetgroup != null ? this.datasetgroup.selectedObject : this._rbObject != null ? this._rbObject : null;
    }

    get selectedObject() : RbObject {
        return this.dataset != null ? this.dataset.selectedObject : this.datasetgroup != null ? this.datasetgroup.selectedObject : this._rbObject != null ? this._rbObject : null;
    }

    get relatedObject() : RbObject {
        return this.dataset != null ? this.dataset.relatedObject : null;
    }

    get isLoading() : boolean {
        return this.dataset != null ? this.dataset.isLoading : this.datasetgroup != null ? this.datasetgroup.isLoading : this.aggregateset != null ? this.aggregateset.isLoading : false;
    }

    evalShow() {
        this.show = Evaluator.eval(decodeURIComponent(this.showExpr), this.rbObject, this.relatedObject) ?? false;
    }
  }