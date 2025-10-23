import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbColorInputComponent } from './rb-color-input.component';

describe('RbColorInputComponent', () => {
  let component: RbColorInputComponent;
  let fixture: ComponentFixture<RbColorInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbColorInputComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbColorInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
