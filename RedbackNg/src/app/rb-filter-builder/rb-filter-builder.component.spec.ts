import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbFilterBuilderComponent } from './rb-filter-builder.component';

describe('RbFilterBuilderComponent', () => {
  let component: RbFilterBuilderComponent;
  let fixture: ComponentFixture<RbFilterBuilderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbFilterBuilderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbFilterBuilderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
