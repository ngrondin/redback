import { Component, OnInit, Input } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';

@Component({
  selector: 'desktop-root',
  templateUrl: './desktop-root.component.html',
  styleUrls: ['./desktop-root.component.css']
})
export class DesktopRootComponent implements OnInit {

  @Input() title : string;
  @Input() initialView : string;
  view: string;
  viewUrl : string;
  viewBase : string;

  constructor(
    private cookieService : CookieService
  ) { }

  ngOnInit() {
    this.cookieService.set('rbtoken','eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJlbWFpbCI6Im5ncm9uZGluNzhAZ21haWwuY29tIiwiZXhwIjoxOTIwNTExOTIyMDAwfQ.zQrN7sheh1PuO4fWru45dTPDtkLAqB9Q0WrwGO6yOeo', 1920511922000, "/", "http://localhost", false, 'Lax');
    this.viewBase = 'http://localhost/rbui/ng8/view/';
    this.view = this.initialView;
    this.viewUrl = this.viewBase + this.view;
  }

  get menuUrl() : string {
    return 'http://localhost/rbui/ng8/menu/wmsmenu'
  }

  navigateTo($event) {
    this.view = $event.view;
    this.viewUrl = this.viewBase + this.view;
  }
}
