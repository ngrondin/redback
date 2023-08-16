import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbActionlistComponent } from './rb-actionlist.component';

describe('RbActionlistComponent', () => {
  let component: RbActionlistComponent;
  let fixture: ComponentFixture<RbActionlistComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbActionlistComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbActionlistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
