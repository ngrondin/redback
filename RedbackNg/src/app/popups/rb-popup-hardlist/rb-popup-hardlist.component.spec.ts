import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbPopupHardlistComponent } from './rb-popup-hardlist.component';

describe('RbPopupHardlistComponent', () => {
  let component: RbPopupHardlistComponent;
  let fixture: ComponentFixture<RbPopupHardlistComponent>;

  beforeEach(async(() => {
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
