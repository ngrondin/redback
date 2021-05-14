import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbNumberTilesComponent } from './rb-number-tiles.component';

describe('RbNumberTilesComponent', () => {
  let component: RbNumberTilesComponent;
  let fixture: ComponentFixture<RbNumberTilesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbNumberTilesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbNumberTilesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
