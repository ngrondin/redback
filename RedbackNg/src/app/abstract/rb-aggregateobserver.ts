import { HostBinding } from "@angular/core";
import { OnInit, Input } from "@angular/core";
import { RbAggregatesetComponent } from "app/rb-aggregateset/rb-aggregateset.component";
import { Subscription } from "rxjs";
import { RbComponent } from "./rb-component";

export abstract class RbAggregateObserverComponent extends RbComponent {
    @Input('aggregateset') aggregateset: RbAggregatesetComponent;
    @Input('show') show: string;
    
    public aggregatesetSubscription: Subscription;
    public showResult: boolean = true;

    @HostBinding('style.display') get visitility() {
        return this.showResult ? 'flex' : 'none';
    }

    constructor() {
        super();
    }

    componentInit() {
        if(this.aggregateset != null) {
            this.aggregatesetSubscription = this.aggregateset.getObservable().subscribe(event => this.internalAggregatesetEvent(event));
        }
        if(this.show != null) {
            this.evalShow()
        } else {
            this.showResult = true;
        }
        this.aggregatesetObserverInit();
    }

    componentDestroy() {
        if(this.aggregatesetSubscription != null) {
            this.aggregatesetSubscription.unsubscribe();
        }        
        this.aggregatesetObserverDestroy();
    }

    abstract aggregatesetObserverInit();

    abstract aggregatesetObserverDestroy();

    abstract onAggregatesetEvent(event: string);


    private internalAggregatesetEvent(event: string) {
        if(this.show != null) {
            this.evalShow();
        }
        this.onAggregatesetEvent(event);
    }

    evalShow() {
        if(this.show == 'true') {
            this.showResult = true;
        } else if(this.show == 'false') {
            this.showResult = false;
        } else {
            let str: string = decodeURIComponent(this.show);
            let relatedObject = this.aggregateset != null ? this.aggregateset.relatedObject : null;
            if(!(str.indexOf("relatedObject.") > -1 && relatedObject == null)) {
                this.showResult = eval(str);            
            } else {
                this.showResult = false;
            }
        }
    }
  }