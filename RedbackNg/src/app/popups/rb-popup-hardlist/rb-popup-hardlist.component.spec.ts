import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbPopupHardlistComponent } from './rb-popup-hardlist.component';

describe('RbPopupHardlistComponent', () => {
  let component: RbPopupHardlistComponent;
  let fixture: ComponentFixture<RbPopupHardlistComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbPopupHardlistComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbPopupHardlistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
