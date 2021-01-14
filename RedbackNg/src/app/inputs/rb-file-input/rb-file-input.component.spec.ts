import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbFileInputComponent } from './rb-file-input.component';

describe('RbFileInputComponent', () => {
  let component: RbFileInputComponent;
  let fixture: ComponentFixture<RbFileInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbFileInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbFileInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
