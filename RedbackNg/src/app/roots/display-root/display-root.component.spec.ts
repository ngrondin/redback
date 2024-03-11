import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DisplayRootComponent } from './display-root.component';

describe('DisplayRootComponent', () => {
  let component: DisplayRootComponent;
  let fixture: ComponentFixture<DisplayRootComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DisplayRootComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DisplayRootComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
