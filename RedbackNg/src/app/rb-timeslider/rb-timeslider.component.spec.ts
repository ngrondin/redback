import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbTimesliderComponent } from './rb-timeslider.component';

describe('RbTimesliderComponent', () => {
  let component: RbTimesliderComponent;
  let fixture: ComponentFixture<RbTimesliderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbTimesliderComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RbTimesliderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
