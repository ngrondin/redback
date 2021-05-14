import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbMapComponent } from './rb-map.component';

describe('RbMapComponent', () => {
  let component: RbMapComponent;
  let fixture: ComponentFixture<RbMapComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbMapComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbMapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
