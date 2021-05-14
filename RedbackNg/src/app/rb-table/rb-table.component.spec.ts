import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbTableComponent } from './rb-table.component';

describe('RbTableComponent', () => {
  let component: RbTableComponent;
  let fixture: ComponentFixture<RbTableComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
