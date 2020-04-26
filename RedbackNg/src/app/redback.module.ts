import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DesktopRootComponent } from './desktop-root/desktop-root.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatToolbarModule, MatButtonModule, MatSidenavModule, MatIconModule, MatListModule, MatList, MatListItem, MatFormFieldModule, MatInputModule, MatCheckboxModule, MatRadioModule, MatSelectModule, MatExpansionModule, MatDialog, MatDialogModule, MatDividerModule, MatProgressSpinner, MatProgressSpinnerModule, MatMenuModule, MatSlideToggleModule, MatTooltipModule, MatTooltip } from '@angular/material';
import { HttpModule } from '@angular/http';
import { HttpClientModule } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { RbDatasetDirective } from './rb-dataset/rb-dataset.directive';
import { RbMenuDirective } from './rb-menu/rb-menu.directive';
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
import { RbViewLoaderComponent } from './rb-view-loader/rb-view-loader.component';
import { RbViewDirective } from './rb-view/rb-view.directive';
import { RbGlobalSeachComponent } from './rb-global-seach/rb-global-seach.component';
import { RbLogComponent } from './rb-log/rb-log.component';
import { RbFilesetDirective } from './rb-fileset/rb-fileset.directive';
import { FileUploadModule } from 'ng2-file-upload';
import { RbFilelistComponent } from './rb-filelist/rb-filelist.component';
import { RbFiledropComponent } from './rb-filedrop/rb-filedrop.component';
import { RbNotificationComponent } from './rb-notification/rb-notification.component';
import { RbLinkComponent } from './rb-link/rb-link.component';
import { RbDynamicformComponent } from './rb-dynamicform/rb-dynamicform.component';
import { RbChoiceInputComponent } from './rb-choice-input/rb-choice-input.component';
import { RbSwitchInputComponent } from './rb-switch-input/rb-switch-input.component';
import { RbBreadcrumbComponent } from './rb-breadcrumb/rb-breadcrumb.component';
import { RbListComponent } from './rb-list/rb-list.component';
import { ConfigService } from './config.service';
import { RbGanttComponent } from './rb-gantt/rb-gantt.component';
import { RbDatasetGroupDirective } from './rb-datasetgroup/rb-datasetgroup.directive';
import { RbDragObjectDirective } from './rb-drag/rb-drag-object.directive';
import { RbDragDropzoneDirective } from './rb-drag/rb-drag-dropzone.directive';
import { DragService } from './rb-drag/drag.service';
import { MapService } from './map.service';
import { RbAggregatesetDirective } from './rb-aggregateset/rb-aggregateset.directive';
import { RbGraphComponent } from './rb-graph/rb-graph.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';

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
    MatSlideToggleModule,
    MatTooltipModule,
    FileUploadModule,
    OverlayModule,
    FormsModule,
    BrowserAnimationsModule,
    HttpClientModule,
    HttpModule,
    NgxChartsModule,
    AgmCoreModule.forRoot({
      apiKey: 'AIzaSyBc0KUFKS6XuCL2PRiFv9XATkMFJah6x88'
    })
  ],
  declarations: [
    DesktopRootComponent,
    RbViewLoaderComponent,
    RbViewDirective,
    RbDatasetDirective,
    RbDatasetGroupDirective,
    RbListScrollDirective,
    RbListComponent,
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
    RbProcessactionsComponent,
    RbGlobalSeachComponent,
    RbLogComponent,
    RbFilesetDirective,
    RbFilelistComponent,
    RbFiledropComponent,
    RbNotificationComponent,
    RbLinkComponent,
    RbDynamicformComponent,
    RbChoiceInputComponent,
    RbSwitchInputComponent,
    RbBreadcrumbComponent,
    RbGanttComponent,
    RbDragObjectDirective,
    RbDragDropzoneDirective,
    RbAggregatesetDirective,
    RbGraphComponent
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
    MatTooltipModule,
    RbViewLoaderComponent,
    RbViewDirective,
    RbDatasetDirective,
    RbDatasetGroupDirective,
    RbListScrollDirective,
    RbListComponent,
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
    RbProcessactionsComponent,
    RbGlobalSeachComponent,
    RbLogComponent,
    RbFilesetDirective,
    RbFilelistComponent,
    RbFiledropComponent,
    RbNotificationComponent,
    RbLinkComponent,
    RbDynamicformComponent,
    RbChoiceInputComponent,
    RbSwitchInputComponent,
    RbBreadcrumbComponent,
    RbGanttComponent,
    RbDragObjectDirective,
    RbDragDropzoneDirective,
    RbAggregatesetDirective,
    RbGraphComponent
  ],
  providers: [
    CookieService,
    ApiService,
    DataService,
    ConfigService,
    DragService,
    MapService
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
