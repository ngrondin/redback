import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbGanttComponent } from './rb-gantt.component';

describe('RbGanttComponent', () => {
  let component: RbGanttComponent;
  let fixture: ComponentFixture<RbGanttComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbGanttComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbGanttComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
