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
import { RbDurationInputComponent } from './rb-duration-input/rb-duration-input.component';
import { RbFilterBuilderComponent } from './rb-filter-builder/rb-filter-builder.component';
import { AgmCoreModule } from '@agm/core';
import { RbProcessactionsComponent } from './rb-processactions/rb-processactions.component';

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
    HttpModule,
    AgmCoreModule.forRoot({
      apiKey: 'AIzaSyBc0KUFKS6XuCL2PRiFv9XATkMFJah6x88'
    })    
  ],
  declarations: [
    DesktopRootComponent,
    RbDatasetDirective,
    RbViewLoaderComponent,
    RbListScrollDirective,
    RbInputComponent,
    RbTextareaInputComponent,
    RbRelatedInputComponent,
    RbDatetimeInputComponent,
    RbDurationInputComponent,
    RbPopupListComponent,
    RbPopupDatetimeComponent,
    RbSearchComponent,
    RbMapComponent,
    RbMenuDirective,
    RbTabDirective,
    RbTabSectionDirective,
    RbFilterBuilderComponent,
    RbProcessactionsComponent
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
    RbListScrollDirective,
    RbInputComponent,
    RbTextareaInputComponent,
    RbRelatedInputComponent,
    RbDatetimeInputComponent,
    RbDurationInputComponent,
    RbPopupListComponent,
    RbPopupDatetimeComponent,
    RbSearchComponent,
    RbMapComponent,
    RbMenuDirective,
    RbTabDirective,
    RbTabSectionDirective,
    RbFilterBuilderComponent,
    RbProcessactionsComponent
  ],
  providers: [
    CookieService,
    ApiService,
    DataService
  ],
  entryComponents: [
    RbPopupListComponent,
    RbPopupDatetimeComponent,
    RbFilterBuilderComponent
  ],
  bootstrap: [
  ] 
})
export class RedbackModule { }
