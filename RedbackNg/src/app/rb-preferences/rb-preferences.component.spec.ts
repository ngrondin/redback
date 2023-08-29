import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbPreferencesComponent } from './rb-preferences.component';

describe('RbPreferencesComponent', () => {
  let component: RbPreferencesComponent;
  let fixture: ComponentFixture<RbPreferencesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbPreferencesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbPreferencesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
