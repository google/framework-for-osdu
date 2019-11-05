package com.osdu.service.google;

import com.google.api.client.googleapis.media.MediaHttpUploader.UploadState;
import com.google.api.client.http.GenericUrl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class UploadProgress {

  Long totalBytesServerReceived = 0L;
  UploadState uploadState;
  Long mediaContentLength;
  GenericUrl url;

  /**
   * Constructor for UploadProgress.
   *
   * @param mediaContentLength length of content byte array
   * @param uploadState        state of upload process
   * @param url                url to upload file to
   */
  public UploadProgress(Long mediaContentLength, UploadState uploadState, GenericUrl url) {
    this.mediaContentLength = mediaContentLength;
    this.uploadState = uploadState;
    this.url = url;
  }

  /**
   * Updates file upload state and log it.
   *
   * @param uploadState new upload state
   */
  public void updateProgressState(UploadState uploadState) {
    this.uploadState = uploadState;
    switch (uploadState) {
      case INITIATION_STARTED:
        log.debug("Initiation of upload started. Url - " + url.toString());
        break;
      case INITIATION_COMPLETE:
        log.debug("Initiation of upload completed. Url - " + url.toString());
        break;
      case MEDIA_IN_PROGRESS:
        log.debug("Upload in progress. Url - " + url.toString() +
            "\nUpload percentage: " + getProgress());
        break;
      case MEDIA_COMPLETE:
        log.debug("Upload Completed! Url - " + url.toString());
        break;
      default:
        log.debug("Upload status: " + uploadState);
    }
  }

  private double getProgress() {
    return mediaContentLength == 0 ? 0 : (double) totalBytesServerReceived / mediaContentLength;
  }
}
