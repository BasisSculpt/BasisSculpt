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

import java.util.HashMap;
import java.util.Map;

/**
 * BasisSculpt - Sculpting Gaussian Basis Sets via Norm Control
 *
 * @author M. Macernis
 * @version 2025.4.v1
 */
public class Dic {

    private static final Map<String, String> messages = new HashMap<>();

    static {
        messages.put("ERROR_FILE_ARGUMENT", "ERROR: --input <file> argument is required.");
        messages.put("ERROR_FILE_NOT_FOUND", "ERROR: Input file not found or not readable: %s");
        messages.put("ERROR_THRESHOLD_REQUIRED", "ERROR: --threshold <float> argument is required (e.g., 1e-5).");
        messages.put("ERROR_STRATEGY_REQUIRED", "ERROR: --strategy <mode> is required (must be 'best-first' or 'full-scan').");
        messages.put("ERROR_LEVEL_REQUIRED_FOR_BEST_FIRST", "ERROR: --level <N> is required only when using strategy 'best-first'.");
        messages.put("ERROR_OUTPUT_FILE", "ERROR: Cannot open output file: %s");
        messages.put("ERROR_LOG_FILE", "ERROR: Cannot open log file: %s");
        messages.put("ERROR_REPORT_FILE", "ERROR: Cannot open report file: %s");
        messages.put("ERROR_INPUT_FILE_ATOMS", "ERROR: Atoms not found in input file: %s");
        messages.put("INFO_FULL_NORM", "Full norm: %.10f");
        messages.put("INFO_PARTIAL_NORM", "Partial norm without primitive #%d: %.10f");
        messages.put("INFO_NO_REDUCTION_POSSIBLE", "Only one primitive - nothing to reduce.");
        messages.put("INFO_PARTIAL_NORM_DELTA", "Without #%d: %.10f | D=%.1e (%.4f%%)");
        messages.put("INFO_REMOVE_ENTRY", "Removed #%2d (alfa = %.10f): norm = %.10f, loss = %.4f%%, contribution = %.4f%%");
        messages.put("INFO_REMOVE_HEADE_REPORT", "Rem. \t alfa \t\t\t norm \t\t\tloss,% \t contr.,%");
        messages.put("INFO_REMOVE_ENTRY_REPORT", "#%2d \t %.10f \t %.10f \t%.4f \t\t %.4f");
        messages.put("INFO_SEPARATOR", "-----------------");
        messages.put("INFO_VERSION_TAG", "Version tag: %s");
        messages.put("INFO_FIXED_BASIS_NAME", "Fixed basis name: %s");
        messages.put("INFO_NORMALIZATION_SEPARATOR", "----------------------");
        messages.put("INFO_FULL_NORMALIZED_NORM", "Full norm after normalization: %.10f");
        messages.put("INFO_VERBOSE_NORMALIZATION", "Full numbers:");
        messages.put("INFO_MATH_CONTEXT_PRECISION", "Selected math precision: %d digits");
        messages.put("WARN_PROJECTION_FAILED", "!! Projection normalization failed: negative discriminant. Solving numerically...");
        messages.put("INFO_OPTIMIZED_S2", "Optimized s2 = %.8f (error = %.2e)");
        messages.put("WARN_SINGLE_SIGN_GROUP", "‼ Cannot project-normalize: only one sign group present.");
        messages.put("WARN_NON_GBS_INPUT", "WARNING: the input does not fit the GBS format! Ignoring and attempting to parse. Check results!");
    }

    public static String getMsg(String key) {
        return messages.getOrDefault(key, "???" + key + "???");
    }

    public static String getMsg(String key, Object... args) {
        String template = getMsg(key);
        return String.format(template, args);
    }
}
