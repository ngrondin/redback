import { Component, OnInit, Inject, ChangeDetectorRef } from '@angular/core';
import { RbPopupComponent } from 'app/rb-popup/rb-popup.component';
import { CONTAINER_DATA } from 'app/tokens';
import { OverlayRef } from '@angular/cdk/overlay';
import { ApiService } from 'app/api.service';

@Component({
  selector: 'rb-popup-addresses',
  templateUrl: './rb-popup-addresses.component.html',
  styleUrls: ['./rb-popup-addresses.component.css']
})
export class RbPopupAddressesComponent extends RbPopupComponent implements OnInit {
  public list: String[] = [];
  public search: String;
  public isLoading: boolean;


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
  }

  public getHighlighted() {
    return null;
  }

  public setSearch(val: String) {
    this.search = val;
    this.isLoading = true;
    this.apiService.predictAddresses(this.search).subscribe(json => this.setAddresses(json));
  }

  public setAddresses(json: any) {
    this.isLoading = false;
    this.list = json.map(item => item.description);
    this.cd.detectChanges();
    console.log("addresses set");
  }

  public select(value: String) {
    this.selected.emit(value);
  }

}
