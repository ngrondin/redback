import { Component, Input, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { MatIconRegistry } from '@angular/material';
import { DomSanitizer } from '@angular/platform-browser';
import { ApiService } from './api.service';

@Component({
  viewProviders: [MatIconRegistry],
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title: string; 
  type: string;
  version: string;
  initialView: string;
  initialViewTitle: string;
  menuView: string;
  iconsets: string[];

  constructor(
      private elementRef: ElementRef,
      private matIconRegistry: MatIconRegistry,
      private domSanitizer: DomSanitizer,
      private apiService: ApiService ) {
    var native = this.elementRef.nativeElement;
    this.type = native.getAttribute("type");
    this.title = native.getAttribute("title");
    this.version = native.getAttribute("version");
    this.initialView = native.getAttribute("initialview");
    this.initialViewTitle = native.getAttribute("initialviewtitle");
    this.menuView = native.getAttribute("menuview");
    this.apiService.uiService = native.getAttribute("uiservice");
    this.apiService.objectService = native.getAttribute("objectservice");
    this.apiService.processService = native.getAttribute("processservice");

    let currentUrl = window.location.href;
    let pos = currentUrl.indexOf(this.apiService.uiService);
    this.apiService.baseUrl = currentUrl.substring(0, pos - 1);

    this.iconsets = native.getAttribute("iconsets").split(",");
    for(const set of this.iconsets) {
      this.matIconRegistry.addSvgIconSetInNamespace(set, this.domSanitizer.bypassSecurityTrustResourceUrl(this.apiService.baseUrl + '/' + this.apiService.uiService + '/resource/' + set + '.svg'));
    }
  }
}
