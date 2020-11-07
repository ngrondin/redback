import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbReportlistComponent } from './rb-reportlist.component';

describe('RbReportlistComponent', () => {
  let component: RbReportlistComponent;
  let fixture: ComponentFixture<RbReportlistComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbReportlistComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbReportlistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
