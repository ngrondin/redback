import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbLogComponent } from './rb-log.component';

describe('RbLogComponent', () => {
  let component: RbLogComponent;
  let fixture: ComponentFixture<RbLogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbLogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbLogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
