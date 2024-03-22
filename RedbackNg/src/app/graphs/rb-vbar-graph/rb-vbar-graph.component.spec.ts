import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbVbarGraphComponent } from './rb-vbar-graph.component';

describe('RbVbarGraphComponent', () => {
  let component: RbVbarGraphComponent;
  let fixture: ComponentFixture<RbVbarGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbVbarGraphComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbVbarGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
