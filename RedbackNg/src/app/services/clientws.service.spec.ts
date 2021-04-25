import { TestBed } from '@angular/core/testing';

import { ClientwsService } from './clientws.service';

describe('ClientwsService', () => {
  let service: ClientwsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ClientwsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
