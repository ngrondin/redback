import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { ApiService } from 'app/services/api.service';
import { Http } from '@angular/http';
import { MenuService } from 'app/services/menu.service';

@Component({
  selector: 'rb-menu',
  templateUrl: './rb-menu.component.html',
  styleUrls: ['./rb-menu.component.css']
})
export class RbMenuComponent implements OnInit {
  @Output('navigate') navigate: EventEmitter<any> = new EventEmitter();
  _type: string;
  _mode: string;
  content: any;

  constructor(
    private menuService: MenuService
  ) { }

  ngOnInit(): void {
    this.menuService.getStartingMenu().subscribe(resp => {
      this.content = resp.menu.content;
      this._type = resp.type;
      this._mode = resp.mode;
      this.sendResize();
    });
  }

  public get mode(): string {
    return this._mode;
  }

  public toggleMenuMode() {
    if(this._mode == 'small') {
      this._mode = 'large'
    } else {
      this._mode = 'small'
    }
    this.menuService.setDefaultMenu(this._type, this.mode);
    this.sendResize();
  }
  
  navigateTo(event: any) {
    this.navigate.emit(event);
  }

  setMenu(type: string) {
    this._type = type;
    this.content = this.menuService.getMenu(this._type).content;
    this.menuService.setDefaultMenu(this._type, this.mode);
    this.sendResize();
  }

  private sendResize() {
    setTimeout(_ => {
      window.dispatchEvent(new Event('resize'));
    });   
  }

}
