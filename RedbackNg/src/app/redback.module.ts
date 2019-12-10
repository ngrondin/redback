import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DesktopRootComponent } from './desktop-root/desktop-root.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatToolbarModule, MatButtonModule, MatSidenavModule, MatIconModule, MatListModule, MatList, MatListItem, MatFormFieldModule, MatInputModule, MatCheckboxModule, MatRadioModule, MatSelectModule, MatExpansionModule, MatDialog, MatDialogModule, MatDividerModule, MatProgressSpinner, MatProgressSpinnerModule, MatMenuModule } from '@angular/material';
import { HttpModule } from '@angular/http';
import { HttpClientModule } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { RbDatasetDirective } from './rb-dataset/rb-dataset.directive';
import { RbMenuDirective } from './rb-menu/rb-menu.directive';
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
import { RbTabDirective } from './rb-tab/rb-tab.directive';
import { RbTabSectionDirective } from './rb-tab-section/rb-tab-section.directive';
import { RbPopupDatetimeComponent } from './rb-popup-datetime/rb-popup-datetime.component';
import { RbDatetimeInputComponent } from './rb-datetime-input/rb-datetime-input.component';
import { RbTextareaInputComponent } from './rb-textarea-input/rb-textarea-input.component';
import { RbSearchComponent } from './rb-search/rb-search.component';
import { RbMapComponent } from './rb-map/rb-map.component';

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
    MatIconModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    OverlayModule,
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
    RbTextareaInputComponent,
    RbRelatedInputComponent,
    RbDatetimeInputComponent,
    RbPopupListComponent,
    RbPopupDatetimeComponent,
    RbSearchComponent,
    RbMapComponent,
    RbMenuDirective,
    RbTabDirective,
    RbTabSectionDirective
  ],
  exports: [
    DesktopRootComponent,
    MatList,
    MatListItem,
    MatIconModule,
    MatDividerModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    RbDatasetDirective,
    RbViewLoaderComponent,
    RbVsectionDirective,
    RbHsectionDirective,
    RbLayoutDirective,
    RbListScrollDirective,
    RbInputComponent,
    RbTextareaInputComponent,
    RbRelatedInputComponent,
    RbDatetimeInputComponent,
    RbPopupListComponent,
    RbPopupDatetimeComponent,
    RbSearchComponent,
    RbMapComponent,
    RbMenuDirective,
    RbTabDirective,
    RbTabSectionDirective
  ],
  providers: [
    CookieService,
    ApiService,
    DataService
  ],
  entryComponents: [
    RbPopupListComponent,
    RbPopupDatetimeComponent
  ],
  bootstrap: [
  ] 
})
export class RedbackModule { }
