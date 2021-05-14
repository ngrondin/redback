import { Component } from "@angular/core";
import { Input } from "@angular/core";
import { DataTarget } from "app/datamodel";
import { RbContainerComponent } from "./rb-container";

@Component({template: ''})
export abstract class RbSetComponent extends RbContainerComponent {
    @Input('object') object: string;
    @Input('basefilter') baseFilter: any;
    @Input('master') master: any;
    @Input('requiresuserfilter') requiresuserfilter: boolean = false;
    @Input('datatarget') dataTarget: DataTarget;
    @Input('ignoretarget') ignoretarget: boolean = false;
  
    constructor() {
        super();
    }
  
    containerInit(): void {
        this.setInit();
    }

    containerDestroy() : void  {
        this.setDestroy();
    }

    abstract setInit();

    abstract setDestroy();

    public abstract reset();

    public abstract clear();

}