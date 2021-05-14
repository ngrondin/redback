import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbPopupListComponent } from './rb-popup-list.component';

describe('RbPopupListComponent', () => {
  let component: RbPopupListComponent;
  let fixture: ComponentFixture<RbPopupListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbPopupListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbPopupListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
