import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbStackedGraphComponent } from './rb-stacked-graph.component';

describe('RbStackedGraphComponent', () => {
  let component: RbStackedGraphComponent;
  let fixture: ComponentFixture<RbStackedGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbStackedGraphComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbStackedGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
