import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbProgressComponent } from './rb-progress.component';

describe('RbProgressComponent', () => {
  let component: RbProgressComponent;
  let fixture: ComponentFixture<RbProgressComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbProgressComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbProgressComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
