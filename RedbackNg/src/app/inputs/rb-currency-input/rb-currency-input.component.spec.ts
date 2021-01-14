import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbCurrencyInputComponent } from './rb-currency-input.component';

describe('RbCurrencyInputComponent', () => {
  let component: RbCurrencyInputComponent;
  let fixture: ComponentFixture<RbCurrencyInputComponent>;

  beforeEach(async(() => {
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
