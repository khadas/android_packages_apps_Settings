/*
 * Copyright 2025 Rockchip Limited
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

package com.android.settings.utils;

//---------rk-code----------
import java.util.ArrayList;
import java.util.List;

/**
 * input mode for touch or remote control
 */
public class InputModeManager {
    private static InputModeManager instance;
    private InputMode currentMode = InputMode.REMOTE;
    private final List<OnInputModeChangeListener> listeners = new ArrayList<>();

    private InputModeManager() {

    }

    public static synchronized InputModeManager getInstance() {
        if (instance == null) {
            instance = new InputModeManager();
        }
        return instance;
    }

    public InputMode getCurrentMode() {
        return currentMode;
    }

    public boolean isInRemoteInputMode() {
        return currentMode == InputMode.REMOTE;
    }

    public void setInputMode(InputMode mode) {
        if (currentMode != mode) {
            currentMode = mode;
            for (OnInputModeChangeListener l : listeners) {
                l.onInputModeChanged(mode);
            }
        }
    }

    public void registerListener(OnInputModeChangeListener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    public void unregisterListener(OnInputModeChangeListener l) {
        listeners.remove(l);
    }

    public interface OnInputModeChangeListener {
        void onInputModeChanged(InputMode newMode);
    }

    public enum InputMode {
        TOUCH,
        REMOTE
    }
}
//--------------------------