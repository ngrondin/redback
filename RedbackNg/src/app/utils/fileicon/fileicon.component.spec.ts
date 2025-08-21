import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FileiconComponent } from './fileicon.component';

describe('FileiconComponent', () => {
  let component: FileiconComponent;
  let fixture: ComponentFixture<FileiconComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FileiconComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FileiconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
