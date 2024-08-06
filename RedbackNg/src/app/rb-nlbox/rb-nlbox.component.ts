import { Component, ElementRef, EventEmitter, Output, ViewChild } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { RbScrollComponent } from 'app/rb-scroll/rb-scroll.component';
import { ApiService } from 'app/services/api.service';
import { ConfigService } from 'app/services/config.service';
import { NavigateService } from 'app/services/navigate.service';
import { NlactionService } from 'app/services/nlaction.service';

@Component({
  selector: 'rb-nlbox',
  templateUrl: './rb-nlbox.component.html',
  styleUrls: ['./rb-nlbox.component.css']
})
export class RbNlboxComponent {
  @Output() close: EventEmitter<any> = new EventEmitter();

  @ViewChild('historyscroll') historyscroll: RbScrollComponent;

  history: any[] = []
  historyPointer = -1;
  currentText: string = "";
  waiting: boolean = false;
  minimized: boolean = false;

  constructor(
    private apiService: ApiService,
    private configService: ConfigService,
    private nlActionService: NlactionService,
    private navigateService: NavigateService
  ) {
  }

  get label(): string {
    return this.configService.nlCommandLabel ?? "Assistant";
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
    let context = {};
    let loadedView = this.navigateService.getCurrentLoadedView();
    let datasets = loadedView.getTopActiveDatasets();
    if(datasets.length > 0) {
      let contextDS = datasets[0];
      let contextObj: RbObject = contextDS != null ? contextDS.selectedObject : null;
      context = {
        objectname: contextDS.objectname,
        uid: contextObj != null ? contextObj.uid : null,
        filter: contextDS.resolvedFilter,
        search: contextDS.userSearch != null ? contextDS.userSearch : contextDS.dataTarget != null ? contextDS.dataTarget.search : null,
        sort: contextDS.baseSort
      }
    }
    this.history.push({text:this.currentText.trim(), assistant:false});
    this.historyPointer = -1; 
    this.waiting = true; 
    let lastCommand = this.currentText;
    this.apiService.nlCommand(this.configService.nlCommandModel, this.currentText, context).subscribe({
      next: (resp) => {
        let text = resp.text != null && resp.text != "" ? resp.text : "...";
        if(resp.text != null) {
          this.history.push({text:text, assistant:true, command: lastCommand, sequence: resp.sequence});
        }
        if(resp.uiactions != null) {
          try {
            this.nlActionService.processSequence(resp.uiactions);
          } catch(err) {
            console.error(err);
          }
        }
        this.waiting = false;
      },
      error: (err) => {
        this.history.push({text:"Something went wrong", assistant:true});
        console.error(err);
        this.waiting = false;
      },
      complete: () => {
        this.waiting = false;
      }
    });
    setTimeout(() => {
      this.currentText = "";
      this.scrollToBottom();       
    }, 10);
  }

  clickClose() {
    this.close.emit();
  }

  clickMinimize() {
    this.minimized = !this.minimized;
  }

  feedback(item: any, points: number) {
    this.apiService.nlFeedback(this.configService.nlCommandModel, item.command, item.sequence, points).subscribe({
      complete: () => {
        console.log("Feedback given");
      }
    });
  }

  toggleShowActions(item: any) {
    item.showsequence = !(item.showsequence ?? false);
  }

  private scrollToBottom() {
    this.historyscroll.scrollToBottom();
  }

}
