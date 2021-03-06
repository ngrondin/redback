import { Input, ViewChild } from "@angular/core";
import { HostBinding } from "@angular/core";
import { OnInit, AfterViewInit, ViewContainerRef } from "@angular/core";
import { RbDataObserverComponent } from "./rb-dataobserver";

export abstract class RbContainerComponent extends RbDataObserverComponent {
    @Input('grow') grow: number;
    @Input('shrink') shrink: number;
    @Input('color') color: string;
    @ViewChild('container', { read: ViewContainerRef, static: true }) container: ViewContainerRef;
    @HostBinding('style.flex-grow') get flexgrow() { return this.grow != null ? this.grow : 1;}
    @HostBinding('style.flex-shrink') get flexshrink() { return this.shrink != null ? this.shrink : 1;}
    @HostBinding('style.background-color') get backgroundColor() { return this.color != null ? this.color : null;}

    //private afterViewInitCallback;

    constructor() {
        super();
    }
  
    dataObserverInit(): void {
        this.containerInit();
        /*if(this.afterViewInitCallback != null) {
          this.afterViewInitCallback();
        }*/
    }

    dataObserverDestroy() : void  {
        this.containerDestroy();
    }

    abstract containerInit();

    abstract containerDestroy();
  
    /*ngAfterViewInit(): void {
      if(this.afterViewInitCallback != null) {
        this.afterViewInitCallback();
      }
    }
  
    public afterViewInit(cb) {
      this.afterViewInitCallback = cb;
    }
  
  
    public getContainer() : ViewContainerRef {
      return this.container;
    }*/
  
  }