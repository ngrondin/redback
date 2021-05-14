import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbDatasetgroupComponent } from './rb-datasetgroup.component';

describe('RbDatasetgroupComponent', () => {
  let component: RbDatasetgroupComponent;
  let fixture: ComponentFixture<RbDatasetgroupComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbDatasetgroupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbDatasetgroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
