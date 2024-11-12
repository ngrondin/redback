import { RbDynamicGraphComponent } from "app/graphs/rb-dynamicgraph/rb-dynamicgraph.component";
import { RbNumberTilesComponent } from "app/graphs/rb-number-tiles/rb-number-tiles.component";
import { RbAddressInputComponent } from "app/inputs/rb-address-input/rb-address-input.component";
import { RbChoiceInputComponent } from "app/inputs/rb-choice-input/rb-choice-input.component";
import { RbCodeInputComponent } from "app/inputs/rb-code-input/rb-code-input.component";
import { RbCurrencyInputComponent } from "app/inputs/rb-currency-input/rb-currency-input.component";
import { RbDatetimeInputComponent } from "app/inputs/rb-datetime-input/rb-datetime-input.component";
import { RbDurationInputComponent } from "app/inputs/rb-duration-input/rb-duration-input.component";
import { RbFileInputComponent } from "app/inputs/rb-file-input/rb-file-input.component";
import { RbNumberInputComponent } from "app/inputs/rb-number-input/rb-number-input.component";
import { RbPercentInputComponent } from "app/inputs/rb-percent-input/rb-percent-input.component";
import { RbRelatedInputComponent } from "app/inputs/rb-related-input/rb-related-input.component";
import { RbHierarchyInputComponent } from "app/inputs/rb-hierarchy-input/rb-hierarchy-input.component";
import { RbStarsInputComponent } from "app/inputs/rb-stars-input/rb-stars-input.component";
import { RbStringInputComponent } from "app/inputs/rb-string-input/rb-string-input.component";
import { RbSwitchInputComponent } from "app/inputs/rb-switch-input/rb-switch-input.component";
import { RbTextareaInputComponent } from "app/inputs/rb-textarea-input/rb-textarea-input.component";
import { RbActiongroupComponent } from "app/clickable/rb-actiongroup/rb-actiongroup.component";
import { RbAggregatesetComponent } from "app/rb-aggregateset/rb-aggregateset.component";
import { RbCalendarComponent } from "app/rb-calendar/rb-calendar.component";
import { RbDatasetComponent } from "app/rb-dataset/rb-dataset.component";
import { RbDatasetGroupComponent } from "app/rb-datasetgroup/rb-datasetgroup.component";
import { RbDynamicformComponent } from "app/rb-dynamicform/rb-dynamicform.component";
import { RbFiledropComponent } from "app/rb-filedrop/rb-filedrop.component";
import { RbFilelistComponent } from "app/rb-filelist/rb-filelist.component";
import { RbFilesetComponent } from "app/rb-fileset/rb-fileset.component";
import { RbFormComponent } from "app/rb-form/rb-form.component";
import { RbGanttComponent } from "app/rb-gantt/rb-gantt.component";
import { RbHsectionComponent } from "app/rb-hsection/rb-hsection.component";
import { RbHseparatorComponent } from "app/rb-hseparator/rb-hseparator.component";
import { RbLayoutComponent } from "app/rb-layout/rb-layout.component";
import { RbLinkComponent } from "app/rb-link/rb-link.component";
import { RbListComponent } from "app/rb-list/rb-list.component";
import { RbList4Component } from "app/rb-list4/rb-list4.component";
import { RbTreeComponent } from "app/rb-tree/rb-tree.component";
import { RbLogComponent } from "app/rb-log/rb-log.component";
import { RbMapComponent } from "app/rb-map/rb-map.component";
import { RbModalComponent } from "app/rb-modal/rb-modal.component";
import { RbProcessactionsComponent } from "app/clickable/rb-processactions/rb-processactions.component";
import { RbSearchComponent } from "app/rb-search/rb-search.component";
import { RbSpacerComponent } from "app/rb-spacer/rb-spacer.component";
import { RbTabSectionComponent } from "app/rb-tab-section/rb-tab-section.component";
import { RbTabComponent } from "app/rb-tab/rb-tab.component";
import { RbTableComponent } from "app/rb-table/rb-table.component";
import { RbLinktableComponent } from "app/rb-linktable/rb-linktable.component";
import { RbTileComponent } from "app/rb-tile/rb-tile.component";
import { RbTimelineComponent } from "app/rb-timeline/rb-timeline.component";
import { RbVcollapseComponent } from "app/rb-vcollapse/rb-vcollapse.component";
import { RbVsectionComponent } from "app/rb-vsection/rb-vsection.component";
import { RbActionButtonComponent } from "app/clickable/rb-actionbutton/rb-actionbutton.component";
import { RbRichtextInputComponent } from "app/inputs/rb-richtext-input/rb-richtext-input.component";
import { RbVseparatorComponent } from "app/rb-vseparator/rb-vseparator.component";
import { RbDynamicformeditorComponent } from "app/rb-dynamicformeditor/rb-dynamicformeditor.component";
import { RbFunnelComponent } from "app/rb-funnel/rb-funnel.component";
import { RbActionlistComponent } from "app/clickable/rb-actionlist/rb-actionlist.component";
import { RbUrlInputComponent } from "app/inputs/rb-url-input/rb-url-input.component";
import { RbStackedGraphComponent } from "app/graphs/rb-stacked-graph/rb-stacked-graph.component";
import { RbVbarGraphComponent } from "app/graphs/rb-vbar-graph/rb-vbar-graph.component";
import { RbHbarGraphComponent } from "app/graphs/rb-hbar-graph/rb-hbar-graph.component";
import { RbScrollComponent } from "app/rb-scroll/rb-scroll.component";
import { RbGroupComponent } from "app/rb-group/rb-group.component";
import { RbTextComponent } from "app/rb-text/rb-text.component";
import { RbHcollapseComponent } from "app/rb-hcollapse/rb-hcollapse.component";
import { RbProgressComponent } from "app/rb-progress/rb-progress.component";
import { RbImageComponent } from "app/rb-image/rb-image.component";

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
    "hierarchyinput": RbHierarchyInputComponent,
    "list3": RbListComponent,
    "list4": RbList4Component,
    "tree": RbTreeComponent,
    "button": RbActionButtonComponent,
    "search": RbSearchComponent,
    "actiongroup": RbActiongroupComponent,
    "addressinput": RbAddressInputComponent,
    "aggregateset": RbAggregatesetComponent,
    "choiceinput": RbChoiceInputComponent,
    "codeinput": RbCodeInputComponent,
    "currencyinput": RbCurrencyInputComponent,
    "durationinput": RbDurationInputComponent,
    "dynamicform": RbDynamicformComponent,
    "dynamicformeditor": RbDynamicformeditorComponent,
    "fileinput": RbFileInputComponent,
    "filedrop": RbFiledropComponent,
    "filelist": RbFilelistComponent,
    "fileset": RbFilesetComponent,
    "textarea": RbTextareaInputComponent,
    'richtext': RbRichtextInputComponent,
    "datepicker": RbDatetimeInputComponent,
    "dateinput": RbTextareaInputComponent,
    "urlinput": RbUrlInputComponent,
    "gantt": RbGanttComponent,
    "link": RbLinkComponent,
    "log": RbLogComponent,
    "map": RbMapComponent,
    "modal": RbModalComponent,
    "processactionsbutton": RbProcessactionsComponent,
    "switch": RbSwitchInputComponent,
    "table": RbTableComponent,
    "linktable": RbLinktableComponent,
    "vcollapse": RbVcollapseComponent,
    "hcollapse": RbHcollapseComponent,
    "hseparator": RbHseparatorComponent,
    "vseparator": RbVseparatorComponent,
    "spacer": RbSpacerComponent,
    "graph": RbDynamicGraphComponent,
    "calendar": RbCalendarComponent,
    "tile": RbTileComponent,
    "numbertiles": RbNumberTilesComponent,
    "stackedgraph": RbStackedGraphComponent,
    "vbargraph": RbVbarGraphComponent,
    "hbargraph": RbHbarGraphComponent,
    "timeline": RbTimelineComponent,
    "stars": RbStarsInputComponent,
    "percentinput": RbPercentInputComponent,
    "funnel":RbFunnelComponent,
    "listbutton": RbActionlistComponent,
    "scroll":RbScrollComponent,
    "group": RbGroupComponent,
    "text":RbTextComponent,
    "progress": RbProgressComponent,
    "image":RbImageComponent
  }