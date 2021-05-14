import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbCodeInputComponent } from './rb-code-input.component';

describe('RbCodeInputComponent', () => {
  let component: RbCodeInputComponent;
  let fixture: ComponentFixture<RbCodeInputComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbCodeInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbCodeInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
