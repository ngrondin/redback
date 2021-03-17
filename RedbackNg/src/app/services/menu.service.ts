import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Observable } from 'rxjs';
import { Observer } from 'rxjs/internal/types';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  fullMenu: any;
  personalMenu: any;
  groupMenu: any;

  constructor(
    private apiService: ApiService,
    private http: Http
  ) {
  }

  getStartingMenu(): Observable<any> {
    const obs = new Observable((observer) => {
      let respCount = 0;
      let url = this.apiService.baseUrl + '/' + this.apiService.uiService + '/menu/default/any';
      this.http.get(url, { withCredentials: true, responseType: 0 }).subscribe(
        resp => {
          this.fullMenu = resp.json();
          if(this.apiService.userprefService != null) {
            this.apiService.getUserPreference('user', 'menu').subscribe(
              resp => {
                this.personalMenu = resp;
                this.apiService.getUserPreference('role', 'menu').subscribe(
                  resp => {
                    this.groupMenu = resp
                    this.apiService.getUserPreference('user', 'defaultmenu').subscribe(
                      resp => {
                        this.pushStartingMenu(observer, resp != null ? resp : {});
                      },
                      error => {
                        this.pushStartingMenu(observer, {});
                      }
                    );
                  },
                  error => {
                    this.pushStartingMenu(observer, {});
                  }
                );
              },
              error => {
                this.pushStartingMenu(observer, {});
              }
            )
          } else {
            this.pushStartingMenu(observer, {});
          }
        },
        error => {
          this.pushStartingMenu(observer, {});
        }
      );
     })
    return obs; 
  }

  private pushStartingMenu(observer: Observer<any>, defaultConfig: any) {
    let startingMenu = null;
    let startingType = null;
    if(defaultConfig.type == null) {
      if(this.personalMenu != null && this.personalMenu.content != null) {
        startingMenu = this.personalMenu;
        startingType = 'personal';
      } else if(this.groupMenu != null && this.groupMenu.content != null) {
        startingMenu = this.groupMenu;
        startingType = 'group';
      } else {
        startingMenu = this.fullMenu;
        startingType = 'full';
      }
    } else if(defaultConfig.type == 'personal' && this.personalMenu != null && this.personalMenu.content != null) {
      startingMenu = this.personalMenu;
      startingType = 'personal';
    } else if(defaultConfig.type == 'group' && this.groupMenu != null && this.groupMenu.content != null) {
      startingMenu = this.groupMenu;
      startingType = 'group';
    } else {
      startingMenu = this.fullMenu;
      startingType = 'full';
    }
    observer.next({
      menu: startingMenu,
      type: startingType, 
      mode: defaultConfig.mode != null ? defaultConfig.mode : 'large'
    });
    observer.complete();
  }

  addToMenu(menu: any, item: any) {
    let exists = false;
    if(menu.content == null) {
      menu.content = [];
    }
    for(var i = 0; i < menu.content.length; i++) {
      if(menu.content[i].name == item.name) {
        exists = true;
      }
    }
    if(!exists) {
      menu.content.push(item);
    }
  }

  removeFromMenu(menu: any, item: any) {
    if(menu != null && menu.content != null) {
      for(var i = 0; i < menu.content.length; i++) {
        if(menu.content[i] === item) {
          menu.content.splice(i, 1);
          return;
        }
      }
    }
  }

  addToPersonalMenu(item: any) {
    if(this.personalMenu == null) {
      this.personalMenu = {};
    }
    this.addToMenu(this.personalMenu, item);
    if(this.apiService.userprefService != null) {
      this.apiService.putUserPreference('user', 'menu', this.personalMenu).subscribe(resp => {});
    }
  }

  removeFromPersonalMenu(item: any) {
    this.removeFromMenu(this.personalMenu, item);
    if(this.apiService.userprefService != null) {
      this.apiService.putUserPreference('user', 'menu', this.personalMenu).subscribe(resp => {});
    }
  }

  addToGroupMenu(item: any) {
    if(this.groupMenu == null) {
      this.groupMenu = {};
    }
    this.addToMenu(this.groupMenu, item);
    if(this.apiService.userprefService != null) {
      this.apiService.putUserPreference('role', 'menu', this.groupMenu).subscribe(resp => {});
    }
  }

  removeFromGroupMenu(item: any) {
    this.removeFromMenu(this.groupMenu, item);
    if(this.apiService.userprefService != null) {
      this.apiService.putUserPreference('role', 'menu', this.groupMenu).subscribe(resp => {});
    }
  }

  getPersonalMenu(): any {
    if(this.personalMenu == null) {
      return {
        content:[]
      }
    } else {
      return this.personalMenu;
    }
  }

  getGroupMenu(): any {
    if(this.groupMenu == null) {
      return {
        content:[]
      }
    } else {
      return this.groupMenu;
    }
  }

  getFullMenu(): any {
    if(this.fullMenu == null) {
      return {
        content:[]
      }
    } else {
      return this.fullMenu;
    }
  }

  getMenu(type: string): any {
    if(type == 'full') {
      return this.getFullMenu();
    } else if(type == 'group') {
      return this.getGroupMenu();
    } else if(type = 'user') {
      return this.getPersonalMenu();
    }
  }

  isInMenu(type: string, name: string): boolean {
    let menu = this.getMenu(type);
    let ret: boolean = false;
    if(menu != null && menu.content != null) {
      for(var i = 0; i < menu.content.length; i++) {
        if(menu.content[i].name == name) {
          ret = true;
        }
      }
    }
    return ret;
  }

  setDefaultMenu(type: string, mode: string) {
    if(this.apiService.userprefService != null) {
      this.apiService.putUserPreference('user', 'defaultmenu', {
        type: type,
        mode: mode
      }).subscribe(resp => {});
    }
  }

}
