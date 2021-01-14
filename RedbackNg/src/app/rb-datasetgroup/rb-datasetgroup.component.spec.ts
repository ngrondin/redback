import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbDatasetgroupComponent } from './rb-datasetgroup.component';

describe('RbDatasetgroupComponent', () => {
  let component: RbDatasetgroupComponent;
  let fixture: ComponentFixture<RbDatasetgroupComponent>;

  beforeEach(async(() => {
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
