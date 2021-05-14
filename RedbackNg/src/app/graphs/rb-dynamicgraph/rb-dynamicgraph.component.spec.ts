import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbGraphComponent } from './rb-dynamicgraph.component';

describe('RbGraphComponent', () => {
  let component: RbGraphComponent;
  let fixture: ComponentFixture<RbGraphComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbGraphComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
