import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbTextConsoleComponent } from './rb-text-console.component';

describe('RbTextConsoleComponent', () => {
  let component: RbTextConsoleComponent;
  let fixture: ComponentFixture<RbTextConsoleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbTextConsoleComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbTextConsoleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
