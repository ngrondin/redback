import { TestBed } from '@angular/core/testing';

import { NlactionService } from '../nlaction.service';

describe('NlactionService', () => {
  let service: NlactionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NlactionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
