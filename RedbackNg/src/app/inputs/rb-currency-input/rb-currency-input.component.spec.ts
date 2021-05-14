import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbCurrencyInputComponent } from './rb-currency-input.component';

describe('RbCurrencyInputComponent', () => {
  let component: RbCurrencyInputComponent;
  let fixture: ComponentFixture<RbCurrencyInputComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbCurrencyInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbCurrencyInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
