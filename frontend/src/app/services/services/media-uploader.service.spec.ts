import { TestBed } from '@angular/core/testing';

import { MediaUploaderService } from './media-uploader.service';

describe('MediaUploaderService', () => {
  let service: MediaUploaderService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MediaUploaderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
