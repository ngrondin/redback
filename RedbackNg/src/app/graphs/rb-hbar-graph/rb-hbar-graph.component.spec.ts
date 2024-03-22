import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbHbarGraphComponent } from './rb-hbar-graph.component';

describe('RbHbarGraphComponent', () => {
  let component: RbHbarGraphComponent;
  let fixture: ComponentFixture<RbHbarGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbHbarGraphComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbHbarGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
