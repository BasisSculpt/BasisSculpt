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
import basissculpt.bin.Report;
import basissculpt.etc.ArgsGet;
import basissculpt.etc.Dic;
import basissculpt.etc.Settings;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * BasisSculpt - Sculpting Gaussian Basis Sets via Norm Control
 *
 * @author M. Macernis
 * @version 2025.4.v1
 */
public class BlockAnalysis {

    private final String blockText;
    private final List<PrimitiveBig> basis;
    private final LogFile log = LogFile.getInstance();
    private final Report report = Report.getInstance();
    private BigDecimal normFull;
    private static final MathContext mc = Settings.getInstance().getMC();

    public BlockAnalysis(String blockText) {
        this.blockText = blockText;
        basis = parseTextBlock(blockText);
    }

    public void fullNorm() {
        // Apskaičiuojame bendrą absoliučių įnašų sumą
        BigDecimal totalContribution = BigDecimal.ZERO;
        List<BigDecimal> individualContributions = new ArrayList<>();
        for (PrimitiveBig p : basis) {
            BigDecimal contribution = p.c.multiply(p.N, mc).abs(mc);
            individualContributions.add(contribution);
            totalContribution = totalContribution.add(contribution, mc);
        }

        ContractedBig full = new ContractedBig(basis);
        BigDecimal rMax = Tools.suggestRmax(full);
        int steps = Tools.suggestSteps(rMax);
        normFull = full.norm(BigDecimal.ZERO, rMax, steps);
        log.println(Dic.getMsg("INFO_FULL_NORM", normFull.doubleValue()));
    }

    public BigDecimal partialNormExcluding(int indexToIgnore) {
        if (indexToIgnore < 0 || indexToIgnore >= basis.size()) {
            throw new IllegalArgumentException("Invalid index to ignore: " + indexToIgnore);
        }

        List<PrimitiveBig> reduced = new ArrayList<>();
        for (int i = 0; i < basis.size(); i++) {
            if (i != indexToIgnore) {
                reduced.add(basis.get(i));
            }
        }

        ContractedBig contracted = new ContractedBig(reduced);
        BigDecimal rMax = Tools.suggestRmax(contracted);
        int steps = Tools.suggestSteps(rMax);
        BigDecimal norm = contracted.norm(BigDecimal.ZERO, rMax, steps);

        log.println(Dic.getMsg("INFO_PARTIAL_NORM", indexToIgnore + 1, norm.doubleValue()));
        return norm;
    }

    public void allPartialNorms() {
        if (basis.size() <= 1) {
            log.println(Dic.getMsg("INFO_NO_REDUCTION_POSSIBLE"));
            return;
        }

        // Pilna norma
        BigDecimal totalContribution = BigDecimal.ZERO;
        List<BigDecimal> individualContributions = new ArrayList<>();
        for (PrimitiveBig p : basis) {
            BigDecimal contribution = p.c.multiply(p.N, mc).abs(mc);
            individualContributions.add(contribution);
            totalContribution = totalContribution.add(contribution, mc);
        }

        ContractedBig full = new ContractedBig(basis);
        BigDecimal rMax = Tools.suggestRmax(full);
        int steps = Tools.suggestSteps(rMax);
        normFull = full.norm(BigDecimal.ZERO, rMax, steps);
        log.println(Dic.getMsg("INFO_FULL_NORM", normFull.doubleValue()));
        report.println(Dic.getMsg("INFO_FULL_NORM", normFull.doubleValue()));
        report.println(Dic.getMsg("INFO_REMOVE_HEADE_REPORT"));

        for (int i = 0; i < basis.size(); i++) {
            // Reduced basis without i component
            List<PrimitiveBig> reduced = new ArrayList<>();
            for (int j = 0; j < basis.size(); j++) {
                if (j != i) {
                    reduced.add(basis.get(j));
                }
            }

            ContractedBig contracted = new ContractedBig(reduced);
            BigDecimal rMaxRed = Tools.suggestRmax(contracted);
            int stepsRed = Tools.suggestSteps(rMaxRed);
            BigDecimal normPartial = contracted.norm(BigDecimal.ZERO, rMaxRed, stepsRed);

            double loss = 0.0;
            if (normFull.doubleValue() > 1e-12) {
                loss = normFull.subtract(normPartial, mc)
                        .divide(normFull, mc)
                        .multiply(BigDecimal.valueOf(100), mc)
                        .doubleValue();
            }

            double inasas = 0.0;
            if (totalContribution.signum() != 0) {
                inasas = individualContributions.get(i)
                        .divide(totalContribution, mc)
                        .multiply(BigDecimal.valueOf(100), mc)
                        .doubleValue();
            }

            log.println(Dic.getMsg(
                    "INFO_REMOVE_ENTRY",
                    i + 1,
                    basis.get(i).alpha.doubleValue(),
                    normPartial.doubleValue(),
                    loss,
                    inasas
            ));

            report.println(Dic.getMsg(
                    "INFO_REMOVE_ENTRY_REPORT",
                    i + 1,
                    basis.get(i).alpha.doubleValue(),
                    normPartial.doubleValue(),
                    loss,
                    inasas
            ));;
            
        }
    }

