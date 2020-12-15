import { Injectable } from '@angular/core';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class UserprefService {

  selectedUIAlt: string = 'base';
  uiAlternates: string[] = ['base', 'alt1'];

  constructor(
    private apiService: ApiService
  ) { 

  }

  public load() {
    if(this.apiService.userprefService != null) {
      this.apiService.getUserPreference('user', 'uialt').subscribe(resp => {
        if(resp.name != null && this.uiAlternates.indexOf(resp.name) > -1) {
          this.selectedUIAlt = resp.name;
        }
      });
    }
  }

  public get selecteduialt() : string {
    return this.selectedUIAlt;
  }

  public set selecteduialt(s: string) {
    this.selectedUIAlt = s;
    if(this.apiService.userprefService != null) {
      this.apiService.putUserPreference('user', 'uialt', {
        name: this.selectedUIAlt
      }).subscribe(resp => {});
    }
  }

  public getAtlClassFor(c: string) : string {
    if(this.selectedUIAlt == 'base') {
      return c;
    } else {
      return c + "-" + this.selectedUIAlt;
    }
  }
 }
