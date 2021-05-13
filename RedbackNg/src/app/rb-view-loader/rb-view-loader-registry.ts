import { RbAddressInputComponent } from "app/inputs/rb-address-input/rb-address-input.component";
import { RbChoiceInputComponent } from "app/inputs/rb-choice-input/rb-choice-input.component";
import { RbCodeInputComponent } from "app/inputs/rb-code-input/rb-code-input.component";
import { RbCurrencyInputComponent } from "app/inputs/rb-currency-input/rb-currency-input.component";
import { RbDatetimeInputComponent } from "app/inputs/rb-datetime-input/rb-datetime-input.component";
import { RbDurationInputComponent } from "app/inputs/rb-duration-input/rb-duration-input.component";
import { RbFileInputComponent } from "app/inputs/rb-file-input/rb-file-input.component";
import { RbNumberInputComponent } from "app/inputs/rb-number-input/rb-number-input.component";
import { RbRelatedInputComponent } from "app/inputs/rb-related-input/rb-related-input.component";
import { RbStringInputComponent } from "app/inputs/rb-string-input/rb-string-input.component";
import { RbSwitchInputComponent } from "app/inputs/rb-switch-input/rb-switch-input.component";
import { RbTextareaInputComponent } from "app/inputs/rb-textarea-input/rb-textarea-input.component";
import { RbActiongroupComponent } from "app/rb-actiongroup/rb-actiongroup.component";
import { RbAggregatesetComponent } from "app/rb-aggregateset/rb-aggregateset.component";
import { RbButtonComponent } from "app/rb-button/rb-button.component";
import { RbCalendarComponent } from "app/rb-calendar/rb-calendar.component";
import { RbDatasetComponent } from "app/rb-dataset/rb-dataset.component";
import { RbDatasetGroupComponent } from "app/rb-datasetgroup/rb-datasetgroup.component";
import { RbDynamicformComponent } from "app/rb-dynamicform/rb-dynamicform.component";
import { RbFiledropComponent } from "app/rb-filedrop/rb-filedrop.component";
import { RbFilelistComponent } from "app/rb-filelist/rb-filelist.component";
import { RbFilesetComponent } from "app/rb-fileset/rb-fileset.component";
import { RbFormComponent } from "app/rb-form/rb-form.component";
import { RbGanttComponent } from "app/rb-gantt/rb-gantt.component";
import { RbGraphComponent } from "app/rb-graph/rb-graph.component";
import { RbHsectionComponent } from "app/rb-hsection/rb-hsection.component";
import { RbHseparatorComponent } from "app/rb-hseparator/rb-hseparator.component";
import { RbLayoutComponent } from "app/rb-layout/rb-layout.component";
import { RbLinkComponent } from "app/rb-link/rb-link.component";
import { RbListComponent } from "app/rb-list/rb-list.component";
import { RbList4Component } from "app/rb-list4/rb-list4.component";
import { RbLogComponent } from "app/rb-log/rb-log.component";
import { RbMapComponent } from "app/rb-map/rb-map.component";
import { RbModalComponent } from "app/rb-modal/rb-modal.component";
import { RbProcessactionsComponent } from "app/rb-processactions/rb-processactions.component";
import { RbSearchComponent } from "app/rb-search/rb-search.component";
import { RbSpacerComponent } from "app/rb-spacer/rb-spacer.component";
import { RbTabSectionComponent } from "app/rb-tab-section/rb-tab-section.component";
import { RbTabComponent } from "app/rb-tab/rb-tab.component";
import { RbTableComponent } from "app/rb-table/rb-table.component";
import { RbTileComponent } from "app/rb-tile/rb-tile.component";
import { RbVcollapseComponent } from "app/rb-vcollapse/rb-vcollapse.component";
import { RbVsectionComponent } from "app/rb-vsection/rb-vsection.component";

export const componentRegistry = {
    "dataset": RbDatasetComponent,
    "datasetgroup": RbDatasetGroupComponent,
    "layout": RbLayoutComponent,
    "form": RbFormComponent,
    "hsection": RbHsectionComponent,
    "vsection": RbVsectionComponent,
    "tabsection": RbTabSectionComponent,
    "tab": RbTabComponent,
    "input": RbStringInputComponent,
    "numberinput":RbNumberInputComponent,
    "relatedinput": RbRelatedInputComponent,
    "list3": RbListComponent,
    "list4": RbList4Component,
    "button": RbButtonComponent,
    "search": RbSearchComponent,
    "actiongroup": RbActiongroupComponent,
    "addressinput": RbAddressInputComponent,
    "aggregateset": RbAggregatesetComponent,
    "choiceinput": RbChoiceInputComponent,
    "codeinput": RbCodeInputComponent,
    "currencyinput": RbCurrencyInputComponent,
    "durationinput": RbDurationInputComponent,
    "dynamicform": RbDynamicformComponent,
    "fileinput": RbFileInputComponent,
    "filedrop": RbFiledropComponent,
    "filelist": RbFilelistComponent,
    "fileset": RbFilesetComponent,
    "textarea": RbTextareaInputComponent,
    "datepicker": RbDatetimeInputComponent,
    "dateinput": RbTextareaInputComponent,
    "gantt": RbGanttComponent,
    "link": RbLinkComponent,
    "log": RbLogComponent,
    "map": RbMapComponent,
    "modal": RbModalComponent,
    "processactionsbutton": RbProcessactionsComponent,
    "switch": RbSwitchInputComponent,
    "table": RbTableComponent,
    "vcollapse": RbVcollapseComponent,
    "hseparator": RbHseparatorComponent,
    "spacer": RbSpacerComponent,
    "graph": RbGraphComponent,
    "calendar": RbCalendarComponent,
    "tile": RbTileComponent
  }