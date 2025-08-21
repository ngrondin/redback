import { Component, OnInit, Output, EventEmitter, Inject } from '@angular/core';
import { CONTAINER_DATA } from 'app/tokens';
import { OverlayRef } from '@angular/cdk/overlay';
import { DataService } from 'app/services/data.service';
import { FilterService } from 'app/services/filter.service';
import { ValueComparator } from 'app/helpers';
import { FilterConfig, SortConfig, FilterAttributeConfig, SortAttributeConfig, FilterBuilderConfig } from './rb-filter-builder-configs';
import { FilterItemConstruct, SavedEntry, SortItemConstruct } from './rb-filter-builder-constructs';
import { UserprefService } from 'app/services/userpref.service';
import { MenuService } from 'app/services/menu.service';
import { NavigateService } from 'app/services/navigate.service';



@Component({
  selector: 'rb-filter-builder',
  templateUrl: './rb-filter-builder.component.html',
  styleUrls: ['./rb-filter-builder.component.css']
})
export class RbFilterBuilderComponent implements OnInit {

  @Output() done: EventEmitter<any> = new EventEmitter();

  userPref: any = {};
  filterConfig: FilterConfig;
  filter: any = {};
  filterConstructs: FilterItemConstruct[] = []
  sortConfig: SortConfig;
  sort: any = {};
  sortConstructs: SortItemConstruct[] = [];
  savedEntries: SavedEntry[] = [];
  _attributeToAddToFilter: any;
  _attributeToAddToSort: any;
  changed: boolean = false;
  tab: string = "filter";
  editingSavedEntry: SavedEntry;

  datechoice: any = [
    { value: "lasthour", display: "Last Hour"},
    { value: "lastday", display: "Last Day"},
    { value: "sincelast", display: "Since Last..."},
    { value: "since", display: "Since..."},
    { value: "nexthour", display: "Next Hour"},
    { value: "nextday", display: "Next Day"},
    { value: "untilnext", display: "Until Next..."},
    { value: "until", display: "Until..."},
    { value: "between", display: "Between..."},
    { value: "rollwindow", display: "Rolling Window..."},
  ]

  constructor(
    @Inject(CONTAINER_DATA) public config: FilterBuilderConfig, 
    public overlayRef: OverlayRef,
    public dataService: DataService,
    private userprefService: UserprefService,
    private filterService: FilterService,
    private menuService: MenuService,
    private navigateService: NavigateService
  ) { 
    this.filter = this.config.initialFilter;
    if(this.config.filterConfig != null) {
      this.filterConfig = new FilterConfig(this.config.filterConfig);
    }
    this.sort = this.config.initialSort;
    if(this.config.sortConfig != null) {
      this.sortConfig = new SortConfig(this.config.sortConfig);
    }
  }

  ngOnInit() {
    this.decompile();

    if(this.config.datasetid != null) {
      this.userPref = this.userprefService.getCurrentViewUISwitch('dataset', this.config.datasetid);
      if(this.userPref != null && this.userPref.saved != null) {
        this.savedEntries = this.userPref.saved.map(e => {
          let filter = this.filterService.removePrefixDollarSign(e.filter);
          return new SavedEntry(e.name, filter, e.sort, e.default ?? false);
        });
      }
    }

    if(this.savedEntries.length > 0) {
      this.tab = "saved";
    }

  }

  get addFilterActions(): any {
    return this.filterConfig.attributes.map(a => ({action:"none", label: a.label, param: a.attribute}));
  }

  get addSortActions(): any {
    return this.sortConfig.attributes.map(a => ({action:"none", label: a.label, param: a.attribute}));
  }


  get empty() : boolean {
    return this.filterConstructs.length == 0 && this.sortConstructs.length == 0;
  }

  get modeCount() : number {
    return (this.hasSavedEntries ? 1 : 0) + (this.canFilter ? 1 : 0) + (this.canSort ? 1 : 0);
  }

  get canSave() : boolean {
    return this.config.datasetid != null;
  }

  get hasSavedEntries() : boolean {
    return this.savedEntries.length > 0;
  }

  get isEditingSavedEntry() : boolean {
    return this.editingSavedEntry != null;
  }

  get canFilter() : boolean {
    return this.config.filterConfig != null;
  }

  get canSort() : boolean {
    return this.config.sortConfig != null;
  }
 
  addAttributeToFilter(attribute: string) {
    let fac = this.filterConfig.attributes.find(ac => ac.attribute == attribute);
    let fic = new FilterItemConstruct(fac, null);
    this.filterConstructs.push(fic);
    this.getAggregatesFor(fac);
    this.changed = true;
  }

  addAttributeToSort(attribute: string) {
    let sac: SortAttributeConfig = this.sortConfig.attributes.find(ac => ac.attribute == attribute);
    let sic = new SortItemConstruct(sac, this.sortConstructs.length, {dir:1});
    this.sortConstructs.push(sic);
    this.changed = true;
  }

  removeFilterItem(fic: FilterItemConstruct) {
    this.filterConstructs.splice(this.filterConstructs.indexOf(fic), 1);
    this.changed = true;
  }

  removeSortItem(sic: SortItemConstruct) {
    this.sortConstructs.splice(this.sortConstructs.indexOf(sic), 1);
    this.changed = true;
  }

  removeSavedEntry(se: SavedEntry) {
    this.savedEntries.splice(this.savedEntries.indexOf(se), 1);
    this.changed = true;
  }

