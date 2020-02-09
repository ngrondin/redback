import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RbObject } from 'app/datamodel';
import { DataService } from 'app/data.service';

@Component({
  selector: 'rb-global-search',
  templateUrl: './rb-global-seach.component.html',
  styleUrls: ['./rb-global-seach.component.css']
})
export class RbGlobalSeachComponent implements OnInit {
  @Input() objectViewMap : any;
  @Output() navigate: EventEmitter<any> = new EventEmitter();

  searchString: string;
  searchResult: RbObject[] = [];
  showResults: boolean = false;
  isLoading: number = 0;

  constructor(
    private dataService: DataService
  ) { }

  ngOnInit() {
  }

  globalSearch() {
    this.searchResult = [];
    this.showResults = true;
    for(let o in this.objectViewMap) {
      this.isLoading = this.isLoading + 1;
      this.dataService.listObjects(o, null, this.searchString).subscribe(
        data => {
          this.searchResult = this.searchResult.concat(data);
          this.isLoading = this.isLoading - 1;
        }
      );
    }
  }

  keyDown(event: any) {
    if(event.keyCode == 13) {
      this.globalSearch();
    }
  }

  blur(event: any) {
    setTimeout(()=> {
      this.showResults = false;;
    }, 100);
  }

  navigateToObject(object: string, uid: string) {
    this.navigate.emit({
      object : object,
      filter : {
        uid : uid
      }
    });
  }
}
