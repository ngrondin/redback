import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbTabSectionComponent } from './rb-tab-section.component';

describe('RbTabSectionComponent', () => {
  let component: RbTabSectionComponent;
  let fixture: ComponentFixture<RbTabSectionComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbTabSectionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbTabSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
