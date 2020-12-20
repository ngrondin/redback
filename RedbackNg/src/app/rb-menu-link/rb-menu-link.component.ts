import { EventEmitter, Input, Output } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { MenuService } from 'app/menu.service';

@Component({
  selector: 'rb-menu-link',
  templateUrl: './rb-menu-link.component.html',
  styleUrls: ['./rb-menu-link.component.css']
})
export class RbMenuLinkComponent implements OnInit {
  @Input('config') config: any;
  @Input('mode') mode: any;
  @Output('navigate') navigate: EventEmitter<any> = new EventEmitter();

  cmTop: number = 100;
  cmLeft: number = 100;
  cmShow: boolean = false;

  constructor(
    private menuService: MenuService
  ) { }

  ngOnInit(): void {
  }

  getTooltip() : string {
    return this.mode != 'large' ? this.config.label : null;
  }

  click() {
    this.navigate.emit({view:this.config.view, filter:{}, reset:true});
  }

  rightclick(event: any) {
    this.cmTop = event.clientY;
    this.cmLeft = event.clientX;
    this.cmShow = true;
  }

  closePopup() {
    this.cmShow = false;
  }

  isInPersonalMenu() {
    return this.menuService.isInMenu('personal', this.config.name);
  }

  addToPersonalMenu() {
    this.menuService.addToPersonalMenu(this.config);
  }

  removeFromPersonalMenu() {
    this.menuService.removeFromPersonalMenu(this.config);
  }

  isInGroupMenu() {
    return this.menuService.isInMenu('group', this.config.name);
  }

  addToGroupMenu() {
    this.menuService.addToGroupMenu(this.config);
  }

  removeFromGroupMenu() {
    this.menuService.removeFromGroupMenu(this.config);
  }

}
