import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbFileviewerComponent } from './rb-fileviewer.component';

describe('RbFileviewerComponent', () => {
  let component: RbFileviewerComponent;
  let fixture: ComponentFixture<RbFileviewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbFileviewerComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbFileviewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
