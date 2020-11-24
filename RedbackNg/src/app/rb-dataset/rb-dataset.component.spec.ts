import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbDatasetComponent } from './rb-dataset.component';

describe('RbDataset1Component', () => {
  let component: RbDatasetComponent;
  let fixture: ComponentFixture<RbDatasetComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbDatasetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbDatasetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
