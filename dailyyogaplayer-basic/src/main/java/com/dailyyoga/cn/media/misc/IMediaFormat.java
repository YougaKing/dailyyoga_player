/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dailyyoga.cn.media.misc;

public interface IMediaFormat {
    // Common
    String KEY_IJK_CODEC_LONG_NAME_UI = "ijk-codec-long-name-ui";
    String KEY_IJK_CODEC_NAME_UI = "ijk-codec-name-ui";
    String KEY_IJK_BIT_RATE_UI = "ijk-bit-rate-ui";

    // Video
    String KEY_IJK_CODEC_PROFILE_LEVEL_UI = "ijk-profile-level-ui";
    String KEY_IJK_CODEC_PIXEL_FORMAT_UI = "ijk-pixel-format-ui";
    String KEY_IJK_RESOLUTION_UI = "ijk-resolution-ui";
    String KEY_IJK_FRAME_RATE_UI = "ijk-frame-rate-ui";

    // Audio
    String KEY_IJK_SAMPLE_RATE_UI = "ijk-sample-rate-ui";
    String KEY_IJK_CHANNEL_UI = "ijk-channel-ui";

    // Codec
    String CODEC_NAME_H264 = "h264";
    
    // Common keys
    String KEY_MIME = "mime";

    // Video Keys
    String KEY_WIDTH = "width";
    String KEY_HEIGHT = "height";

    String getString(String name);

    int getInteger(String name);
}
