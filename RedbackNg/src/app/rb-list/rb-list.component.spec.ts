import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbListComponent } from './rb-list.component';

describe('RbListComponent', () => {
  let component: RbListComponent;
  let fixture: ComponentFixture<RbListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
