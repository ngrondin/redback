import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { ToastrModule } from 'ngx-toastr';

import {Compiler, COMPILER_OPTIONS, CompilerFactory, Injectable, APP_INITIALIZER} from '@angular/core';
import {JitCompilerFactory} from '@angular/platform-browser-dynamic';
import { Injector } from '@angular/core';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DesktopRootComponent } from './roots/desktop-root/desktop-root.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRadioModule } from '@angular/material/radio';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSliderModule } from '@angular/material/slider';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatMenuModule } from '@angular/material/menu';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HttpClientModule } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { ApiService } from './services/api.service';
import { DataService } from './services/data.service';
import { FormsModule } from '@angular/forms';
import { FileService } from './services/file.service';
import { RbRelatedInputComponent } from './inputs/rb-related-input/rb-related-input.component';
import { RbPopupListComponent } from './popups/rb-popup-list/rb-popup-list.component';
import { RbPopupDatetimeComponent } from './popups/rb-popup-datetime/rb-popup-datetime.component';
import { RbDatetimeInputComponent } from './inputs/rb-datetime-input/rb-datetime-input.component';
import { RbTextareaInputComponent } from './inputs/rb-textarea-input/rb-textarea-input.component';
import { RbSearchComponent } from './rb-search/rb-search.component';
import { RbMapComponent } from './rb-map/rb-map.component';
import { RbDurationInputComponent } from './inputs/rb-duration-input/rb-duration-input.component';
import { RbFilterBuilderComponent } from './rb-filter-builder/rb-filter-builder.component';
import { AgmCoreModule, LAZY_MAPS_API_CONFIG, LazyMapsAPILoaderConfigLiteral} from '@agm/core';
import { RbProcessactionsComponent } from './clickable/rb-processactions/rb-processactions.component';
import { RbViewLoaderComponent } from './rb-view-loader/rb-view-loader.component';
import { RbViewDirective } from './rb-view/rb-view.directive';
import { RbGlobalSeachComponent } from './rb-global-seach/rb-global-seach.component';
import { RbLogComponent } from './rb-log/rb-log.component';
import { RbFilelistComponent } from './rb-filelist/rb-filelist.component';
import { RbFiledropComponent } from './rb-filedrop/rb-filedrop.component';
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
import { FilterService } from './services/filter.service';
import { RbDynamicGraphComponent } from './graphs/rb-dynamicgraph/rb-dynamicgraph.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { RbDragChangeformDirective } from './rb-drag/rb-drag-changeform.directive';
import { AgmOverlays } from "agm-overlays"
import { RbChatComponent } from './rb-chat/rb-chat.component';
import { RbAddressInputComponent } from './inputs/rb-address-input/rb-address-input.component';
import { RbPopupAddressesComponent } from './popups/rb-popup-addresses/rb-popup-addresses.component';
import { RbModalComponent } from './rb-modal/rb-modal.component';
import { RbFileInputComponent } from './inputs/rb-file-input/rb-file-input.component';
import { RbVcollapseComponent } from './rb-vcollapse/rb-vcollapse.component';
import { RbCurrencyInputComponent } from './inputs/rb-currency-input/rb-currency-input.component';
import { RbCodeInputComponent } from './inputs/rb-code-input/rb-code-input.component';
import { AceModule } from 'ngx-ace-wrapper';
import 'brace';
import 'brace/mode/text';
import 'brace/mode/html';
import 'brace/mode/javascript';
import 'brace/theme/github';
import 'brace/theme/eclipse';
import { RbTableComponent } from './rb-table/rb-table.component';
import { ReportService } from './services/report.service';
import { RbReportlistComponent } from './rb-reportlist/rb-reportlist.component';
import { RbActiongroupComponent } from './clickable/rb-actiongroup/rb-actiongroup.component';
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
import { RbActionButtonComponent } from './clickable/rb-actionbutton/rb-actionbutton.component';
import { RbDatasetGroupComponent } from './rb-datasetgroup/rb-datasetgroup.component';
import { RbFormComponent } from './rb-form/rb-form.component';
import { RbAggregatesetComponent } from './rb-aggregateset/rb-aggregateset.component';
import { RbFilesetComponent } from './rb-fileset/rb-fileset.component';
import { RbHseparatorComponent } from './rb-hseparator/rb-hseparator.component';
import { RbSpacerComponent } from './rb-spacer/rb-spacer.component';
import { RbCalendarComponent } from './rb-calendar/rb-calendar.component';
import { RbPopupHardlistComponent } from './popups/rb-popup-hardlist/rb-popup-hardlist.component';
import { RbStringInputComponent } from './inputs/rb-string-input/rb-string-input.component';
import { RbNumberInputComponent } from './inputs/rb-number-input/rb-number-input.component';
import { RbViewHeaderComponent } from './rb-view-header/rb-view-header.component';
import { DragService } from './services/drag.service';
import { RbDialogComponent } from './rb-dialog/rb-dialog.component';
import { RbTileComponent } from './rb-tile/rb-tile.component';
import { RbTimesliderComponent } from './rb-timeslider/rb-timeslider.component';
import { RbNumberTilesComponent } from './graphs/rb-number-tiles/rb-number-tiles.component';
import { RbTimelineComponent } from './rb-timeline/rb-timeline.component';
import { CachedSrcDirective, RbDatePipe, RbTimePipe } from './helpers';
import { RbStarsInputComponent } from './inputs/rb-stars-input/rb-stars-input.component';
import { RbPercentInputComponent } from './inputs/rb-percent-input/rb-percent-input.component';
import { RbNotificationListComponent } from './rb-notification-list/rb-notification-list.component';
import { PlatformModule } from '@angular/cdk/platform';
import { RbTreeComponent } from './rb-tree/rb-tree.component';
import { RbHierarchyInputComponent } from './inputs/rb-hierarchy-input/rb-hierarchy-input.component';
import { RbPopupHierarchyComponent } from './popups/rb-popup-hierarchy/rb-popup-hierarchy.component';
import { RbPopupActionsComponent } from './popups/rb-popup-actions/rb-popup-actions.component';
import { RbListItemComponent } from './clickable/rb-list-item/rb-list-item.component';
import { RbClickableComponent } from './clickable/rb-clickable/rb-clickable.component';
import { RbIconbuttonComponent } from './clickable/rb-iconbutton/rb-iconbutton.component';
import { RbButtonComponent } from './clickable/rb-button/rb-button';
import { RbRichtextInputComponent } from './inputs/rb-richtext-input/rb-richtext-input.component';
import { RbVseparatorComponent } from './rb-vseparator/rb-vseparator.component'
import { RedbackgraphsModule } from 'redbackgraphs';
import { RbDynamicformeditorComponent } from './rb-dynamicformeditor/rb-dynamicformeditor.component';
import { PortalRootComponent } from './roots/portal-root/portal-root.component';
import { RbActionlistComponent } from './clickable/rb-actionlist/rb-actionlist.component';
import { RbFunnelComponent } from './rb-funnel/rb-funnel.component';
import { RbIconbuttonBadgeComponent } from './clickable/rb-iconbutton-badge/rb-iconbutton-badge.component';
import { RbUrlInputComponent } from './inputs/rb-url-input/rb-url-input.component';




