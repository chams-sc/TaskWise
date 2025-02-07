/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package com.bin4rybros.sdk.cubism.framework.motion;

/**
 * モーション再生終了コールバック
 */
public interface IFinishedMotionCallback {
    void execute(ACubismMotion motion);
}
