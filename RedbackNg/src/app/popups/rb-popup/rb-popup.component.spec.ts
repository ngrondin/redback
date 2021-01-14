import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbPopupComponent } from './rb-popup.component';

describe('RbPopupComponent', () => {
  let component: RbPopupComponent;
  let fixture: ComponentFixture<RbPopupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbPopupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbPopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
