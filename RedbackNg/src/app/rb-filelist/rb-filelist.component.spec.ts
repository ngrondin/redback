import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbFilelistComponent } from './rb-filelist.component';

describe('RbFilelistComponent', () => {
  let component: RbFilelistComponent;
  let fixture: ComponentFixture<RbFilelistComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbFilelistComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbFilelistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
