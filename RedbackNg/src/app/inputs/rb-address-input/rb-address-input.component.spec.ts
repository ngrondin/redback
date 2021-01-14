import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbAddressInputComponent } from './rb-address-input.component';

describe('RbAddressInputComponent', () => {
  let component: RbAddressInputComponent;
  let fixture: ComponentFixture<RbAddressInputComponent>;

  beforeEach(async(() => {
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
