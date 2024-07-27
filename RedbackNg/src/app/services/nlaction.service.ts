import { Injectable } from '@angular/core';
import { NavigateService } from './navigate.service';
import { ActionService } from './action.service';

@Injectable({
  providedIn: 'root'
})
export class NlactionService {

  constructor(
    private navigateService: NavigateService,
    private actionSevice: ActionService
  ) { }

  public async processSequence(tokens: string[])  {
    let cur = 0;
    for(cur = 0; cur < tokens.length; cur++) {
      let curToken = tokens[cur];
      if(curToken.startsWith("$")) {
        let params = [];
        for(let i = cur + 1; i < tokens.length && !tokens[i].startsWith("$"); i++) {
          params.push(tokens[i]);
        }
        await this.runCommand(curToken.substring(1), params);
        cur += params.length;
        await this.pause();
      }
    }
  }

  async runCommand(command: string, params: string[]) {
    if(command == 'navtouid' && params.length == 2) {
      await this.navigateService.navigateTo({
        view: params[0],
        filter: {uid: "'" + params[1] + "'"},
        reset: true
      });
    } else if(command == 'navtouids' && params.length == 2) {
      await this.navigateService.navigateTo({
        view: params[0],
        filter: {
          uid: {
            $in: params[1].split(',').map(a => "'" + a + "'")
          }
        }, 
        reset: true
      });
    } else if(command == 'navtosearch' && params.length == 2) {
      await this.navigateService.navigateTo({
        view: params[0],
        search: params[1],
        reset: true
      })
    } else if(command == 'navtofilter' && params.length == 2) {
      await this.navigateService.navigateTo({
        view: params[0],
        filter: JSON.parse(params[1]),
        reset: true
      })      
    } else if(command == 'navtocontext' && params.length == 2) {
      await this.navigateService.navigateTo({
        view: params[0],
        objectuid: params[1],
        reset: true
      })      
    } else if(command == 'navto' && params.length == 1) {
      await this.navigateService.navigateTo({
        view: params[0],
        filter: {},
        reset: true
      })
    } else if(command == 'opentab' && params.length >= 1) {
      await this.navigateService.navigateTo({
        tab: params.join(" ")
      })
    } else if(command == 'launchreport' && params.length == 1) {

    }
  }

  pause(): Promise<void> {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        resolve();
      }, 100);
    })
  }
}
