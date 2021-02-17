import { Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})
export class ErrorService {

  constructor(
    private toastr: ToastrService
  ) { }

  receiveHttpError(httpError: any) {
    let msg: string = httpError.error.error;
    if(msg == null) {
      msg = "No details";
    }
    let parts = msg.split(" : ");
    this.toastr.error(parts[parts.length - 1], 'Error', {disableTimeOut: true});
    console.error(msg);
  }
}
