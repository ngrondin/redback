import { Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { LogService } from './log.service';

@Injectable({
  providedIn: 'root'
})
export class ErrorService {

  constructor(
    private toastr: ToastrService,
    private logService: LogService
  ) { }

  receiveHttpError(error: any) {
    let msg: any = error;
    if(typeof msg == 'object') {
      while(msg != null && typeof msg == 'object') {
        msg = msg.error != null ? msg.error : msg.text != null ? msg.text: null;
      }
    }
    this.showError(typeof msg == 'string' ? msg : "No details");
  }

  showError(msg: string) {
      this.logService.error(msg);
      let parts = msg.split(" : ");
      this.toastr.error(parts[parts.length - 1], 'Error', {disableTimeOut: true});
  }
}
