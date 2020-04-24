import { Directive, Input } from '@angular/core';
import { RbDatasetDirective } from 'app/rb-dataset/rb-dataset.directive';

@Directive({
  selector: 'rb-datasetgroup',
  exportAs: 'datasetgroup'
})
export class RbDatasetGroupDirective {
  @Input('active') active: boolean;

  datasets: RbDatasetDirective[] = [];

  constructor() { }

  public register(name: string, dataset: RbDatasetDirective) {
    this.datasets[name] = dataset;
  }

  public get lists() : any {
    let l = {};
    for(let key of Object.keys(this.datasets)) {
      l[key] = this.datasets[key].list;
    }
    return l;
  }
}
