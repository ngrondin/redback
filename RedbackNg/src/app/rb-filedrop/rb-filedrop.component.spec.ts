import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbFiledropComponent } from './rb-filedrop.component';

describe('RbFiledropComponent', () => {
  let component: RbFiledropComponent;
  let fixture: ComponentFixture<RbFiledropComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbFiledropComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbFiledropComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
