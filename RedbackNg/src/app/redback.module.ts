import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DesktopRootComponent } from './desktop-root/desktop-root.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatToolbarModule, MatButtonModule, MatSidenavModule, MatIconModule, MatListModule, MatList, MatListItem } from '@angular/material';
import { HttpModule } from '@angular/http';
import { HttpClientModule } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { RbDatasetDirective } from './rb-dataset.directive';
import { RbViewLoaderComponent } from './rb-view-loader/rb-view-loader.component';
import { RbVsectionDirective } from './rb-vsection.directive';
import { RbLayoutDirective } from './rb-layout.directive';
import { RbHsectionDirective } from './rb-hsection.directive';
import { RbListScrollDirective } from './rb-list-scroll.directive';
import { ApiService } from './api.service';
import { DataService } from './data.service';

@NgModule({
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    BrowserAnimationsModule,
    HttpClientModule,
    HttpModule    
  ],
  declarations: [
    DesktopRootComponent,
    RbDatasetDirective,
    RbViewLoaderComponent,
    RbVsectionDirective,
    RbHsectionDirective,
    RbLayoutDirective,
    RbListScrollDirective
  ],
  exports: [
    DesktopRootComponent,
    RbDatasetDirective,
    RbViewLoaderComponent,
    RbVsectionDirective,
    RbHsectionDirective,
    RbLayoutDirective,
    RbListScrollDirective,
    MatList,
    MatListItem
  ],
  providers: [
    CookieService,
    ApiService,
    DataService
  ],
  bootstrap: [
  ] 
})
export class RedbackModule { }
