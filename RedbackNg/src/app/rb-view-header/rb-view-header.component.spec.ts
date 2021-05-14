import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbViewHeaderComponent } from './rb-view-header.component';

describe('RbViewHeaderComponent', () => {
  let component: RbViewHeaderComponent;
  let fixture: ComponentFixture<RbViewHeaderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbViewHeaderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbViewHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
