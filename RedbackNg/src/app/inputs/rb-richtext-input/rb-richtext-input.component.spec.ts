import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbRichtextInputComponent } from './rb-richtext-input.component';

describe('RbRichtextInputComponent', () => {
  let component: RbRichtextInputComponent;
  let fixture: ComponentFixture<RbRichtextInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbRichtextInputComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbRichtextInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
