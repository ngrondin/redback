import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbProcessactionsComponent } from './rb-processactions.component';

describe('RbProcessactionsComponent', () => {
  let component: RbProcessactionsComponent;
  let fixture: ComponentFixture<RbProcessactionsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbProcessactionsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbProcessactionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
