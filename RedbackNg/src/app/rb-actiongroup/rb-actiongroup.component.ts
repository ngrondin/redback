import { Component, Input, OnInit } from '@angular/core';
import { ApiService } from 'app/services/api.service';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { UserprefService } from 'app/services/userpref.service';
import { NotificationService } from 'app/services/notification.service';
import { Subscription } from 'rxjs';
import { RbNotification, RbObject } from 'app/datamodel';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { Evaluator } from 'app/helpers';

export class RbActiongroupAction {
  action: string;
  param: string;
  label: string;
  focus: boolean;

  constructor(a: string, p: string, l: string, f: boolean) {
    this.action = a;
    this.param = p;
    this.label = l;
    this.focus = f;
  }
}

@Component({
  selector: 'rb-actiongroup',
  templateUrl: './rb-actiongroup.component.html',
  styleUrls: ['./rb-actiongroup.component.css']
})
export class RbActiongroupComponent extends RbDataObserverComponent {
  //@Input('dataset') dataset: RbDatasetComponent;
  @Input('actions') actions: any;
  @Input('domaincategory') domaincategory: string;
  @Input('showprocessinteraction') showprocessinteraction: boolean = false;
  @Input('round') round: boolean = false;
  @Input('hideonempty') hideonempty: boolean = false;

  open: boolean = false;
  actionning: boolean = false;
  domainActions: RbActiongroupAction[];
  notification: RbNotification;
  notificationRetreived: boolean = false;
  notificationSubscription: Subscription;
  actionData: RbActiongroupAction[] = [];

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService,
    public userpref: UserprefService
  ) {
    super();
  }
  

  dataObserverInit() {
    if(this.showprocessinteraction) {   
      this.notificationSubscription = this.notificationService.getObservable().subscribe(event => this.onNotificationEvent(event));
    }
    if(this.domaincategory != null && this.domaincategory != "") {
      this.apiService.listDomainFunctions(this.domaincategory).subscribe(json => {
        this.domainActions = [];
        json.result.forEach(item => {
          this.domainActions.push(new RbActiongroupAction('executedomain', item.name,item.description, false));
        });
        this.calcActionData();
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
        this.getNotificationThenCalcActions();
      } else {
        this.calcActionData();
      }
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

  get focus() : boolean {
    let ret: boolean = false;
    if(this.notification != null) {
      for(let action of this.notification.actions) {
        if(action.main == true) {
          ret = true;
        }
      }
    }
    return ret;
  }

  public activate() {
    this.open = true;
    if(this.showprocessinteraction && this.notificationRetreived == false) {
      this.getNotificationThenCalcActions();
    }
  }

  public deactivate() {
    this.open = false;
  }
  
  private calcActionData() {
    this.actionData = [];
    let object = this.rbObject;
    let relatedObject = this.dataset != null ? this.dataset.relatedObject : null;
    if(this.showprocessinteraction && this.notification != null) {
      for(var action of this.notification.actions) {
        this.actionData.push(new RbActiongroupAction("processaction", action.action, action.description, action.main))
      }
    }
    if(this.actions != null) {
      this.actions.forEach(item => {
        if(item.show == null || item.show == true || (typeof item.show == 'string' && (Evaluator.eval(item.show, this.rbObject, this.relatedObject) == true))) {
          let swtch = this.userpref.getUISwitch('action',  item.action + "_" + item.param);
          if(swtch == null || swtch == true) {
            this.actionData.push(new RbActiongroupAction(item.action, item.param, item.label, false));
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

  private getNotificationThenCalcActions() {
    this.notificationService.getNotificationFor(this.rbObject.objectname, this.rbObject.uid).subscribe(notif => {
      this.notification = notif;
      this.notificationRetreived = true;
      this.calcActionData();
    });
  }

  public clickAction(action: RbActiongroupAction) {
    if(action.action == 'processaction' && this.notification != null) {
      let notif = this.notification;
      this.actionning = true;
      this.notificationService.actionNotification(this.notification, action.param).subscribe(() => {
          if(this.notification === notif) {
            this.notification = null;
            this.notificationRetreived = false;
          }
        }).add(() => {
          this.actionning = false;
          this.calcActionData();
        });
    } else {
      this.actionning = true;
      this.dataset.action(action.action, action.param).subscribe().add(() => {
        this.actionning = false;
      });
    }
  }
}
