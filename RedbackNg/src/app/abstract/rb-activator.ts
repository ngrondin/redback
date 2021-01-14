import { OnInit } from "@angular/core";
import { Observable, Observer } from "rxjs";
import { RbContainerComponent } from "./rb-container";

export abstract class RbActivatorComponent extends RbContainerComponent implements OnInit {
  activatorOn: boolean;
  private activationObservers: Observer<boolean>[] = [];

  containerInit() {
    this.activatorInit();
  }

  containerDestroy() {
      this.activatorDestroy();
  }

  abstract activatorInit();

  abstract activatorDestroy();

  getActivationObservable() : Observable<boolean>  {
    return new Observable<boolean>((observer) => {
      this.activationObservers.push(observer);
    });
  }

  public activate() {
    this.activatorOn = true;
    this.publishState();
  }

  public deactivate() {
    this.activatorOn = false;
    this.publishState();
  }

  private publishState() {
    this.activationObservers.forEach((observer) => {
        observer.next(this.activatorOn);
    }); 
  }
}