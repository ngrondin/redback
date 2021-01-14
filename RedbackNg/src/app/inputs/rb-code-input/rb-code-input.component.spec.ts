import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbCodeInputComponent } from './rb-code-input.component';

describe('RbCodeInputComponent', () => {
  let component: RbCodeInputComponent;
  let fixture: ComponentFixture<RbCodeInputComponent>;

  beforeEach(async(() => {
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
