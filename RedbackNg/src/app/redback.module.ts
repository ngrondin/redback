import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DesktopRootComponent } from './desktop-root/desktop-root.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatToolbarModule, MatButtonModule, MatSidenavModule, MatIconModule, MatListModule, MatList, MatListItem, MatFormFieldModule, MatInputModule, MatCheckboxModule, MatRadioModule, MatSelectModule, MatExpansionModule, MatDialog, MatDialogModule, MatDividerModule, MatProgressSpinner, MatProgressSpinnerModule, MatMenuModule, MatSlideToggleModule, MatTooltipModule, MatTooltip } from '@angular/material';
import { HttpModule } from '@angular/http';
import { HttpClientModule } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { RbListScrollDirective } from './rb-list-scroll/rb-list-scroll.directive';
import { ApiService } from './services/api.service';
import { DataService } from './services/data.service';
import { RbInputComponent } from './inputs/rb-input/rb-input.component';
import { FormsModule } from '@angular/forms';
import { RbRelatedInputComponent } from './inputs/rb-related-input/rb-related-input.component';
import { RbPopupListComponent } from './popups/rb-popup-list/rb-popup-list.component';
import { OverlayModule } from '@angular/cdk/overlay';
import { RbPopupDatetimeComponent } from './popups/rb-popup-datetime/rb-popup-datetime.component';
import { RbDatetimeInputComponent } from './inputs/rb-datetime-input/rb-datetime-input.component';
import { RbTextareaInputComponent } from './inputs/rb-textarea-input/rb-textarea-input.component';
import { RbSearchComponent } from './rb-search/rb-search.component';
import { RbMapComponent } from './rb-map/rb-map.component';
import { RbDurationInputComponent } from './inputs/rb-duration-input/rb-duration-input.component';
import { RbFilterBuilderComponent } from './rb-filter-builder/rb-filter-builder.component';
import { AgmCoreModule } from '@agm/core';
import { RbProcessactionsComponent } from './rb-processactions/rb-processactions.component';
import { RbViewLoaderComponent } from './rb-view-loader/rb-view-loader.component';
import { RbViewDirective } from './rb-view/rb-view.directive';
import { RbGlobalSeachComponent } from './rb-global-seach/rb-global-seach.component';
import { RbLogComponent } from './rb-log/rb-log.component';
import { FileUploadModule } from 'ng2-file-upload';
import { RbFilelistComponent } from './rb-filelist/rb-filelist.component';
import { RbFiledropComponent } from './rb-filedrop/rb-filedrop.component';
import { RbNotificationComponent } from './rb-notification/rb-notification.component';
import { RbLinkComponent } from './rb-link/rb-link.component';
import { RbDynamicformComponent } from './rb-dynamicform/rb-dynamicform.component';
import { RbChoiceInputComponent } from './inputs/rb-choice-input/rb-choice-input.component';
import { RbSwitchInputComponent } from './inputs/rb-switch-input/rb-switch-input.component';
import { RbBreadcrumbComponent } from './rb-breadcrumb/rb-breadcrumb.component';
import { RbListComponent } from './rb-list/rb-list.component';
import { ConfigService } from './services/config.service';
import { RbGanttComponent } from './rb-gantt/rb-gantt.component';
import { RbDragObjectDirective } from './rb-drag/rb-drag-object.directive';
import { RbDragDropzoneDirective } from './rb-drag/rb-drag-dropzone.directive';
import { DragService } from './rb-drag/drag.service';
import { MapService } from './services/map.service';
import { RbGraphComponent } from './rb-graph/rb-graph.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { RbDragChangeformDirective } from './rb-drag/rb-drag-changeform.directive';
import { AgmOverlays } from "agm-overlays"
import { RbChatComponent } from './rb-chat/rb-chat.component';
import { RbAddressInputComponent } from './inputs/rb-address-input/rb-address-input.component';
import { RbPopupInputComponent } from './inputs/rb-popup-input/rb-popup-input.component';
import { RbPopupAddressesComponent } from './popups/rb-popup-addresses/rb-popup-addresses.component';
import { RbModalComponent } from './rb-modal/rb-modal.component';
import { RbFileInputComponent } from './inputs/rb-file-input/rb-file-input.component';
import { RbVcollapseComponent } from './rb-vcollapse/rb-vcollapse.component';
import { RbCurrencyInputComponent } from './inputs/rb-currency-input/rb-currency-input.component';
import { RbCodeInputComponent } from './inputs/rb-code-input/rb-code-input.component';
import { AceEditorModule } from 'ng2-ace-editor';
import { AceConfigInterface, AceModule, ACE_CONFIG } from 'ngx-ace-wrapper';
import 'brace';
import 'brace/mode/text';
import 'brace/mode/html';
import 'brace/mode/javascript';
import 'brace/theme/github';
import 'brace/theme/eclipse';
import { RbTableComponent } from './rb-table/rb-table.component';
import { ReportService } from './services/report.service';
import { RbReportlistComponent } from './rb-reportlist/rb-reportlist.component';
import { RbActiongroupComponent } from './rb-actiongroup/rb-actiongroup.component';
import { RbDatasetComponent } from './rb-dataset/rb-dataset.component';
import { RbLayoutComponent } from './rb-layout/rb-layout.component';
import { RbHsectionComponent } from './rb-hsection/rb-hsection.component';
import { RbVsectionComponent } from './rb-vsection/rb-vsection.component';
import { RbTabComponent } from './rb-tab/rb-tab.component';
import { RbTabSectionComponent } from './rb-tab-section/rb-tab-section.component';
import { RbMenuComponent } from './rb-menu/rb-menu.component';
import { RbMenuGroupComponent } from './rb-menu-group/rb-menu-group.component';
import { RbMenuLinkComponent } from './rb-menu-link/rb-menu-link.component';
import { MenuService } from './services/menu.service';
import { RbList4Component } from './rb-list4/rb-list4.component';
import { RbButtonComponent } from './rb-button/rb-button.component';
import { RbDatasetGroupComponent } from './rb-datasetgroup/rb-datasetgroup.component';
import { RbFormComponent } from './rb-form/rb-form.component';
import { RbAggregatesetComponent } from './rb-aggregateset/rb-aggregateset.component';
import { RbFilesetComponent } from './rb-fileset/rb-fileset.component';
import { RbHseparatorComponent } from './rb-hseparator/rb-hseparator.component';
import { RbSpacerComponent } from './rb-spacer/rb-spacer.component';
import { RbInputCommonComponent } from './inputs/rb-input-common/rb-input-common.component';

