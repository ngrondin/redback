import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DesktopRootComponent } from './desktop-root/desktop-root.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatToolbarModule, MatButtonModule, MatSidenavModule, MatIconModule, MatListModule, MatList, MatListItem, MatFormFieldModule, MatInputModule, MatCheckboxModule, MatRadioModule, MatSelectModule, MatExpansionModule, MatDialog, MatDialogModule } from '@angular/material';
import { HttpModule } from '@angular/http';
import { HttpClientModule } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { RbDatasetDirective } from './rb-dataset/rb-dataset.directive';
import { RbViewLoaderComponent } from './rb-view-loader/rb-view-loader.component';
import { RbVsectionDirective } from './rb-vsection/rb-vsection.directive';
import { RbLayoutDirective } from './rb-layout/rb-layout.directive';
import { RbHsectionDirective } from './rb-hsection/rb-hsection.directive';
import { RbListScrollDirective } from './rb-list-scroll/rb-list-scroll.directive';
import { ApiService } from './api.service';
import { DataService } from './data.service';
import { RbInputComponent } from './rb-input/rb-input.component';
import { FormsModule } from '@angular/forms';
import { RbRelatedInputComponent } from './rb-related-input/rb-related-input.component';
import { RbPopupListComponent } from './rb-popup-list/rb-popup-list.component';
import { OverlayModule } from '@angular/cdk/overlay';

@NgModule({
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatRadioModule,
    MatSelectModule,
    MatExpansionModule,
    MatDialogModule,
    FormsModule,
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
    RbListScrollDirective,
    RbInputComponent,
    RbRelatedInputComponent,
    RbPopupListComponent
  ],
  exports: [
    DesktopRootComponent,
    MatList,
    MatListItem,
    RbDatasetDirective,
    RbViewLoaderComponent,
    RbVsectionDirective,
    RbHsectionDirective,
    RbLayoutDirective,
    RbListScrollDirective,
    RbInputComponent,
    RbRelatedInputComponent,
    RbPopupListComponent
  ],
  providers: [
    CookieService,
    ApiService,
    DataService
  ],
  entryComponents: [
    RbPopupListComponent
  ],
  bootstrap: [
  ] 
})
export class RedbackModule { }
