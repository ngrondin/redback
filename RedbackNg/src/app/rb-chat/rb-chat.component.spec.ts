import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbChatComponent } from './rb-chat.component';

describe('RbChatComponent', () => {
  let component: RbChatComponent;
  let fixture: ComponentFixture<RbChatComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbChatComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