    public List<PrimitiveBig> normalizeBlock() {
        log.println(Dic.getMsg("INFO_NORMALIZATION_SEPARATOR"));
        long negative = basis.stream()
                .map(p -> p.c)
                .filter(c -> c.compareTo(BigDecimal.ZERO) < 0)
                .count();

        ContractedBig full = new ContractedBig(basis);
        BigDecimal rMax = Tools.suggestRmax(full);
        int steps = Tools.suggestSteps(rMax);

        ContractedBig fullNormalized;
        if (negative == 0) {
            fullNormalized = full.normalize(BigDecimal.ZERO, rMax, steps);
        } else {
            fullNormalized = full.normalizeViaProjection(BigDecimal.ZERO, rMax, steps);
        }

        BigDecimal normFullNormalized = fullNormalized.norm(BigDecimal.ZERO, rMax, steps);
        log.println(Dic.getMsg("INFO_FULL_NORMALIZED_NORM", normFullNormalized.doubleValue()));

        List<PrimitiveBig> fullNormalizedOrdered = fullNormalized.getPrimitivesInOriginalOrder(full.originalAlphaOrder);
        for (PrimitiveBig p : fullNormalizedOrdered) {
            String alphaStr = String.format("%.6E", p.alpha.doubleValue()).replace('E', 'D');
            String cStr = String.format("%.6E", p.c.doubleValue()).replace('E', 'D');
            String line = String.format("      %s          %s", alphaStr, cStr);
            if (p.c.compareTo(BigDecimal.ZERO) >= 0) {
                line = line.replace("          ", "           "); // atitinka vizualų stumdymą
            }
            log.println(line);
        }

        if (ArgsGet.getInstance().isKey("verbose")) {
            log.println(Dic.getMsg("INFO_VERBOSE_NORMALIZATION"));
            for (PrimitiveBig p : fullNormalizedOrdered) {
                log.println("Alfa: " + p.alpha);
                log.println("C: " + p.c);
            }
        }

        log.println(Dic.getMsg("INFO_NORMALIZATION_SEPARATOR"));
        return fullNormalizedOrdered;
    }

    private static List<PrimitiveBig> parseTextBlock(String text) {
        List<PrimitiveBig> primitives = new ArrayList<>();
        String[] lines = text.strip().split("\\R");
        for (String line : lines) {
            if (line.startsWith("#") || line.trim().isEmpty()) {
                continue;
            }
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 2) {
                BigDecimal alpha = new BigDecimal(parts[0].replace("D", "E"), mc);
                BigDecimal c = new BigDecimal(parts[1].replace("D", "E"), mc);
                primitives.add(new PrimitiveBig(alpha, c));
            }
        }
        return primitives;
    }

}
