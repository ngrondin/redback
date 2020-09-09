import { Component, OnInit, Input } from '@angular/core';
import { ApiService } from 'app/api.service';
import { RbObject } from 'app/datamodel';
import { DataService } from 'app/data.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'rb-processactions',
  templateUrl: './rb-processactions.component.html',
  styleUrls: ['./rb-processactions.component.css']
})
export class RbProcessactionsComponent implements OnInit {

  @Input('object') rbObject: RbObject;
  @Input('round') round: boolean = false;

  pid: string;
  actions: any;
  message: string;
  loading: boolean;
  
  constructor(
    private apiService: ApiService,
    private dataService: DataService,
    private toastr: ToastrService
  ) { }

  ngOnInit() {
    this.setAssignment(null);
  }

  activate() {
    if(this.rbObject != null) {
      let filter: any = {
        "data.uid": this.rbObject.uid
      };
      this.loading = true;
      this.pid = null;
      this.message = null;
      this.actions = [];
      this.apiService.listAssignments(null, filter).subscribe(resp => this.setAssignment(resp));
    }
  }

  setAssignment(data: any) {
    if(data != null && data.result != null && data.result.length > 0) {
      this.pid = data.result[0].pid;
      this.actions = data.result[0].actions;
      this.message = data.result[0].message;
    } else {
      this.pid = null;
      this.message = "No actions available";
      this.actions = [];
    }
    this.loading = false;
  }

  clickAction(action: string) {
    this.apiService.actionAssignment(this.pid, action).subscribe(
      resp => {
        this.receiveActionResponse(resp)
      },
      error => {
        this.toastr.error(error.headers.status, error.error.error, {disableTimeOut: true});
      }
    );
  }

  receiveActionResponse(resp: any) {
    if(resp != null && resp.rbobjectupdate != null && resp.rbobjectupdate.length > 0) {
      for(let row of resp.rbobjectupdate) {
        this.dataService.getServerObject(row.objectname, row.uid).subscribe(resp => {});
      }
    }
  }
}
