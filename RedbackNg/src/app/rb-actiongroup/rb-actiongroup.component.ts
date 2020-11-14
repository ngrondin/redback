import { Component, Input, OnInit } from '@angular/core';
import { ApiService } from 'app/api.service';
import { RbDatasetDirective } from 'app/rb-dataset/rb-dataset.directive';

@Component({
  selector: 'rb-actiongroup',
  templateUrl: './rb-actiongroup.component.html',
  styleUrls: ['./rb-actiongroup.component.css']
})
export class RbActiongroupComponent implements OnInit {
  @Input('dataset') dataset: RbDatasetDirective;
  @Input('actions') _actions: any;
  @Input('domaincategory') domainCategory: string;
  @Input('round') round: boolean = false;

  message: string;
  loading: boolean;
  _domainActions: any;


  constructor(
    private apiService: ApiService
  ) { }

  ngOnInit(): void {
    if(this.domainCategory != null && this.domainCategory != "") {
      this.apiService.listDomainFunctions(this.domainCategory).subscribe(json => {
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

  public get actions() {
    let ret = [];
    let object = this.dataset.selectedObject;
    if(object != null) {
      if(this._actions != null) {
        this._actions.forEach(item => {
          if(eval(item.show)) {
            ret.push(item);
          }
        });
      }
      if(this._domainActions != null) {
        this._domainActions.forEach(item => {
          ret.push(item);
        });
      }
    }
    return ret;
  }

  public clickAction(action: any) {
    this.dataset.action(action.action, action.param);
  }
}
