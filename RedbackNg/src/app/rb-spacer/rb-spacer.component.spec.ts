import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbSpacerComponent } from './rb-spacer.component';

describe('RbSpacerComponent', () => {
  let component: RbSpacerComponent;
  let fixture: ComponentFixture<RbSpacerComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbSpacerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbSpacerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
