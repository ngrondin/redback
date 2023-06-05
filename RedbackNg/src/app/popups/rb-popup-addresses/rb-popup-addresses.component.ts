import { Component, OnInit, Inject, ChangeDetectorRef } from '@angular/core';
import { RbPopupComponent } from 'app/popups/rb-popup/rb-popup.component';
import { CONTAINER_DATA } from 'app/tokens';
import { OverlayRef } from '@angular/cdk/overlay';
import { ApiService } from 'app/services/api.service';

@Component({
  selector: 'rb-popup-addresses',
  templateUrl: './rb-popup-addresses.component.html',
  styleUrls: ['./rb-popup-addresses.component.css']
})
export class RbPopupAddressesComponent extends RbPopupComponent implements OnInit {
  public list: String[] = [];
  public center: any;
  public radius: number;
  public search: String;
  public isLoading: boolean;
  public highlightIndex: number = -1;


  constructor(
    @Inject(CONTAINER_DATA) public config: any, 
    public overlayRef: OverlayRef,
    private apiService: ApiService,
    private cd: ChangeDetectorRef
  ) {
    super();
    this.isLoading = false;
  }

  ngOnInit(): void {
    if(this.config.center != null) {
      this.center = this.config.center;
      this.radius = this.config.radius;
    }    
  }

  public getHighlighted() {
    if(this.highlightIndex > -1 && this.highlightIndex < this.list.length) {
      return this.list[this.highlightIndex];
    } else {
      return null;
    }
  }

  public setSearch(val: String) {
    this.search = val;
    this.isLoading = true;
    this.apiService.predictAddresses(this.search, this.center, this.radius).subscribe(json => this.setAddresses(json));
  }

  public keyTyped(keyCode: number) {
    if(keyCode == 40) { // Down
      if(this.highlightIndex < this.list.length - 1) {
        this.highlightIndex++;
      }
    } else if(keyCode == 38) { // Up
      if(this.highlightIndex > 0) {
        this.highlightIndex--;
      }
    } else if(keyCode == 13) {
      if(this.highlightIndex > -1 && this.highlightIndex < this.list.length) {
        this.select(this.list[this.highlightIndex]);
      }
    }
  }

  public setAddresses(json: any) {
    this.isLoading = false;
    this.list = json.map(item => item.description);
    this.cd.detectChanges();
    //console.log("addresses set");
  }

  public select(value: String) {
    this.selected.emit(value);
  }

}
