import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class NlactionService {
  navigateTo: Function = null;

  constructor() { }

  public processSequence(tokens: string[]) {
    let cur = 0;
    for(cur = 0; cur < tokens.length; cur++) {
      let curToken = tokens[cur];
      if(curToken.startsWith("$")) {
        let params = [];
        for(let i = cur + 1; i < tokens.length && !tokens[i].startsWith("$"); i++) {
          params.push(tokens[i]);
        }
        this.runCommand(curToken.substring(1), params);
        cur += params.length;
      }
    }
  }

  runCommand(command: string, params: string[]) {
    if(command == 'navtouid' && params.length == 2) {
      this.navigateTo({
        view: params[0],
        filter: {uid: "'" + params[1] + "'"},
        reset: true
      });
    } else if(command == 'navtouids' && params.length == 2) {
      this.navigateTo({
        view: params[0],
        filter: {
          uid: {
            $in: params[1].split(',').map(a => "'" + a + "'")
          }
        }, 
        reset: true
      });
    } else if(command == 'navtosearch' && params.length == 2) {
      this.navigateTo({
        view: params[0],
        filter: {},
        search: params[1],
        reset: true
      })
    } else if(command == 'navto' && params.length == 1) {
      this.navigateTo({
        view: params[0],
        filter: {},
        reset: true
      })
    }
  }
}