/*
const DEFAULT_ACE_CONFIG: AceConfigInterface = {
};
*/
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
    FormsModule,
    OverlayModule,
    FormsModule,
    BrowserAnimationsModule,
    HttpClientModule,
    HttpModule,
    NgxChartsModule,
    AgmOverlays,
    AgmCoreModule.forRoot({
      apiKey: 'AIzaSyBc0KUFKS6XuCL2PRiFv9XATkMFJah6x88'
    }),
    AceEditorModule,
    AceModule
  ],
  declarations: [
    DesktopRootComponent,
    RbViewLoaderComponent,
    RbViewDirective,
    RbLayoutComponent,
    RbHsectionComponent,
    RbVsectionComponent,
    RbDatasetComponent,
    RbDatasetGroupComponent,
    RbListScrollDirective,
    RbListComponent,
    RbList4Component,
    RbInputComponent,
    RbTextareaInputComponent,
    RbRelatedInputComponent,
    RbDatetimeInputComponent,
    RbDurationInputComponent,
    RbPopupListComponent,
    RbPopupDatetimeComponent,
    RbPopupAddressesComponent,
    RbSearchComponent,
    RbMapComponent,
    RbMenuComponent,
    RbMenuGroupComponent,
    RbMenuLinkComponent,
    RbTabComponent,
    RbTabSectionComponent,
    RbFilterBuilderComponent,
    RbProcessactionsComponent,
    RbGlobalSeachComponent,
    RbLogComponent,
    RbFilesetComponent,
    RbFilelistComponent,
    RbFiledropComponent,
    RbFileInputComponent,
    RbNotificationComponent,
    RbLinkComponent,
    RbModalComponent,
    RbDynamicformComponent,
    RbChoiceInputComponent,
    RbSwitchInputComponent,
    RbAddressInputComponent,
    RbBreadcrumbComponent,
    RbGanttComponent,
    RbDragObjectDirective,
    RbDragDropzoneDirective,
    RbDragChangeformDirective,
    RbAggregatesetComponent,
    RbGraphComponent,
    RbChatComponent,
    RbVcollapseComponent,
    RbCurrencyInputComponent,
    RbCodeInputComponent,
    RbTableComponent,
    RbReportlistComponent,
    RbActiongroupComponent,
    RbButtonComponent,
    RbFormComponent,
    RbHseparatorComponent,
    RbSpacerComponent
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
    RbLayoutComponent,
    RbHsectionComponent,
    RbVsectionComponent,
    RbViewLoaderComponent,
    RbViewDirective,
    RbDatasetComponent,
    RbDatasetGroupComponent,
    RbListScrollDirective,
    RbListComponent,
    RbList4Component,
    RbInputComponent,
    RbTextareaInputComponent,
    RbRelatedInputComponent,
    RbDatetimeInputComponent,
    RbDurationInputComponent,
    RbPopupListComponent,
    RbPopupDatetimeComponent,
    RbPopupAddressesComponent,
    RbSearchComponent,
    RbMapComponent,
    RbMenuComponent,
    RbMenuGroupComponent,
    RbMenuLinkComponent,
    RbTabComponent,
    RbTabSectionComponent,
    RbFilterBuilderComponent,
    RbProcessactionsComponent,
    RbGlobalSeachComponent,
    RbLogComponent,
    RbFilesetComponent,
    RbFilelistComponent,
    RbFiledropComponent,
    RbFileInputComponent,
    RbNotificationComponent,
    RbLinkComponent,
    RbModalComponent,
    RbDynamicformComponent,
    RbChoiceInputComponent,
    RbSwitchInputComponent,
    RbAddressInputComponent,
    RbBreadcrumbComponent,
    RbGanttComponent,
    RbDragObjectDirective,
    RbDragDropzoneDirective,
    RbDragChangeformDirective,
    RbAggregatesetComponent,
    RbGraphComponent,
    RbChatComponent,
    RbVcollapseComponent,
    RbCurrencyInputComponent,
    RbCodeInputComponent,
    RbTableComponent,
    RbReportlistComponent,
    RbActiongroupComponent,
    RbButtonComponent,
    RbFormComponent,
    RbHseparatorComponent,
    RbSpacerComponent
  ],
  providers: [
    CookieService,
    ApiService,
    DataService,
    ConfigService,
    DragService,
    MapService,
    ReportService,
    MenuService/*,
    {
      provide: ACE_CONFIG,
      useValue: DEFAULT_ACE_CONFIG
    }*/
  ],
  entryComponents: [
    RbPopupListComponent,
    RbPopupDatetimeComponent,
    RbFilterBuilderComponent,
    RbPopupAddressesComponent,
    RbReportlistComponent
  ],
  bootstrap: [
  ] 
})
export class RedbackModule { }
