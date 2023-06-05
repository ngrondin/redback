
import { Injectable, Injector } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { RbDialogComponent } from 'app/rb-dialog/rb-dialog.component';
import { Overlay } from 'ngx-toastr';

type DialogCallbackFunction = () => void;

export class DialogOption {
  label: string;
  callback: DialogCallbackFunction;
}

@Injectable({
  providedIn: 'root'
})
export class DialogService {
  tooltipElement: any;

  constructor(
    public injector: Injector,
    public overlay: Overlay,
    public dialog: MatDialog
  ) { }

  public openDialog(text: string, options: DialogOption[] ) {
    let dialogRef = this.dialog.open(RbDialogComponent, {
      data: {
        text: text,
        options: options
      },
      autoFocus: false,
      restoreFocus: false
    });
  }

  public showTooltip(text: string, related: any, position: string) {
    this.hideTooltip();
    let tgt = related;
    let top = 0;
    let left = 0;
    while(tgt != null) {
      top = top + tgt.offsetTop;
      left = left + tgt.offsetLeft;
      tgt = tgt.offsetParent;
    }
    let div = document.createElement("div");
    div.innerHTML = text;
    div.className = "rb-tip";
    if(position == "below") {
      div.style.top = (top + related.clientHeight) + "px";
      div.style.left = (left + 0) + "px";
      div.style["max-width"] = Math.max(150, related.clientWidth) + "px";
    } else if(position == "right") {
      div.style.top = (top ) + "px";
      div.style.left = (left + related.clientWidth) + "px";
    } 
    document.body.appendChild(div);
    this.tooltipElement = div;
  }

  public hideTooltip() {
    if(this.tooltipElement != null) {
      document.body.removeChild(this.tooltipElement);
      this.tooltipElement = null;
    }
  }

}
