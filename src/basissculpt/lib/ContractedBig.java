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

import basissculpt.bin.LogFile;
import basissculpt.etc.Dic;
import basissculpt.etc.Settings;
import ch.obermuhlner.math.big.BigDecimalMath;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

/**
 * BasisSculpt - Sculpting Gaussian Basis Sets via Norm Control
 *
 * @author M. Macernis
 * @version 2025.4.v1
 */
public class ContractedBig {

    private static final MathContext mc = Settings.getInstance().getMC();
    private static final BigDecimal FOUR_PI = new BigDecimal(4 * Math.PI, mc);

    public List<PrimitiveBig> primitives;
    public List<BigDecimal> originalAlphaOrder;
    
    private static LogFile log = LogFile.getInstance();

    public ContractedBig(List<PrimitiveBig> primitives) {
        this.primitives = primitives;
        this.originalAlphaOrder = new ArrayList<>();
        for (PrimitiveBig p : primitives) {
            this.originalAlphaOrder.add(p.alpha);
        }
    }

    public BigDecimal phi(BigDecimal r) {
        return primitives.stream()
                .map(p -> p.value(r))
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b, mc));
    }

    public BigDecimal norm(BigDecimal rMin, BigDecimal rMax, int steps) {
        BigDecimal dr = rMax.subtract(rMin, mc).divide(BigDecimal.valueOf(steps), mc);
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i <= steps; i++) {
            BigDecimal r = rMin.add(dr.multiply(BigDecimal.valueOf(i), mc), mc);
            BigDecimal val = phi(r);
            BigDecimal term = val.multiply(val, mc)
                                 .multiply(FOUR_PI, mc)
                                 .multiply(r.pow(2, mc), mc)
                                 .multiply(dr, mc);
            sum = sum.add(term, mc);
        }
        return sum;
    }

    public ContractedBig normalize(BigDecimal rMin, BigDecimal rMax, int steps) {
    BigDecimal currentNorm = this.norm(rMin, rMax, steps);
    BigDecimal scale = BigDecimal.ONE.divide(BigDecimalMath.sqrt(currentNorm, mc), mc);
    List<PrimitiveBig> scaled = new ArrayList<>();
    for (PrimitiveBig p : primitives) {
        scaled.add(p.scaled(scale));
    }
    return new ContractedBig(scaled);
}

    
    public ContractedBig normalizeViaProjection(BigDecimal rMin, BigDecimal rMax, int steps) {
        List<PrimitiveBig> positive = new ArrayList<>();
        List<PrimitiveBig> negative = new ArrayList<>();

        for (PrimitiveBig p : primitives) {
            if (p.c.compareTo(BigDecimal.ZERO) >= 0) {
                positive.add(p);
            } else {
                negative.add(p);
            }
        }

        if (positive.isEmpty() || negative.isEmpty()) {
            log.getInstance().println(Dic.getMsg("WARN_SINGLE_SIGN_GROUP"));
            return this;
        }

        ContractedBig phiPlus = new ContractedBig(positive);
        ContractedBig phiMinus = new ContractedBig(negative);

        BigDecimal A = phiPlus.norm(rMin, rMax, steps);
        BigDecimal B = phiMinus.norm(rMin, rMax, steps);
        BigDecimal C = phiPlus.overlapWith(phiMinus, rMin, rMax, steps);

        BigDecimal a = B;
        BigDecimal b = C.multiply(BigDecimal.valueOf(2), mc);
        BigDecimal c = A.subtract(BigDecimal.ONE, mc);

        BigDecimal discriminant = b.pow(2, mc).subtract(a.multiply(c, mc).multiply(BigDecimal.valueOf(4), mc), mc);

        List<PrimitiveBig> result;

        if (discriminant.compareTo(BigDecimal.ZERO) < 0) {
            log.getInstance().println(Dic.getMsg("WARN_PROJECTION_FAILED"));
            BigDecimal s1 = BigDecimal.ONE;
            BigDecimal s2 = minimizeS2Big(positive, negative, rMin, rMax, steps, new BigDecimal("1e-6"));
            result = mergeScaled(positive, negative, s1, s2);
        } else {
            BigDecimal sqrtD = BigDecimalMath.sqrt(discriminant, mc); 
            BigDecimal twoA = a.multiply(BigDecimal.valueOf(2), mc);

            BigDecimal s2a = b.negate().add(sqrtD, mc).divide(twoA, mc);
            BigDecimal s2b = b.negate().subtract(sqrtD, mc).divide(twoA, mc);
            BigDecimal s2 = s2a.abs().compareTo(s2b.abs()) < 0 ? s2a : s2b;

            result = mergeScaled(positive, negative, BigDecimal.ONE, s2);
        }

        return PrimitiveBig.renormalizedContracted(new ContractedBig(result), rMin, rMax, steps);
    }

    private static BigDecimal minimizeS2Big(List<PrimitiveBig> positive, List<PrimitiveBig> negative,
                                            BigDecimal rMin, BigDecimal rMax, int steps, BigDecimal tolerance) {
        BigDecimal left = new BigDecimal("-10.0", mc);
        BigDecimal right = new BigDecimal("10.0", mc);
        BigDecimal goldenRatio = BigDecimalMath.sqrt(new BigDecimal("5"), mc).subtract(BigDecimal.ONE, mc)
                .divide(new BigDecimal("2"), mc);

        BigDecimal x1 = right.subtract(goldenRatio.multiply(right.subtract(left, mc), mc), mc);
        BigDecimal x2 = left.add(goldenRatio.multiply(right.subtract(left, mc), mc), mc);

        BigDecimal bestS2 = x1;
        BigDecimal bestError = new BigDecimal(Double.MAX_VALUE, mc);

        for (int i = 0; i < 100; i++) {
            ContractedBig c1 = new ContractedBig(mergeScaled(positive, negative, BigDecimal.ONE, x1));
            ContractedBig c2 = new ContractedBig(mergeScaled(positive, negative, BigDecimal.ONE, x2));

            BigDecimal err1 = c1.norm(rMin, rMax, steps).subtract(BigDecimal.ONE, mc).abs(mc);
            BigDecimal err2 = c2.norm(rMin, rMax, steps).subtract(BigDecimal.ONE, mc).abs(mc);

            if (err1.compareTo(err2) < 0) {
                right = x2;
                x2 = x1;
                x1 = right.subtract(goldenRatio.multiply(right.subtract(left, mc), mc), mc);
                bestS2 = x1;
                bestError = err1;
            } else {
                left = x1;
                x1 = x2;
                x2 = left.add(goldenRatio.multiply(right.subtract(left, mc), mc), mc);
                bestS2 = x2;
                bestError = err2;
            }

            if (right.subtract(left, mc).abs(mc).compareTo(tolerance) < 0) {
                break;
            }
        }
        log.getInstance().println(Dic.getMsg("INFO_OPTIMIZED_S2", bestS2.doubleValue(), bestError.doubleValue()));
        return bestS2;
    }

    private static List<PrimitiveBig> mergeScaled(List<PrimitiveBig> pos, List<PrimitiveBig> neg,
                                                  BigDecimal s1, BigDecimal s2) {
        List<PrimitiveBig> result = new ArrayList<>();
        for (PrimitiveBig p : pos) result.add(p.scaled(s1));
        for (PrimitiveBig p : neg) result.add(p.scaled(s2));
        return result;
    }  

    public BigDecimal overlapWith(ContractedBig other, BigDecimal rMin, BigDecimal rMax, int steps) {
        BigDecimal dr = rMax.subtract(rMin, mc).divide(BigDecimal.valueOf(steps), mc);
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i <= steps; i++) {
            BigDecimal r = rMin.add(dr.multiply(BigDecimal.valueOf(i), mc), mc);
            BigDecimal val1 = this.phi(r);
            BigDecimal val2 = other.phi(r);
            BigDecimal term = val1.multiply(val2, mc)
                                  .multiply(FOUR_PI, mc)
                                  .multiply(r.pow(2, mc), mc)
                                  .multiply(dr, mc);
            sum = sum.add(term, mc);
        }
        return sum;
    }

    public List<PrimitiveBig> getPrimitivesInOriginalOrder(List<BigDecimal> alphaOrder) {
        List<PrimitiveBig> ordered = new ArrayList<>();
        for (BigDecimal alpha : alphaOrder) {
            for (PrimitiveBig p : primitives) {
                if (p.alpha.subtract(alpha, mc).abs(mc).compareTo(new BigDecimal("1e-12")) < 0) {
                    ordered.add(p);
                    break;
                }
            }
        }
        return ordered;
    }
} 