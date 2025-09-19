import { Directive, HostListener, Input } from '@angular/core';
import { DialogService } from 'app/services/dialog.service';

@Directive({
  selector: '[tip]'
})
export class RbTipDirective {
  @Input('tip') tip: any;

  mouseIsOver: boolean = false;

  constructor(
    private dialogService: DialogService
  ) { }

  @HostListener('mouseenter', ['$event']) onEnter(event: any) {
    this.mouseIsOver = true;
    if(this.tip != null) {
      setTimeout(() => {
        if(this.mouseIsOver == true) {
          this.dialogService.showTooltip(this.tip, event.target, "below");
        }
      }, 1000);  
    }
  }

  @HostListener('mouseleave', ['$event']) onLeave(event: any) {
    this.mouseIsOver = false;
    this.dialogService.hideTooltip();
  }

}