export function createCompiler(compilerFactory: CompilerFactory) {
  return compilerFactory.createCompiler();
}

export let AppInjector: Injector;

declare global {
  interface Window { redback: any; }
}
window.redback = window.redback || {};



//export let isoDateRegExp: RegExp = /^(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(?:\\.\\d{1,9})?(?:Z|[+-][01]\\d:[0-5]\\d)$/;


@NgModule({
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        PlatformModule,
        MatIconModule,
        HttpClientModule,
        ToastrModule.forRoot(),
        CommonModule,
        MatToolbarModule,
        MatButtonModule,
        MatSidenavModule,
        MatListModule,
        MatFormFieldModule,
        MatInputModule,
        MatCheckboxModule,
        MatRadioModule,
        MatExpansionModule,
        MatDialogModule,
        MatDividerModule,
        MatProgressSpinnerModule,
        MatProgressBarModule,
        MatMenuModule,
        MatSlideToggleModule,
        MatSliderModule,
        MatTooltipModule,
        FormsModule,
        FormsModule,
        BrowserAnimationsModule,
        HttpClientModule,
        NgxChartsModule,
        AgmOverlays,
        AgmCoreModule.forRoot({
            apiKey: window['googlekey']
        }),
        AceModule,
        RedbackgraphsModule
    ],
    exports: [
        MatIconModule
    ],
    declarations: [
        AppComponent,
        DesktopRootComponent,
        PortalRootComponent,
        RbViewLoaderComponent,
        RbViewDirective,
        RbLayoutComponent,
        RbHsectionComponent,
        RbVsectionComponent,
        RbDatasetComponent,
        RbDatasetGroupComponent,
        RbListComponent,
        RbList4Component,
        RbStringInputComponent,
        RbNumberInputComponent,
        RbTextareaInputComponent,
        RbRelatedInputComponent,
        RbHierarchyInputComponent,
        RbDatetimeInputComponent,
        RbDurationInputComponent,
        RbPopupListComponent,
        RbPopupHierarchyComponent,
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
        RbDynamicGraphComponent,
        RbChatComponent,
        RbVcollapseComponent,
        RbCurrencyInputComponent,
        RbCodeInputComponent,
        RbTableComponent,
        RbReportlistComponent,
        RbButtonComponent,
        RbActiongroupComponent,
        RbActionButtonComponent,
        RbFormComponent,
        RbHseparatorComponent,
        RbSpacerComponent,
        RbCalendarComponent,
        RbPopupHardlistComponent,
        RbViewHeaderComponent,
        RbDialogComponent,
        RbTileComponent,
        RbTimesliderComponent,
        RbNumberTilesComponent,
        RbTimelineComponent,
        RbDatePipe,
        RbTimePipe,
        RbStarsInputComponent,
        RbPercentInputComponent,
        RbNotificationListComponent,
        RbTreeComponent,
        RbPopupActionsComponent,
        RbListItemComponent,
        RbClickableComponent,
        RbIconbuttonComponent,
        RbRichtextInputComponent,
        RbVseparatorComponent,
        RbDynamicformeditorComponent,
        RbActionlistComponent,
        RbFunnelComponent,
        RbIconbuttonBadgeComponent,
        RbUrlInputComponent,
        CachedSrcDirective
    ],
    providers: [
        { provide: COMPILER_OPTIONS, useValue: {}, multi: true },
        { provide: CompilerFactory, useClass: JitCompilerFactory, deps: [COMPILER_OPTIONS] },
        { provide: Compiler, useFactory: createCompiler, deps: [CompilerFactory] },
        CookieService,
        ApiService,
        DataService,
        FileService,
        ConfigService,
        DragService,
        FilterService,
        ReportService,
        MenuService
    ],
    bootstrap: [
        AppComponent
    ]
})
export class AppModule { 
  constructor(private injector: Injector) {
    AppInjector = this.injector;
  }  
}
