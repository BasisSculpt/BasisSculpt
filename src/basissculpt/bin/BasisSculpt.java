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
package basissculpt.bin;

import basissculpt.etc.Args;
import basissculpt.etc.ArgsGet;
import basissculpt.etc.Dic;
import basissculpt.etc.Settings;
import basissculpt.lib.read.BasisParser;
import basissculpt.lib.read.BasisPrinter;
import java.io.IOException;
import java.util.List;

/**
 * BasisSculpt - Sculpting Gaussian Basis Sets via Norm Control
 *
 * @author M. Macernis
 * @version 2025.4.v1
 */
public class BasisSculpt {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            start(args);
        } catch (IOException ex) {
        } finally {
            LogFile log = LogFile.getInstance();
            log.flush();
            log.close();
            Output out = Output.getInstance();
            out.flush();
            out.close();
            Report report = Report.getInstance();
            report.flush();
            report.close();
        }
    }

    public static void start(String[] args) throws IOException {

        new ArgsGet(new Args(args));

        ArgsGet argset = ArgsGet.getInstance();

        if (argset.isKey("version")) {
            System.out.println(basissculpt.etc.Version.codeVersion);
            System.exit(0);
        }
        if (argset.isKey("citation")) {
            System.out.println(String.format("""
BasisSculpt - Basis Set Reduction and Correction Tool
Version: %s

If you use this tool in a scientific publication, please cite as:

    Macernis, M., 
    Component-wise AO basis reduction: norm loss, negative contribution normalization, and functional implications. 
    Phys. Chem. Chem. Phys. 2025, 27 (27), 14555-14564.
    https://doi.org/10.1039/D5CP01681A                                          
                                             
""", basissculpt.etc.Version.codeVersion));
            System.exit(0);
        }

        if (argset.isKey("license")) {
            System.out.println("""                               
 BSD 3-Clause License
 
 Copyright (c) 2025, M. Macernis
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, 
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation 
    and/or other materials provided with the distribution.

 3. Neither the name of the copyright holder nor the names of its contributors 
    may be used to endorse or promote products derived from this software 
    without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
                               """);
            System.exit(0);
        }

        if (argset.isKey("help")) {

            String compile = basissculpt.etc.Version.getVersion("exe");
//#if develop != "exe"
            compile = basissculpt.etc.Version.getVersion("jar");
//#endif

            System.out.println(String.format("""
BasisSculpt - Basis Set Reduction and Correction Tool
Version: %s
Citation: Macernis M., Phys. Chem. Chem. Phys. 2025, 27 (27), 14555-14564.
License: BSD 3-Clause
Copyright (C) 2025 M. Macernis

Usage:
  %s \\
    --input <input_file> \\
    --threshold <norm_tolerance> \\
    [--normalize] \\
    [--output <output_file>] \\
    [--output-gbs-format yes|no] \\
    [--output-threshold <numeric_threshold>] \\
    [--log <log_file>] \\
    [--report <summary_file>] \\
    [--version-tag <tag>] \\
    [--fixed-basis-name <label>] \\
    [--verbose] \\
    [--help] \\
    [--license] \\
    [--citation]

Arguments:
  --input <file>           Input basis set file (.gbs format)
  --threshold <value>      Maximum allowed deviation from original norm (e.g., 1e-8)
  --normalize              Enable post-analysis re-normalization
  --output <file>          File to write the resulting basis set if set "normalize"
                           Default: stdout
  --output-gbs-format yes|no
                           Output format: 'yes' for full .gbs format with atom headers,
                           'no' for plain block format suitable for Gaussian input (GEN).
                           Default: yes         
  --output-threshold <value>
                           Threshold controlling numeric precision of basis function output values.
                           Values below this threshold will be formatted with corresponding decimal precision
                           (maximum reliable precision: 15 digits, IEEE 754 double). 
                           Default: X.XXXXXXD+XX                                                                              
  --log <file>             Write detailed diagnostic log to file
                           Default: stdout
  --report <file>          Write summary report with norm loss and contributions
  --version-tag <tag>      Custom tag to label this analysis
  --fixed-basis-name <str> Basis set name label for output
  --verbose                Enable verbose console output
  --help                   Show this help message and exits (other arguments ignored)
  --license                Displays the full BSD-3-Clause license text and exits. 
  --citation               Displays the recommended citation information and exits. 

Example:
  %s --input def2svp.gbs --threshold 1e-8 --normalize --output def2svp_reduced.gbs

    """, basissculpt.etc.Version.codeVersion, compile, compile));
            System.exit(0);
        }

        LogFile log = LogFile.getInstance();
        Report report = Report.getInstance();

        if (argset.isKey("version-tag")) {
            String version = argset.getString("version-tag");
            log.println(Dic.getMsg("INFO_VERSION_TAG", version));
            report.println(Dic.getMsg("INFO_VERSION_TAG", version));
        }

        if (argset.isKey("fixed-basis-name")) {
            String name = argset.getString("fixed-basis-name");
            log.println(Dic.getMsg("INFO_FIXED_BASIS_NAME", name));
            report.println(Dic.getMsg("INFO_FIXED_BASIS_NAME", name));
        }

        int precision = Settings.getInstance().getMC().getPrecision();
        log.println(Dic.getMsg("INFO_MATH_CONTEXT_PRECISION", precision));

        List<BasisParser.AtomSection> atoms = BasisParser
                .parse(Settings.getInstance().getInput());

        new BasisPrinter(atoms).run();
    }

}
