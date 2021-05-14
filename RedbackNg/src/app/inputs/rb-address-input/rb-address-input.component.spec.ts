import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbAddressInputComponent } from './rb-address-input.component';

describe('RbAddressInputComponent', () => {
  let component: RbAddressInputComponent;
  let fixture: ComponentFixture<RbAddressInputComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbAddressInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbAddressInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
