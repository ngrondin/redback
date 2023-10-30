import { Component, ElementRef, EventEmitter, Output, ViewChild } from '@angular/core';
import { ApiService } from 'app/services/api.service';
import { ConfigService } from 'app/services/config.service';

@Component({
  selector: 'rb-nlbox',
  templateUrl: './rb-nlbox.component.html',
  styleUrls: ['./rb-nlbox.component.css']
})
export class RbNlboxComponent {
  @Output() close: EventEmitter<any> = new EventEmitter();
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  @ViewChild('historyscroll') historyscroll: ElementRef;

  history: any[] = []
  currentText: string = "";

  constructor(
    private apiService: ApiService,
    private configService: ConfigService
  ) {
  }

  onKeyDown(event) {
    if(event.keyCode == 13) {
      this.post()
    }
  }

  post() {
    this.apiService.nlCommand(this.configService.nlCommandModel, this.currentText).subscribe((resp) => {
      this.history.push({text:this.currentText});
      this.currentText = "";
      this.scrollToBottom(); 
      if(resp.text != null) {
        this.history.push({text:resp.text});
      }
      if(resp.actions != null) {
        this.processActions(resp.actions);
      }
    });
 
  }

  onClose() {
    this.close.emit();
  }

  processActions(actions: string[]) {
    if(actions[0] == 'navto' && actions.length >= 3) {
      this.navigate.emit({
        view: actions[1],
        filter: {},
        search: actions[2]
      });  
    }
  }

  private scrollToBottom() {
    setTimeout(() => {
      if(this.historyscroll != null) {
        this.historyscroll.nativeElement.scrollTop = this.historyscroll.nativeElement.scrollHeight;
      }
    }, 100);
  }

}
