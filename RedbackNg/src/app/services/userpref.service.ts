import { Injectable, ViewChild } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class UserprefService {

  selectedUIAlt: string = 'base';
  uiAlternates: string[] = ['base', 'alt1', 'alt2'];
  currentView: string;
  domainUISwitches: any = {};
  roleUISwitches: any = {};
  userUISwitches: any = {};

  constructor(
    private apiService: ApiService
  ) { 

  }

  public load() {
    return new Observable<null>((observer) => {
      if(this.apiService.userprefService != null) {
        this.apiService.getUserPreference('user', 'uialt').subscribe(
          resp => {
            if(resp.name != null && this.uiAlternates.indexOf(resp.name) > -1) {
              this.selectedUIAlt = resp.name;
              this.apiService.getUserPreference('domain', 'uiswitch').subscribe(
                resp => {
                  this.domainUISwitches = resp;
                  this.apiService.getUserPreference('role', 'uiswitch').subscribe(
                    resp => {
                      this.roleUISwitches = resp;
                      this.apiService.getUserPreference('user', 'uiswitch').subscribe(
                        resp => {
                          this.userUISwitches = resp;
                          observer.next();
                          observer.complete();
                        },
                        error => {
                          observer.error(error);
                        }
                      );
                    },
                    error => {
                      observer.error(error);
                    }
                  );
                },
                error => {
                  observer.error(error);
                }
              );
            }
          },
          error => {
            observer.error(error);
          }
        );
      } else {
        observer.next();
        observer.complete();
      }
    });

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

  public setCurrentView(view: string) {
    this.currentView = view;
  }

  public getUISwitch(comp: string, name: string) : Boolean {
    let val = this.getUISwitchValue(this.userUISwitches, comp, name);
    if(val != null) {
      return val;
    } else {
      val = this.getUISwitchValue(this.roleUISwitches, comp, name);
      if(val != null) {
        return val;
      } else {
        val = this.getUISwitchValue(this.domainUISwitches, comp, name);
        if(val != null) {
          return val;
        } else {
          return null;
        }
      }
    }
  }

  private getUISwitchValue(cfg: any, comp: string, name: string) : any {
    if(cfg[this.currentView] != null) {
      if(cfg[this.currentView][comp] != null) {
        if(cfg[this.currentView][comp][name] != null) {
          return cfg[this.currentView][comp][name];
        }
      } 
    }
    return null;
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
      map[this.currentView][comp][name] = val;
      if(this.apiService.userprefService != null) {
        this.apiService.putUserPreference(level, 'uiswitch', map).subscribe(resp => {});
      }      
    }
    
  }

 }
