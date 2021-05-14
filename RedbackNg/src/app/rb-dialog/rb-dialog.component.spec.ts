import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbDialogComponent } from './rb-dialog.component';

describe('RbDialogComponent', () => {
  let component: RbDialogComponent;
  let fixture: ComponentFixture<RbDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
