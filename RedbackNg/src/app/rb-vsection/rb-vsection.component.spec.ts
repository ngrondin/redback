import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbVsectionComponent } from './rb-vsection.component';

describe('RbVsectionComponent', () => {
  let component: RbVsectionComponent;
  let fixture: ComponentFixture<RbVsectionComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbVsectionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbVsectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
