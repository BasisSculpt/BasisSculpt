# BasisSculpt Tool

**Primitive Gaussian Function Reduction Tool**  
Version: `2025.4.v1`

This tool performs norm-controlled elimination of primitive Gaussian functions from a given block (e.g., a shell of a basis set), enabling compact representation while preserving numerical accuracy.

---

## 🚀 Purpose

To analyze and report the numerical significance of each primitive Gaussian function in a basis block (e.g., cc-pVDZ Carbon s-shell). The tool provides norm contribution analysis and allows informed manual reduction, but does not automatically perform basis optimization.

---

## 🧠 Key Features

- Computes full block norm and norm loss upon individual function removal
- Quantifies each primitive's contribution in percent
- Supports normalization and high-precision output
- Generates full logs and diagnostic reports
- Ready for publication and reproducible diagnostics

---

## 📥 Usage

### ✅ Requirements
- **Java 21 or any compatible newer version** (required for `.jar` version)

### 💻 Platform-Independent (JAR) Binary

```bash
java -jar BasisSculpt-2025.4.1.jar \
  --input <input_file> \
  --threshold <norm_tolerance> \
  [--normalize] \
  [--output <output_file>] \
  [--output-gbs-format yes|no] \
  [--output-threshold <numeric_threshold>] \
  [--log <log_file>] \
  [--report <summary_file>] \
  [--version-tag <tag>] \
  [--fixed-basis-name <label>] \
  [--verbose]
```

### 💻 Windows Executable

```bash
BasisSculpt.exe \
  --input <input_file> \
  --threshold <norm_tolerance> \
  [--normalize] \
  [--output <output_file>] \
  [--output-gbs-format yes|no] \
  [--output-threshold <numeric_threshold>] \
  [--log <log_file>] \
  [--report <summary_file>] \
  [--version-tag <tag>] \
  [--fixed-basis-name <label>] \
  [--verbose]
```

---

## ⚙️ Flags and Parameters

### Required:
- `--input <path>` – Input file containing Gaussian primitives (one shell/block).
- `--threshold <float|int>` – Maximum allowed norm deviation considered significant (e.g., `1e-5` or `5` for `1e-5` ). 

### Recommended:
- `--normalize` – Enables renormalization after contribution analysis.
- `--output <path>` – Output file for basis (after optional normalization).
- `--output-gbs-format yes|no` – Output format: 'yes' for full .gbs format with atom headers, 'no' for plain block format suitable for Gaussian input (GEN). Default: yes 
- `--output-threshold <int>` – Threshold controlling numeric precision of basis function output values.  Values below this threshold will be formatted with corresponding decimal precision (maximum allowed reliable precision: 15 digits, IEEE 754 double). Default: X.XXXXXXD+XX 
- `--log <path>` – Log file with full step-by-step analysis.
- `--report <path>` – Write summary report with norm loss and contributions.
- `--version-tag <id>` – Custom identifier to track tool version and analysis context.
- `--fixed-basis-name <label>` – Tag indicating the original basis set used (e.g., `cc-pVDZ`).
- `--verbose` – Print detailed output to terminal during execution.

### Optional:
- `--help` – Displays all available options and exits. Other arguments ignored always.
- `--license` – Displays the full BSD-3-Clause license text and exits. 
- `--citation` – Displays the recommended citation information and exits. 

---

## 📘 Example

Evaluating cc-pVDZ Hydrogen s-shell with a threshold of 1e-5 with `java` and `2025.4.1` version:

```bash
java -jar BasisSculpt-2025.4.1.jar \
  --input cc-pvdz_H_s.txt \
  --threshold 1e-5 \
  --normalize \
  --output analyzed_H_s.txt \
  --log H_s_analysis.log \
  --version-tag cc-pvdz-H_s-v1 \
  --fixed-basis-name cc-pVDZ 
```

---

## 🧪 Output
- Primitive-by-primitive contribution table (in log and report)
- Full norm, loss percentages, individual function impact
- Optional renormalized basis block

---

## 📌 Scope

This tool is intended for **diagnostic evaluation only**. No automatic optimization or elimination is performed. Users may manually remove primitives based on contribution analysis and verify results independently.

---

## 🧾 License

This project is licensed under the BSD 3-Clause License.

```
Copyright (c) 2025, M. Macernis
All rights reserved.
```

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.  
2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.  
3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

**This software is provided "as is" without any warranty**, express or implied, including but not limited to the warranties of merchantability or fitness for a particular purpose.  
In no event shall the copyright holder or contributors be liable for any damages.

---

## 🧭 Citation

If you use this tool in a scientific publication, please cite both the tool and the basis set source used in your computations. 
This ensures reproducibility and traceability.

> Note: Use the `--version-tag` option during execution to assign an identifiable version to your results.

A formal citation text will be provided upon release of the corresponding peer-reviewed article.

Until then, please cite:

>  Mačernis M., *BasisSculpt*, version `2025.4.v1`, 2025.

### Example command:
```bash
--version-tag cc-pvdz-H_s-v1
```
### In publication recommendation:
`cc-pvdz-H_s-v1`[1], based on `cc-pVDZ` [2-5]

> Note: This tool uses the GBS format independently of Gaussian or other quantum chemistry packages; however, basis set references may follow the conventions of those ecosystems. It is recommended to use Basis Set Exchange or similar tools for format conversion.

### References

[1] Mačernis M., BasisSculpt, version 2025.4.v1, 2025.

[2] Pritchard, B. P., Altarawy, D., Didier, B., Gibson, T. D., Windus, T. L. A New Basis Set Exchange: An Open, Up-to-date Resource for the Molecular Sciences Community J. Chem. Inf. Model. 59, 4814–4820 (2019). DOI: 10.1021/acs.jcim.9b00725

[3] Feller, D. The role of databases in support of computational chemistry calculations J. Comput. Chem. 17, 1571–1586 (1996). DOI: 10.1002/(SICI)1096-987X(199610)17:13<1571::AID-JCC9>3.0.CO;2-P

[4] Schuchardt, K. L., Didier, B. T., Elsethagen, T., Sun, L., Gurumoorthi, V., Chase, J., Li, J., Windus, T. L. Basis Set Exchange: A Community Database for Computational Sciences J. Chem. Inf. Model. 47, 1045–1052 (2007). DOI: 10.1021/ci600510j

---

## 🆘 Help

Use the `--help` flag to display:
- Tool description and purpose
- All command-line arguments and defaults
- Version and license summary
- Citation guidance if available

Example:
```bash
java -jar basissetcorrection.jar --help
```

---

Supervised and validated by a physicist pursuing high-precision computational models.

Žemaitijos certified 😄
