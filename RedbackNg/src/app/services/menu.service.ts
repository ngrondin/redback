import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Observer } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  fullMenu: any;
  personalMenu: any;
  groupMenu: any;
  config: any;
  observers: Observer<any>[] = [];

  constructor(
    private apiService: ApiService,
    private http: HttpClient
  ) {
  }

  load(): Observable<null> {
    const obs = new Observable<null>((observer) => {
      let respCount = 0;
      let url = this.apiService.baseUrl + '/' + this.apiService.uiService + '/menu/default/any';
      this.http.get(url, { withCredentials: true, responseType: 'json' }).subscribe(
        resp => {
          this.fullMenu = resp;
          if(this.apiService.userprefService != null) {
            this.apiService.getUserPreference('user', 'menu').subscribe(
              resp => {
                this.personalMenu = resp;
                this.apiService.getUserPreference('role', 'menu').subscribe(
                  resp => {
                    this.groupMenu = resp
                    this.apiService.getUserPreference('user', 'defaultmenu').subscribe(
                      resp => {
                        this.config = resp;
                        observer.next();
                        observer.complete();
                        this.publish();
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
            )
          } else {
            observer.next();
            observer.complete();
          }
        },
        error => {
          observer.error(error);
        }
      );
     })
    return obs; 
  }

  public getObservable(): Observable<any> {
    return new Observable<any>((observer) => {
      this.observers.push(observer);
    })
  }

  private publish() {
    let curMenu = this.getCurrentMenu();
    this.observers.forEach(observer => observer.next({
      config: this.config,
      menu: curMenu
    }));
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
    this.publish();
  }

  removeFromMenu(menu: any, item: any) {
    if(menu != null && menu.content != null) {
      for(var i = 0; i < menu.content.length; i++) {
        if(menu.content[i] === item) {
          menu.content.splice(i, 1);
          this.publish();
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


  getCurrentMenu() : any {
    let menu: any = null;
    if(this.config.type == null) {
      if(this.personalMenu != null && this.personalMenu.content != null) {
        menu = this.personalMenu;
      } else if(this.groupMenu != null && this.groupMenu.content != null) {
        menu = this.groupMenu;
      } else {
        menu = this.fullMenu;
      }
    } else if(this.config.type == 'personal') {
      menu = this.getPersonalMenu();
    } else if(this.config.type == 'group') {
      menu = this.getGroupMenu();
    } else {
      menu = this.getFullMenu();
    }
    return menu;
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

  setMenu(type: string, mode: string) {
    this.config = {
      type: type,
      mode: mode
    }
    if(this.apiService.userprefService != null) {
      this.apiService.putUserPreference('user', 'defaultmenu', this.config).subscribe(resp => {});
      this.publish();
    }
  }

}
