import { Component, ComponentRef, Input,  } from '@angular/core';
import { ApiService } from 'app/services/api.service';
import { UserprefService } from 'app/services/userpref.service';
import { NotificationService } from 'app/services/notification.service';
import { Observable, Subscription } from 'rxjs';
import { RbNotification } from 'app/datamodel';
import { Evaluator } from 'app/helpers';
import { ActionService } from 'app/services/action.service';
import { PopupService } from 'app/services/popup.service';
import { RbPopupActionsComponent } from 'app/popups/rb-popup-actions/rb-popup-actions.component';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { RbDataButtonComponent } from '../abstract/rb-databutton';


export class RbActiongroupAction {
  action: string;
  target: string;
  param: any;
  timeout: number;
  label: string;
  confirm: string;
  focus: boolean;
  show: string;

  constructor(a: string, t: string, p: any, to:number, l: string, c: string, f: boolean, s: string) {
    this.action = a;
    this.target = t;
    this.param = p;
    this.timeout = to;
    this.label = l;
    this.confirm = c;
    this.focus = f;
    this.show = s;
  }
}

@Component({
  selector: 'rb-actiongroup',
  templateUrl: '../rb-button/rb-button.html',
  styleUrls: ['../rb-button/rb-button.css']
})
export class RbActiongroupComponent extends RbDataButtonComponent {
  @Input('actions') actions: any;
  @Input('menucategory') menucategory: string;
  @Input('domaincategory') domaincategory: string; // Decrecated
  @Input('showprocessinteraction') showprocessinteraction: boolean = false;
  @Input('script') script: string = null;
  @Input('round') round: boolean = false;
  @Input('hideonempty') hideonempty: boolean = false;
  
  domainActions: RbActiongroupAction[];
  actionData: RbActiongroupAction[] = [];
  notification: RbNotification;
  notificationRetreived: boolean = false;
  notificationSubscription: Subscription;
  scriptActions: any[];
  
  popupComponentRef: ComponentRef<RbPopupComponent>;

  constructor(
    private apiService: ApiService,
    private actionService: ActionService,
    private notificationService: NotificationService,
    public userpref: UserprefService,
    public popupService: PopupService
  ) {
    super();
    this.label = 'Actions';
  }
  

  dataObserverInit() {
    if(this.showprocessinteraction) {   
      this.notificationSubscription = this.notificationService.getObservable().subscribe(event => this.onNotificationEvent(event));
    }
    let category = this.menucategory != null && this.menucategory != "" ? this.menucategory : this.domaincategory != null && this.domaincategory != "" ? this.domaincategory : null;
    if(category != null) {
      this.domainActions = [];
      this.apiService.listScripts(category).subscribe(json => {
        for(var item of json.list) {
          this.domainActions.push(new RbActiongroupAction('executeglobal', item.name, null, item.timeout, item.description, item.confirm, false, item.show));
        }
        this.apiService.listDomainFunctions(category).subscribe(json => {
          for(var item of json.result) {
            this.domainActions.push(new RbActiongroupAction('executedomain', item.name, null, item.timeout, item.description, item.confirm, false, null));
          }
          this.calcActionData();
        });
      });
    } else {
      this.calcActionData();
    }
  }

  dataObserverDestroy() {
    if(this.notificationSubscription != null) {
      this.notificationSubscription.unsubscribe();
    }
  }

  onDatasetEvent(event: any) {
    if(event == 'select') {
      this.getNotification().subscribe(() => {
        this.getScriptActions().subscribe(() => {
          this.calcActionData();
        })
      });
    } else if(event == 'update') {
      this.calcActionData();
    } else if(event == 'clear') {
      this.notification = null;
      this.calcActionData();
    }
  }

  onActivationEvent(event: any) {
    if(this.active == true) {

    }
  }

  onNotificationEvent(event: any) {
    if(this.showprocessinteraction) {
      if(event.type == 'notification') {
        if(this.rbObject != null && event.notification.data != null && this.rbObject.objectname == event.notification.data.objectname && this.rbObject.uid == event.notification.data.uid) {
          this.notification = event.notification;
          this.notificationRetreived = true;
          this.calcActionData();
        }
      } else if(event.type == 'completion') {
        if(this.notification === event.notification) {
          this.notification = null;
          this.notificationRetreived = false;
          this.calcActionData();
        }
      }  
    }
  }

  get open() : boolean {
    return this.popupComponentRef != null;
  }

