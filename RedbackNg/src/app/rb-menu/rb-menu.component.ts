import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { MenuService } from 'app/services/menu.service';
import { Subscription } from 'rxjs';

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
  subscription: Subscription;

  constructor(
    private menuService: MenuService
  ) { }

  ngOnInit(): void {
    this.subscription = this.menuService.getObservable().subscribe(state => {
      this.content = state.menu.content;
      this._type = state.config.type;
      this._mode = state.config.mode;
      this.sendResize();
    });
  }

  ngOnDestroy(): void {
    if(this.subscription != null) {
      this.subscription.unsubscribe();
    }
}

  public get mode(): string {
    return this._mode;
  }

  public toggleMenuMode() {
    let newMode = this._mode == 'small' ? 'large' : 'small';
    this.menuService.setMenu(this._type, newMode);
  }
  
  setMenu(newType: string) {
    this.menuService.setMenu(newType, this._mode);
  }

  private sendResize() {
    setTimeout(_ => {
      window.dispatchEvent(new Event('resize'));
    });   
  }

  navigateTo(event: any) {
    this.navigate.emit(event);
  }
}
