import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbMultiRelatedInputComponent } from './rb-multi-related-input.component';

describe('RbMultiRelatedInputComponent', () => {
  let component: RbMultiRelatedInputComponent;
  let fixture: ComponentFixture<RbMultiRelatedInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbMultiRelatedInputComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbMultiRelatedInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
