/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