  get enabled() : boolean {
    return this.actionData.length > 0;
  }

  get focus() : boolean {
    if(!this.open && this.showprocessinteraction && this.notification != null) {
      for(var action of this.notification.actions) {
        if(action.main == true) return true;
      }
    }
    return false;
  }

  click() {
    if(this.popupComponentRef == null) {
      if(this.showprocessinteraction && this.notificationRetreived == false && this.rbObject != null) {
        this.getNotification().subscribe(() => {
          this.calcActionData();
          this.openPopup();
        });        
      } else {
        this.openPopup();
      }
    } else {
      this.closePopup();
    }
  }
  
  public openPopup() {
    this.popupComponentRef = this.popupService.openPopup(this.buttonContainerRef, RbPopupActionsComponent, {actions: this.actionData});
    this.popupComponentRef.instance.selected.subscribe(value => this.clickAction(value));
    this.popupComponentRef.instance.cancelled.subscribe(() => this.closePopup());
  }

  public closePopup() {
    this.popupService.closePopup();
    this.popupComponentRef = null;
  }
  
  private calcActionData() {
    this.actionData = [];
    let object = this.rbObject;
    let relatedObject = this.dataset != null ? this.dataset.relatedObject : null;
    if(this.showprocessinteraction && this.notification != null) {
      for(var action of this.notification.actions) {
        this.actionData.push(new RbActiongroupAction("processaction", action.action, null, null, action.description, action.confirm, action.main, null));
      }
    }
    if(this.actions != null) {
      this.actions.forEach(item => {
        if(item.show == null || item.show == true || (typeof item.show == 'string' && (Evaluator.eval(item.show, this.rbObject, this.relatedObject) == true))) {
          let swtch = this.userpref.getCurrentViewUISwitch('action',  item.action + "_" + item.param);
          if(swtch == null || swtch == true) {
            this.actionData.push(new RbActiongroupAction(item.action, item.target, item.param, item.timeout, item.label, item.confirm, false, null));
          }
        }
      });
    }
    if(this.scriptActions != null) {
      for(const item of this.scriptActions) {
        let param = {action: "'" + item.action + "'", objectname: "'" + this.rbObject?.objectname + "'", uid: "'" + this.rbObject?.uid + "'"};
        this.actionData.push(new RbActiongroupAction("executeglobal", this.script, param, 10000, item.label, null, false, null));
      }
    }
    if(this.domainActions != null) {
      this.domainActions.forEach(item => {
        if(item.show == null || (item.show != null && Evaluator.eval(item.show, this.rbObject, this.relatedObject))) {
          this.actionData.push(item);
        }
      });
    }
  }

  private getNotification() : Observable<null> {
    const obs = new Observable<null>((observer) => {
      if(this.showprocessinteraction == true && this.rbObject != null) {
        this.notificationService.getNotificationFor(this.rbObject.objectname, this.rbObject.uid).subscribe(notif => {
          this.notification = notif;
          this.notificationRetreived = true;
          observer.next();
          observer.complete();
        }, (error) => {
          observer.error(error);
        });
      } else {
        observer.next();
        observer.complete();
      }
    });
    return obs;
  }

  private getScriptActions() : Observable<null> {
    const obs = new Observable<null>((observer) => {
      if(this.script != null) {
        let param = {action:"listactions"};
        if(this.rbObject != null) {
          param['objectname'] = this.rbObject.objectname;
          param['uid'] = this.rbObject.uid;
        }
        this.apiService.executeGlobal(this.script, param).subscribe(resp => {
          this.scriptActions = resp.data.list;
          observer.next();
          observer.complete();
        }, (error) => {
          observer.error(error);
        });
      } else {
        observer.next();
        observer.complete();
      }
    });
    return obs;
  }

  public clickAction(action: RbActiongroupAction) {
    this.closePopup();
    if(action.action == 'processaction' && this.notification != null) {
      let notif = this.notification;
      this.running = true;
      this.notificationService.actionNotification(this.notification, action.target, action.confirm).subscribe(() => {
          if(this.notification === notif) {
            this.notification = null;
            this.notificationRetreived = false;
          }
        }).add(() => {
          this.running = false;
          this.calcActionData();
        });
    } else {
      this.running = true;
      this.actionService.action(this.dataset, action.action, action.target, action.param, null, action.confirm, action.timeout).subscribe().add(() => {
        this.running = false;
      });
    }
  }
}
