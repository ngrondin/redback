import { Component, ElementRef, EventEmitter, Output, ViewChild } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
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
  historyPointer = -1;
  currentText: string = "";
  waiting: boolean = false;

  constructor(
    private apiService: ApiService,
    private configService: ConfigService
  ) {
  }

  onKeyDown(event: any) {
    if(event.keyCode == 13) {
      this.post();
    } else if(event.keyCode == 38) {
      if(this.historyPointer == -1) {
        this.historyPointer = this.history.length - 1;
      } else {
        this.historyPointer--;
      }
      while(this.historyPointer > -1 && this.history[this.historyPointer].assistant == true) {
        this.historyPointer--;
      }
      if(this.historyPointer > -1) {
        this.currentText = this.history[this.historyPointer].text;
      }
    } else if(event.keyCode == 40) {
      if(this.historyPointer == this.history.length - 1) {
        this.historyPointer = 0;
      } else {
        this.historyPointer++;
      }
      while(this.historyPointer < this.history.length && this.history[this.historyPointer].assistant == true) {
        this.historyPointer++;
      }
      if(this.historyPointer > -1) {
        this.currentText = this.history[this.historyPointer].text;
      }
    }
  }

  post() {
    let contextDS: RbDatasetComponent = window.redback?.currentLoadedView?.topSets[0];
    let contextObj: RbObject = contextDS != null ? contextDS.selectedObject : null;
    let context = {
      objectname: contextDS != null ? contextDS.objectname : null,
      uid: contextObj != null ? contextObj.uid : null,
      filter: contextDS != null ? contextDS.resolvedFilter : null,
      search: contextDS != null ? contextDS.searchString : null
    }
    this.apiService.nlCommand(this.configService.nlCommandModel, this.currentText, context).subscribe(
      (resp) => {
        let text = resp.text != null && resp.text != "" ? resp.text : "...";
        if(resp.text != null) {
          this.history.push({text:text, assistant:true});
        }
        if(resp.actions != null) {
          this.processActions(resp.actions);
        }
        this.waiting = false;
      },
      (err) => {
        this.history.push({text:"Something went wrong", assistant:true});
        this.waiting = false;
      }
    );
    setTimeout(() => {
      this.history.push({text:this.currentText.trim(), assistant:false});
      this.currentText = "";
      this.scrollToBottom(); 
      this.waiting = true; 
      this.historyPointer = -1; 
    }, 10);
  }

  onClose() {
    this.close.emit();
  }

  processActions(actions: string[]) {
    if(actions[0] == 'navtosearch' && actions.length >= 3) {
      this.navigate.emit({
        view: actions[1],
        filter: {},
        search: actions[2],
        reset: true
      });  
    } else if(actions[0] == 'navtouid' && actions.length >= 3) {
      this.navigate.emit({
        view: actions[1],
        filter: {uid: "'" + actions[2] + "'"},
        reset: true
      });  
    }
  }

  private scrollToBottom() {
    setTimeout(() => {
      if(this.historyscroll != null) {
        this.historyscroll.nativeElement.scrollTop = this.historyscroll.nativeElement.scrollHeight;
      }
    }, 10);
  }

}