  toggleDir(sic: SortItemConstruct) {
    sic.direction = (-1 * sic.direction );
    this.changed = true;
  }

  getAggregatesFor(fac: FilterAttributeConfig) {
    if(fac.type == 'multiselect') {
      let fltr = {};
      for (const key in this.filter) {
        if(key != fac.attribute) {
          fltr[key] = this.filter[key];
        }
      }
      if(this.filterConfig.useBaseFilter == true && this.config.aggregateFilter != null) {
        fltr = this.filterService.mergeFilters(fltr, this.config.aggregateFilter);
      }
      fltr = this.filterService.resolveFilter(fltr, null, null, null);
      this.dataService.aggregate(this.config.objectname, fltr, null, [fac.attribute], [{function:"count", name:"_cnt"}], null, 0, 2000).subscribe({
        next: (list) => {
          fac.options = list.map(agg => ({
            name: agg.getDimension(fac.attribute + "." + fac.displayAttribute) ?? 'Empty', 
            value: agg.getDimension(fac.attribute) ?? null,
            count:agg.getMetric("_cnt")
          }));
          fac.options.sort((a, b) => ValueComparator.valueCompare(a, b, 'name'));
        }
      });
    }    
  }

  selectTab(t) {
    this.tab = t;
  }

  clickApply() {
    this.compile();
    this.done.emit({filter: this.filter, sort: this.sort});
  }

  clickSave() {
    if(this.editingSavedEntry == null) {
      this.editingSavedEntry = new SavedEntry(null, {}, {}, false);
      this.tab = 'details';
    } else {
      this.compile();
      this.editingSavedEntry.name = this.editingSavedEntry.name ?? "Unnamed";
      this.editingSavedEntry.filter = this.filter;
      this.editingSavedEntry.sort = this.sort;
      if(!this.savedEntries.includes(this.editingSavedEntry)) {
        this.savedEntries.push(this.editingSavedEntry);
      }
      if(this.editingSavedEntry.default == true) {
        this.savedEntries.filter(e => e != this.editingSavedEntry).forEach(e => e.default = false);
      }
      this.commitSavedEntries();
      this.editingSavedEntry = null;
      this.tab = 'saved';
    }
  }

  clickClear() {
    this.done.emit({filter: null, sort: null});
  }

  clickCancel() {
    if(this.editingSavedEntry != null) {
      this.editingSavedEntry = null;
      this.tab = 'saved';
    } else {
      this.overlayRef.dispose();
    }
  }

  clickSavedEntry(entry: SavedEntry) {
    this.done.emit({filter: entry.filter, sort: entry.sort});
  }

  clickSavedEntryEdit(entry: SavedEntry) {
    this.editingSavedEntry = entry;
    this.filter = entry.filter;
    this.sort = entry.sort;
    this.decompile();
    this.tab = 'details';
  }

  createMenuLink() {
    let entry = this.editingSavedEntry;
    if(entry != null) {
      let currentView = this.navigateService.getCurrentLoadedView();
      let menuLink = {
        type: "menulink",
        view: currentView.name,
        icon: "filter",
        label: entry.name,
        filter: this.filterService.prefixDollarSign(entry.filter)
      }
      this.menuService.addToPersonalMenu(menuLink);
    }
    this.editingSavedEntry = null;
    this.tab = 'saved';
  }

  deleteSavedEntry() {
    this.savedEntries.splice(this.savedEntries.indexOf(this.editingSavedEntry), 1);
    this.commitSavedEntries();
    this.editingSavedEntry = null;
    this.tab =  this.savedEntries.length > 0 ? 'saved' : 'filter';
  }

  dateOptionComparison(option: any, value: any) : boolean  {
    return JSON.stringify(option) == JSON.stringify(value);
  }

  decompile() {
    if(this.filter != null) {
      this.filterConstructs = [];
      for(let key in this.filter) {
        let fac: FilterAttributeConfig = this.filterConfig.getAttributeConfig(key);
        let fic = new FilterItemConstruct(fac, this.filter[key]);
        this.filterConstructs.push(fic);
        this.getAggregatesFor(fac);
      }
    }

    if(this.sort != null) {
      this.sortConstructs = [];
      for(let key in this.sort) {
        let order: number = parseInt(key);
        let sortItem: any = this.sort[key];
        let sic = new SortItemConstruct(this.sortConfig.getAttributeConfig(sortItem.attribute), order, sortItem);
        this.sortConstructs.push(sic);
      }
    }
  }

  compile() {
    if(this.filterConstructs.length > 0) {
      this.filter = {};
      for(let fic of this.filterConstructs) {
        this.filter[fic.config.attribute] = fic.getFilterValue();
      }  
    } else {
      this.filter = null;
    }
    if(this.sortConstructs.length > 0) {
      this.sort = {};
      for(let sic of this.sortConstructs) {
        this.sort[sic.order] = sic.getSortValue();
      }  
    } else {
      this.sort = null;
    }
  }

  commitSavedEntries() {
    if(this.config.datasetid != null) {
      if(this.userPref == null) this.userPref = {};
      this.userPref.saved = this.savedEntries.map(e => ({
        name: e.name, 
        filter: this.filterService.prefixDollarSign(e.filter),
        sort: e.sort, 
        default: e.default ?? false
      }));
      this.userprefService.setUISwitch("user", "dataset", this.config.datasetid, this.userPref);
    }
  }


}
