import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbPopupAddressesComponent } from './rb-popup-addresses.component';

describe('RbPopupAddressesComponent', () => {
  let component: RbPopupAddressesComponent;
  let fixture: ComponentFixture<RbPopupAddressesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbPopupAddressesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbPopupAddressesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
