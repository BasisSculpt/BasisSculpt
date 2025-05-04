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
package basissculpt.lib;

import basissculpt.etc.Settings;
import ch.obermuhlner.math.big.BigDecimalMath;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * BasisSculpt - Sculpting Gaussian Basis Sets via Norm Control
 *
 * @author M. Macernis
 * @version 2025.4.v1
 */
public class PrimitiveBig {

    private static final MathContext mc = Settings.getInstance().getMC();
    private static final BigDecimal PI = new BigDecimal(Math.PI, mc);
    private static final BigDecimal TWO = new BigDecimal("2", mc);
    private static final BigDecimal THREE = new BigDecimal("3", mc);
    private static final BigDecimal FOUR = new BigDecimal("4", mc);
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal EPS = new BigDecimal("1e-10");

    public BigDecimal alpha;
    public BigDecimal c;
    public BigDecimal N;

    public PrimitiveBig(BigDecimal alpha, BigDecimal c) {
        this.alpha = alpha;
        this.c = c;
        BigDecimal ratio = TWO.multiply(alpha, mc).divide(PI, mc);
        this.N = BigDecimalMath.pow(ratio, THREE.divide(FOUR, mc), mc); // (2*alpha/pi)^(3/4)
    }

    public BigDecimal value(BigDecimal r) {
        BigDecimal r2 = r.multiply(r, mc);
        BigDecimal exponent = alpha.multiply(r2, mc).negate();
        double expVal = Math.exp(exponent.doubleValue());
        return c.multiply(N, mc).multiply(BigDecimal.valueOf(expVal), mc);
    }

    public PrimitiveBig scaled(BigDecimal scale) {
        return new PrimitiveBig(alpha, c.multiply(scale, mc));
    }

    public PrimitiveBig rescaled(BigDecimal newC) {
        return new PrimitiveBig(alpha, newC);
    }

    public static List<PrimitiveBig> scaleAll(List<PrimitiveBig> list, BigDecimal factor) {
        return list.stream().map(p -> p.scaled(factor)).collect(Collectors.toList());
    }

    public static List<PrimitiveBig> renormalize(List<PrimitiveBig> list, BigDecimal targetNorm, Function<List<PrimitiveBig>, BigDecimal> normFunction) {
        BigDecimal currentNorm = normFunction.apply(list);
        BigDecimal scale = targetNorm.divide(BigDecimalMath.sqrt(currentNorm, mc), mc);
        return scaleAll(list, scale);
    }

    public static List<PrimitiveBig> renormalizeIfNeeded(List<PrimitiveBig> list, BigDecimal targetNorm, Function<List<PrimitiveBig>, BigDecimal> normFunction) {
        BigDecimal currentNorm = normFunction.apply(list);
        if (currentNorm.subtract(targetNorm, mc).abs(mc).compareTo(EPS) > 0) {
            BigDecimal scale = targetNorm.divide(BigDecimalMath.sqrt(currentNorm, mc), mc);
            return scaleAll(list, scale);
        } else {
            return list;
        }
    }

    public static ContractedBig renormalizedContracted(ContractedBig original, BigDecimal rMin, BigDecimal rMax, int steps) {
        Function<List<PrimitiveBig>, BigDecimal> normFunc = list -> new ContractedBig(list).norm(rMin, rMax, steps);
        List<PrimitiveBig> renorm = renormalizeIfNeeded(original.primitives, ONE, normFunc);
        return new ContractedBig(renorm);
    }

}
