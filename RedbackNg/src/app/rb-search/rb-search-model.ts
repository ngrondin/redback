import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { RbDatasetGroupComponent } from 'app/rb-datasetgroup/rb-datasetgroup.component';
import { RbSearchTarget } from './rb-search-target';

export class SearchMode {
  label: string;
  filterconfig?: any;
  sortconfig?: any;
  targetdatasetid?: string;
  searchtarget?: RbSearchTarget;
  filterValue?: any;
  sortValue?: any;

  constructor(json: any) {
    this.label = json.label;
    this.filterconfig = json.filter;
    this.sortconfig = json.sort;
    this.targetdatasetid = json.targetdatasetid;
  }

  resolveSearchTarget(dataset: RbDatasetComponent, datasetgroup: RbDatasetGroupComponent) {
    if(this.searchtarget == null) {
      if(dataset != null) {
        this.searchtarget = dataset;
      } else if(datasetgroup != null) {
        if(this.targetdatasetid != null) {
          this.searchtarget = datasetgroup.datasets[this.targetdatasetid];
        }
        if(this.searchtarget == null) { //Didn't find the specified dataset id
          this.searchtarget = datasetgroup;
        }
      }
    }
  }
}
