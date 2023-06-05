import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbActiongroupComponent } from './rb-actiongroup.component';

describe('RbActiongroupComponent', () => {
  let component: RbActiongroupComponent;
  let fixture: ComponentFixture<RbActiongroupComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbActiongroupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbActiongroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
