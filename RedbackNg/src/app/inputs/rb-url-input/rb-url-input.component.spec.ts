import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbUrlInputComponent } from './rb-url-input.component';

describe('RbUrlInputComponent', () => {
  let component: RbUrlInputComponent;
  let fixture: ComponentFixture<RbUrlInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbUrlInputComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbUrlInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
