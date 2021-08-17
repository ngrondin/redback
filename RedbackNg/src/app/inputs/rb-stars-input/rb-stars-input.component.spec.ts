import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbStarsInputComponent } from './rb-stars-input.component';

describe('RbStarsInputComponent', () => {
  let component: RbStarsInputComponent;
  let fixture: ComponentFixture<RbStarsInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbStarsInputComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RbStarsInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
