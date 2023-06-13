import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbVseperatorComponent } from './rb-vseparator.component';

describe('RbVseperatorComponent', () => {
  let component: RbVseperatorComponent;
  let fixture: ComponentFixture<RbVseperatorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbVseperatorComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbVseperatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
