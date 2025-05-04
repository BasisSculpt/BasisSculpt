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
package basissculpt.lib.read;

import basissculpt.bin.LogFile;
import basissculpt.bin.Output;
import basissculpt.bin.Report;
import basissculpt.etc.ArgsGet;
import basissculpt.etc.Dic;
import basissculpt.lib.BlockAnalysis;
import basissculpt.lib.PrimitiveBig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BasisSculpt - Sculpting Gaussian Basis Sets via Norm Control
 *
 * @author M. Macernis
 * @version 2025.4.v1
 */
public class BasisPrinter {

    private final List<BasisParser.AtomSection> atoms;

    public BasisPrinter(List<BasisParser.AtomSection> atoms) {
        this.atoms = atoms;
    }

    public void run() {
        LogFile log = LogFile.getInstance();
        Output out = Output.getInstance();
        Report report = Report.getInstance();
        out.println("! " + ArgsGet.getInstance().getStringNotNull("fixed-basis-name")
                + " " + ArgsGet.getInstance().getStringNotNull("version-tag")
        );
        for (BasisParser.AtomSection atom : atoms) {
            log.println(Dic.getMsg("INFO_SEPARATOR"));
            report.println(Dic.getMsg("INFO_SEPARATOR"));
            log.println("Atom: " + atom.atom);
            report.println("Atom: " + atom.atom);
            if (ArgsGet.getInstance().getStringNotNull("output-gbs-format").toLowerCase().contains("no")) {
                out.println(atom.atom);
            } else {
                out.println("-" + atom.atom);
            }
            log.println(Dic.getMsg("INFO_SEPARATOR"));
            report.println(Dic.getMsg("INFO_SEPARATOR"));

            Block bAll = new Block("ALL", "joined");
            for (BasisParser.Block block : atom.blocks) {
                log.println("  Block type: " + block.type);
                report.println("  Block type: " + block.type);
                log.println("  Header: " + block.header);
                out.println(block.type + " " + block.header);

                Block b = new Block(block.type, block.header);//

                if (block.type.length() == 1) {
                    for (String line : block.lines) {
                        log.println("    " + line);
                        b.addLine(line);

                        bAll.addLine(line);
                    }
                } else {
                    Map<String, List<String>> split = BasisParser.expandBlock(block);
                    List<List<PrimitiveBig>> pAll = new ArrayList<>();
                    for (Map.Entry<String, List<String>> entry : split.entrySet()) {
                        log.println("    Subblock: " + entry.getKey());
                        b = new Block(block.type, block.header);//
                        for (String l : entry.getValue()) {
                            log.println("      " + l);
                            b.addLine(l);

                            bAll.addLine(l);
                        }
                        new BlockAnalysis(b.getBlockData()).allPartialNorms();
                        if (ArgsGet.getInstance().isKey("normalize")) {
                            List<PrimitiveBig> p = new BlockAnalysis(b.getBlockData()).normalizeBlock();
                            pAll.add(p);

                        }
                    }
                    List<PrimitiveBig> primitives = pAll.get(0);
                    int components = pAll.size(); // pvz. SP → 2, SPD → 3

                    for (int i = 0; i < primitives.size(); i++) {
                        StringBuilder sb = new StringBuilder();

                        // alfa
                        double alphaVal = pAll.get(0).get(i).alpha.doubleValue();
                        sb.append(String.format("%16s", String.format("%.6E", alphaVal).replace('E', 'D')));

                        // all c values
                        for (int j = 0; j < components; j++) {
                            double cVal = pAll.get(j).get(i).c.doubleValue();
                            sb.append(String.format("%16s", String.format("%.6E", cVal).replace('E', 'D')));
                        }

                        out.println(sb.toString());
                    }

                    continue;
                }
                new BlockAnalysis(b.getBlockData()).allPartialNorms();
                if (ArgsGet.getInstance().isKey("normalize")) {
                    List<PrimitiveBig> primitives = new BlockAnalysis(b.getBlockData()).normalizeBlock();

                    for (int i = 0; i < primitives.size(); i++) {
                        StringBuilder sb = new StringBuilder();

                        Double threshold = ArgsGet.getInstance().getDouble("output-threshold");
                        if (threshold == null) {
                            // α
                            double alphaVal = primitives.get(i).alpha.doubleValue();
                            sb.append(String.format("%16s", String.format("%.6E", alphaVal).replace('E', 'D')));

                            double cVal = primitives.get(i).c.doubleValue();
                            sb.append(String.format("%16s", String.format("%.6E", cVal).replace('E', 'D')));

                        } else {
                            int decimals = Math.abs((int) Math.round(Math.log10(threshold)));
                            // α
                            double alphaVal = primitives.get(i).alpha.doubleValue();
                            sb.append(formatAlwaysZeroD(alphaVal, decimals));

                            sb.append("   ");//separator

                            double cVal = primitives.get(i).c.doubleValue();
                            sb.append(formatAlwaysZeroD(cVal, decimals));

                        }

                        out.println(sb.toString());
                    }

                }
            }

            log.println(bAll);
            new BlockAnalysis(bAll.getBlockData()).allPartialNorms();
            log.println(Dic.getMsg("INFO_SEPARATOR"));
            out.println("****");
        }

        log.flush();
        out.flush();
    }

    /**
     *
     * BasisSet values in standard format and Fortran style (0.xxxxxxD±xx).
     * Formats the given double value ensuring the leading digit is always zero,
     * followed by the specified number of decimals and a Fortran-style exponent
     * ('D' notation).
     *
     * Example: 13.0 with 6 decimals -> 0.130000D+02
     *
     * @param value the numeric value to format
     * @param decimals the number of decimal places after the dot (maximum reliable precision: 15 digits, IEEE 754 double)
     * @return formatted string in BasisSet/Fortran 'D' exponent format
     */
    private static String formatAlwaysZeroD(double value, int decimals) {
        if(decimals>15) decimals=15;
        if (value == 0.0) {
            return String.format("0.%0" + decimals + "dD+00", 0);
        }

        int exponent = (int) Math.floor(Math.log10(Math.abs(value))) + 1;
        double mantissa = value / Math.pow(10, exponent);

        return String.format("%." + decimals + "fD%+03d", mantissa, exponent);
    }

}
