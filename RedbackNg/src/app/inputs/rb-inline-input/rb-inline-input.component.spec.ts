import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbInlineInputComponent } from './rb-inline-input.component';

describe('RbInlineInputComponent', () => {
  let component: RbInlineInputComponent;
  let fixture: ComponentFixture<RbInlineInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbInlineInputComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbInlineInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
