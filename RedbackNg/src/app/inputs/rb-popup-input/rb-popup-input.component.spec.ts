import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbPopupInputComponent } from './rb-popup-input.component';

describe('RbPopupInputComponent', () => {
  let component: RbPopupInputComponent;
  let fixture: ComponentFixture<RbPopupInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbPopupInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbPopupInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
