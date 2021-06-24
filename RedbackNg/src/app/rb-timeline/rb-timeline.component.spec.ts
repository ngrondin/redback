import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbTimelineComponent } from './rb-timeline.component';

describe('RbTimelineComponent', () => {
  let component: RbTimelineComponent;
  let fixture: ComponentFixture<RbTimelineComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbTimelineComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RbTimelineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
