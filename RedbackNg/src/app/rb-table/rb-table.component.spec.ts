import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbTableComponent } from './rb-table.component';

describe('RbTableComponent', () => {
  let component: RbTableComponent;
  let fixture: ComponentFixture<RbTableComponent>;

  beforeEach(async(() => {
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
