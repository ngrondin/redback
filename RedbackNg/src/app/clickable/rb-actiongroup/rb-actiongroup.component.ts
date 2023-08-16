import { Component, ComponentRef, Input,  } from '@angular/core';
import { ApiService } from 'app/services/api.service';
import { UserprefService } from 'app/services/userpref.service';
import { NotificationService } from 'app/services/notification.service';
import { Observable, Subscription } from 'rxjs';
import { RbNotification } from 'app/datamodel';
import { Evaluator } from 'app/helpers';
import { ActionService } from 'app/services/action.service';
import { RbButtonComponent } from 'app/clickable/rb-button/rb-button';
import { PopupService } from 'app/services/popup.service';
import { RbPopupActionsComponent } from 'app/popups/rb-popup-actions/rb-popup-actions.component';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { RbDataButtonComponent } from '../abstract/rb-databutton';


export class RbActiongroupAction {
  action: string;
  target: string;
  param: string;
  timeout: number;
  label: string;
  confirm: string;
  focus: boolean;

  constructor(a: string, t: string, p: string, to:number, l: string, c: string, f: boolean) {
    this.action = a;
    this.target = t;
    this.param = p;
    this.timeout = to;
    this.label = l;
    this.confirm = c;
    this.focus = f;
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
  @Input('domaincategory') domaincategory: string;
  @Input('showprocessinteraction') showprocessinteraction: boolean = false;
  @Input('round') round: boolean = false;
  @Input('hideonempty') hideonempty: boolean = false;
  
  domainActions: RbActiongroupAction[];
  actionData: RbActiongroupAction[] = [];
  notification: RbNotification;
  notificationRetreived: boolean = false;
  notificationSubscription: Subscription;
  
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
          this.domainActions.push(new RbActiongroupAction('executeglobal', item.name, null, item.timeout, item.description, item.confirm, false));
        }
        this.apiService.listDomainFunctions(category).subscribe(json => {
          for(var item of json.result) {
            this.domainActions.push(new RbActiongroupAction('executedomain', item.name, null, item.timeout, item.description, item.confirm, false));
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
      if(this.showprocessinteraction) {
        this.getNotificationThenCalcActions().subscribe(() => {});
      } else {
        this.calcActionData();
      }
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
        this.getNotificationThenCalcActions().subscribe(() => {
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
        this.actionData.push(new RbActiongroupAction("processaction", null, action.action, null, action.description, null, action.main));
      }
    }
    if(this.actions != null) {
      this.actions.forEach(item => {
        if(item.show == null || item.show == true || (typeof item.show == 'string' && (Evaluator.eval(item.show, this.rbObject, this.relatedObject) == true))) {
          let swtch = this.userpref.getCurrentViewUISwitch('action',  item.action + "_" + item.param);
          if(swtch == null || swtch == true) {
            this.actionData.push(new RbActiongroupAction(item.action, item.target, item.param, item.timeout, item.label, item.confirm, false));
          }
        }
      });
    }
    if(this.domainActions != null) {
      this.domainActions.forEach(item => {
        this.actionData.push(item);
      });
    }
  }

  private getNotificationThenCalcActions() : Observable<null> {
    const obs = new Observable<null>((observer) => {
      if(this.rbObject != null) {
        this.notificationService.getNotificationFor(this.rbObject.objectname, this.rbObject.uid).subscribe(notif => {
          this.notification = notif;
          this.notificationRetreived = true;
          this.calcActionData();
          observer.complete()
        });
      }
    });
    return obs;
  }

  public clickAction(action: RbActiongroupAction) {
    this.closePopup();
    if(action.action == 'processaction' && this.notification != null) {
      let notif = this.notification;
      this.running = true;
      this.notificationService.actionNotification(this.notification, action.param).subscribe(() => {
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
