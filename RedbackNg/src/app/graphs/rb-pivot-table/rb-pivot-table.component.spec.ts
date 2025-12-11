import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbPivotTableComponent } from './rb-pivot-table.component';

describe('RbPivotTableComponent', () => {
  let component: RbPivotTableComponent;
  let fixture: ComponentFixture<RbPivotComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbPivotTableComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbPivotTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
