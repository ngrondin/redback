import { Component, Input, OnInit } from '@angular/core';
import { ApiService } from 'app/services/api.service';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-actiongroup',
  templateUrl: './rb-actiongroup.component.html',
  styleUrls: ['./rb-actiongroup.component.css']
})
export class RbActiongroupComponent implements OnInit {
  @Input('dataset') dataset: RbDatasetComponent;
  @Input('actions') actions: any;
  @Input('domaincategory') domaincategory: string;
  @Input('round') round: boolean = false;

  message: string;
  loading: boolean;
  _domainActions: any;


  constructor(
    private apiService: ApiService,
    public userpref: UserprefService
  ) { }

  ngOnInit(): void {
    if(this.domaincategory != null && this.domaincategory != "") {
      this.apiService.listDomainFunctions(this.domaincategory).subscribe(json => {
        this._domainActions = [];
        json.result.forEach(item => {
          this._domainActions.push({
            action: 'executedomain',
            param: item.name,
            label: item.description,
            show: 'true'
          })
        });
      });
    }
  }

  public get actionData() {
    let ret = [];
    let object = this.dataset.selectedObject;
    if(this.actions != null) {
      this.actions.forEach(item => {
        if(item.show == null || item.show == true || (typeof item.show == 'string' && (item.show.indexOf('object.') == -1 || object != null) && eval(item.show))) {
          let swtch = this.userpref.getUISwitch('action',  item.action + "_" + item.param);
          if(swtch == null || swtch == true) {
            ret.push(item);
          }
        }
      });
    }
    if(this._domainActions != null) {
      this._domainActions.forEach(item => {
        ret.push(item);
      });
    }
    return ret;
  }

  public clickAction(action: any) {
    this.dataset.action(action.action, action.param);
  }
}
