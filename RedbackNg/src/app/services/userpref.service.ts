import { Injectable, ViewChild } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { defaultGlobalPrefs } from './userpref.service.default';
import { Formatter } from 'app/helpers';

export class PrefOption {
  value: string;
  label: string;

  constructor(json: any) {
    this.value = json.value;
    this.label = json.label;
  }
}

export class GlobalPref {
  code: string;
  label: string;
  icon: string;
  options: PrefOption[];

  constructor(json: any) {
    this.code = json.code;
    this.label = json.label;
    this.icon = json.icon;
    this.options = [];
    if(json.options != null) {
      this.options = json.options.map(element => new PrefOption(element));
    }
  }
}

@Injectable({
  providedIn: 'root'
})
export class UserprefService {
  userdisplay: string;
  username: string;
  globalPrefs: GlobalPref[] = [];
  currentView: string;
  domainUISwitches: any = {};
  roleUISwitches: any = {};
  userUISwitches: any = {};

  constructor(
    private apiService: ApiService
  ) { 
    for(const entry of defaultGlobalPrefs) {
      this.globalPrefs.push(new GlobalPref(entry));
    };
    Formatter.userPrefService = this;
  }

  public load(): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      if(this.apiService.userprefService != null) {
        this.apiService.getUserPreference('domain', 'uiswitch').subscribe({
          next: (resp) => {
            this.domainUISwitches = resp;
            this.apiService.getUserPreference('role', 'uiswitch').subscribe({
              next: (resp) => {
                this.roleUISwitches = resp;
                this.apiService.getUserPreference('user', 'uiswitch').subscribe({
                  next: (resp) => {
                    this.userUISwitches = resp;
                    resolve();
                  },
                  error: error => {
                    reject(error);
                  }
                });
              },
              error: error => {
                reject(error);
              }
            });
          },
          error: error => {
            reject(error);
          }
        });         
      } else {
        resolve();
      }
    });
  }

  public addGlobalPreference(json: any) {
    this.globalPrefs.push(json);
  }

  public getGlobalPreferences() : GlobalPref[] {
    return this.globalPrefs;
  }

  public getInitialView() : any {
    return this.getGlobalPreferenceValue("initialview");
  }

  public getGlobalPreferenceValue(name: string) : any {
    var stack = [this.userUISwitches, this.roleUISwitches, this.domainUISwitches];
    for(let i = 0; i < stack.length; i++) {
      if(stack[i]["_general"] != null && stack[i]["_general"][name] != null) {
        return stack[i]["_general"][name];
      }
    }
    return null;
  }

  public setGlobalPreferenceValue(level: string, name: string, val: any) {
    let map = level == 'domain' ? this.domainUISwitches : level == 'role' ? this.roleUISwitches : level == 'user' ? this.userUISwitches : null;
    if(map != null) {
      if(map["_general"] == null) {
        map["_general"] = {};
      }
      map["_general"][name] = val;
      if(this.apiService.userprefService != null) {
        this.apiService.putUserPreference(level, 'uiswitch', map).subscribe(resp => {});
      }      
    }
  }

  public setCurrentView(view: string) {
    this.currentView = view;
  }

  public getCurrentViewUISwitch(cat: string, name: string) : any {
    return this.getUISwitch(this.currentView, cat, name);
  }

  private getUISwitch(view: string, cat: string, name: string) : any {
    var stack = [this.domainUISwitches, this.roleUISwitches, this.userUISwitches];
    var val = null;
    for(let i = 0; i < stack.length; i++) {
      if(stack[i][view] != null && stack[i][view][cat] != null && stack[i][view][cat][name] != null) {
        var readValue = stack[i][view][cat][name];
        val = (typeof val == 'object' && typeof readValue == 'object' ? {...val, ... readValue} : readValue);
      }
    }
    return val;
  }

  public setUISwitch(level: string, comp: string, name: string, val: any) {
    let map = level == 'domain' ? this.domainUISwitches : level == 'role' ? this.roleUISwitches : level == 'user' ? this.userUISwitches : null;
    if(map != null) {
      if(map[this.currentView] == null) {
        map[this.currentView] = {};
      }
      if(map[this.currentView][comp] == null) {
        map[this.currentView][comp] = {};
      }
      var currentValue = map[this.currentView][comp][name];
      map[this.currentView][comp][name] = (typeof currentValue == 'object' && typeof val == 'object' ? {... currentValue, ...val} : val);
      if(this.apiService.userprefService != null) {
        this.apiService.putUserPreference(level, 'uiswitch', map).subscribe(resp => {});
      }      
    } 
  }

 }
