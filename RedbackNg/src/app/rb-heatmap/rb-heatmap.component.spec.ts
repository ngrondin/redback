import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbHeatmapComponent } from './rb-heatmap.component';

describe('RbHeatmapComponent', () => {
  let component: RbHeatmapComponent;
  let fixture: ComponentFixture<RbHeatmapComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbHeatmapComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbHeatmapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
