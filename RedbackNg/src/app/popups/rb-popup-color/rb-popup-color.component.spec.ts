import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbPopupColorComponent } from './rb-popup-color.component';

describe('RbPopupColorComponent', () => {
  let component: RbPopupColorComponent;
  let fixture: ComponentFixture<RbPopupColorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbPopupColorComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbPopupColorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
