import { Input, ViewChild } from "@angular/core";
import { Component } from "@angular/core";
import { HostBinding } from "@angular/core";
import { OnInit, AfterViewInit, ViewContainerRef } from "@angular/core";
import { RbDataObserverComponent } from "./rb-dataobserver";

@Component({template: ''})
export abstract class RbContainerComponent extends RbDataObserverComponent {
    @Input('grow') grow: number;
    @Input('shrink') shrink: number;
    @Input('basis') basis: string;
    @Input('color') color: string;
    @Input('width') width: number = null;
    @Input('height') height: number = null;
    @ViewChild('container', { read: ViewContainerRef, static: true }) container: ViewContainerRef;
    @HostBinding('style.flex-grow') get flexgrow() { return this.sizeIsSet ? null : this.grow != null ? this.grow : 1;}
    @HostBinding('style.flex-shrink') get flexshrink() { return this.sizeIsSet ? null : this.shrink != null ? this.shrink : 1;}
    @HostBinding('style.flex-basis') get flexbasis() { return this.sizeIsSet ? null : this.basis != null ? this.basis : this.grow == 0 ? "auto" : 0;}
    @HostBinding('style.width') get containerwidth() { return (this.width != null ? ((0.88 * this.width) + 'vw'): null);}
    @HostBinding('style.height') get containerheight() { return (this.height != null ? ((0.88 * this.height) + 'vw'): null);}
    @HostBinding('style.background-color') get backgroundColor() { return this.color != null ? this.color : null;}

    constructor() {
        super();
    }
  
    dataObserverInit(): void {
        this.containerInit();
    }

    dataObserverDestroy() : void  {
        this.containerDestroy();
    }

    get sizeIsSet() {
        return this.width != null || this.height != null;
    }

    abstract containerInit();

    abstract containerDestroy();
  
  
  }