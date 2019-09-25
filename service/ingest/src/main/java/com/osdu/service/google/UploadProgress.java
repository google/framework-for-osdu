package com.osdu.service.google;

import com.google.api.client.googleapis.media.MediaHttpUploader.UploadState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class UploadProgress {

  public UploadProgress(Long mediaContentLength, UploadState uploadState) {
    this.mediaContentLength = mediaContentLength;
    this.uploadState = uploadState;
  }

  Long totalBytesServerReceived = 0L;
  UploadState uploadState;
  Long mediaContentLength;

  public void updateProgressState(UploadState uploadState) {
    this.uploadState = uploadState;
    switch (uploadState) {
      case INITIATION_STARTED:
        log.debug("Initiation Started");
        break;
      case INITIATION_COMPLETE:
        log.debug("Initiation Completed");
        break;
      case MEDIA_IN_PROGRESS:
        log.debug("Upload in progress");
        log.debug("Upload percentage: " + getProgress());
        break;
      case MEDIA_COMPLETE:
        log.debug("Upload Completed!");
        break;
      default:
        log.debug("Upload status: " + uploadState);
    }
  }

  double getProgress() {
    return mediaContentLength == 0 ? 0 : (double) totalBytesServerReceived / mediaContentLength;
  }
}
