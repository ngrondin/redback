import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbRelatedInputComponent } from './rb-related-input.component';

describe('RbRelatedInputComponent', () => {
  let component: RbRelatedInputComponent;
  let fixture: ComponentFixture<RbRelatedInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbRelatedInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbRelatedInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
