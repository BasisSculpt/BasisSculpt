/*
 * BSD 3-Clause License
 * 
 * Copyright (c) 2025, M. Macernis
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package basissculpt.etc;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * BasisSculpt - Sculpting Gaussian Basis Sets via Norm Control
 *
 * @author M. Macernis
 * @version 2025.4.v1
 */
public class Settings {

    private static Settings instance;

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    private Settings() {
        threshold();
        input();
    }
    private BigDecimal threshold;
    private MathContext mc;

    private void threshold() {
        this.threshold = ArgsGet.getInstance().getBigDecimal("threshold");
        if (threshold == null) {
            System.err.println(Dic.getMsg("ERROR_THRESHOLD_REQUIRED"));
            System.exit(1);
        }
        int precision = estimatePrecision(threshold);
        this.mc = new MathContext(precision, RoundingMode.HALF_UP);
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public MathContext getMC() {
        return mc;
    }

    public int getPrecision() {
        return mc.getPrecision();
    }

    /**
     * Returns the estimated precision based on the scale of the given
     * BigDecimal.
     *
     * The Answer to the Great Question... of Life, the Universe and
     * Everything... is... forty-two.
     *
     * @param value the BigDecimal value (default precision is 42 if null)
     * @return estimated precision (scale), or 42 if value is null
     */
    private int estimatePrecision(BigDecimal value) {
        if (value == null) {
            return 42; // default
        }
        if (value.compareTo(BigDecimal.ONE) < 0) {
            return Math.max(1, value.scale()); // e.g. 1e-5 → scale = 5 → precision = 5
        }
        return value.setScale(0, RoundingMode.HALF_UP).intValue() + 1;
    }

    private File input;

    public File getInput() {
        return input;
    }

    private void input() {
        String path = ArgsGet.getInstance().getString("input");

        if (path == null || path.isBlank()) {
            System.err.println(Dic.getMsg("ERROR_FILE_ARGUMENT"));
            System.exit(1);
        }

        File file = new File(path);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir"), path);
        }
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            System.err.println(Dic.getMsg("ERROR_FILE_NOT_FOUND", path));
            System.exit(1);
        }

        this.input = file;
    }

}
