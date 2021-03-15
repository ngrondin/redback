import { TOUCH_BUFFER_MS } from '@angular/cdk/a11y';
import { SimpleChanges } from '@angular/core';
import { EventEmitter, HostBinding, Output } from '@angular/core';
import { Component, Input, OnInit } from '@angular/core';
import { ViewTarget } from 'app/datamodel';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-view-header',
  templateUrl: './rb-view-header.component.html',
  styleUrls: ['./rb-view-header.component.css']
})
export class RbViewHeaderComponent implements OnInit {
  @Input('title') title: string;
  @Input('targetStack') targetStack : ViewTarget[];
  @Output('backTo') back: EventEmitter<any> = new EventEmitter();
  @HostBinding('style.backgroundColor') get backColor() { return this.color != null ? this.color.back : "white";}
  @HostBinding('style.color') get foreColor() { return this.color != null ? this.color.fore : "darkblue";}

  defaultColor: any = {name:"Default", fore:"white", back:"#2071C5"};
  color: any = this.defaultColor;
  colors: any = [
    this.defaultColor,    
    {name:"Dark Blue", fore:"white", back:"#5F71A8"},
    {name:"Light Blue", fore:"white", back:"#0091D5"},
    {name:"Orange", fore:"white", back:"#EA6A47"},
    {name:"Teal", fore:"white", back:"#7E909A"},
    {name:"Grey", fore:"white", back:"#757E8E"},
    {name:"Red", fore:"white", back:"#B54051"},
    {name:"Green", fore:"white", back:"#40B569"},
    {name:"Black", fore:"white", back:"#202020"}
  ]
  constructor(
    public userPref: UserprefService
  ) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges) {
    this.getPreferredColor();
  }

  backTo(event) {
    this.back.emit(event);
  }

  selectColor(color: any) {
    this.color = color;
    this.updatePreferredColor();
  }

  getPreferredColor() {
    let color = this.userPref.getUISwitch("viewheader", "color");
    if(color != null) { 
      this.color = color;
    } else {
      this.color = this.defaultColor;
    }
  }

  updatePreferredColor() {
    this.userPref.setUISwitch("user", "viewheader", "color", this.color);
  }
}

