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

import java.math.BigDecimal;
import java.util.Map;

/**
 * BasisSculpt - Sculpting Gaussian Basis Sets via Norm Control
 *
 * @author M. Macernis
 * @version 2025.4.v1
 */
public class ArgsGet {

    private static ArgsGet instance;
    private final Map<String, String> map;

    public ArgsGet(Args args) {
        this.map = args.getMapArgs();
        this.instance = this;
    }

    public static ArgsGet getInstance() {
        return instance;
    }

    public String getString(String key) {
        return map.get(key);
    }

    public String getStringNotNull(String key) {
       if( map.get(key)== null) return "";
       return map.get(key);
    }
    
    public Integer getInt(String key) {
        String value = map.get(key);
        try {
            return value != null ? Integer.valueOf(value) : null;
        } catch (NumberFormatException ex) {
        }
        return null;
    }

    public Double getDouble(String key) {
        String value = map.get(key);
        try {
            return value != null ? Double.valueOf(value) : null;
        } catch (NumberFormatException ex) {
        }
        return null;
    }

    public boolean isKey(String key) {
        return map.containsKey(key);
    }

    public BigDecimal getBigDecimal(String key) {
        String value = map.get(key);
        try {
            return value != null ? new BigDecimal(value) : null;
        } catch (NumberFormatException ex) {
        }
        return null;
    }
}
