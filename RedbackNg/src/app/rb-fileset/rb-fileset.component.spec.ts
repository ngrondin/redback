import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbFilesetComponent } from './rb-fileset.component';

describe('RbFilesetComponent', () => {
  let component: RbFilesetComponent;
  let fixture: ComponentFixture<RbFilesetComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbFilesetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbFilesetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
