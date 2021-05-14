import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbVcollapseComponent } from './rb-vcollapse.component';

describe('RbVcollapseComponent', () => {
  let component: RbVcollapseComponent;
  let fixture: ComponentFixture<RbVcollapseComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbVcollapseComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbVcollapseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
