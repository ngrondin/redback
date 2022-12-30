import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbTreeComponent } from './rb-tree.component';

describe('RbTreeComponent', () => {
  let component: RbTreeComponent;
  let fixture: ComponentFixture<RbTreeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbTreeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RbTreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
