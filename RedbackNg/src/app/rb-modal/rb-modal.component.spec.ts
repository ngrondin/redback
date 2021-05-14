import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbModalComponent } from './rb-modal.component';

describe('RbModalComponent', () => {
  let component: RbModalComponent;
  let fixture: ComponentFixture<RbModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
