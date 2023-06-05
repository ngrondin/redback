import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbPopupActionsComponent } from './rb-popup-actions.component';

describe('RbPopupActionsComponent', () => {
  let component: RbPopupActionsComponent;
  let fixture: ComponentFixture<RbPopupActionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbPopupActionsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbPopupActionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
