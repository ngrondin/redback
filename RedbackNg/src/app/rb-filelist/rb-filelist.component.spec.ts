import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbFilelistComponent } from './rb-filelist.component';

describe('RbFilelistComponent', () => {
  let component: RbFilelistComponent;
  let fixture: ComponentFixture<RbFilelistComponent>;

  beforeEach(async(() => {
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
